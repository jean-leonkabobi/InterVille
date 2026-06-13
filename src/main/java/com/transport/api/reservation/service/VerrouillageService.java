package com.transport.api.reservation.service;

import com.transport.api.bus.entity.Bus;
import com.transport.api.bus.entity.Siege;
import com.transport.api.bus.repository.BusRepository;
import com.transport.api.bus.repository.SiegeRepository;
import com.transport.api.common.exception.ResourceNotFoundException;
import com.transport.api.reservation.dto.*;
import com.transport.api.reservation.entity.VerrouSiege;
import com.transport.api.reservation.repository.ReservationRepository;
import com.transport.api.reservation.repository.VerrouSiegeRepository;
import com.transport.api.trajet.entity.Trajet;
import com.transport.api.trajet.repository.TrajetRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VerrouillageService {

    private static final Logger logger = LoggerFactory.getLogger(VerrouillageService.class);
    private static final String TRAJET_NOT_FOUND = "Trajet non trouvé";
    private static final String BUS_NOT_FOUND = "Bus non trouvé";

    private final VerrouSiegeRepository verrouSiegeRepository;
    private final ReservationRepository reservationRepository;
    private final TrajetRepository trajetRepository;
    private final BusRepository busRepository;
    private final SiegeRepository siegeRepository;

    @Value("${reservation.verrouillage.timeout-minutes:15}")
    private long timeoutMinutes;

    public SiegesDisponiblesResponse getSiegesDisponibles(Long trajetId) {
        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new ResourceNotFoundException(TRAJET_NOT_FOUND));

        Bus bus = busRepository.findById(trajet.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException(BUS_NOT_FOUND));

        List<Siege> allSieges = siegeRepository.findByBusId(bus.getId());
        List<VerrouSiege> verrousActifs = verrouSiegeRepository.findByTrajetId(trajetId);

        List<Long> siegesVerrouillesIds = verrousActifs.stream()
                .map(VerrouSiege::getSiegeId)
                .toList();

        List<Long> siegesReservesIds = reservationRepository.findReservedSeatIdsByTrajetId(trajetId);

        List<SiegeDisponibleDto> siegesDto = new ArrayList<>();
        int libres = 0;
        int verrouilles = 0;
        int reserves = 0;

        for (Siege siege : allSieges) {
            String statut;
            LocalDateTime verrouExpireAt = null;

            if (siegesReservesIds.contains(siege.getId())) {
                statut = "RESERVE";
                reserves++;
            } else if (siegesVerrouillesIds.contains(siege.getId())) {
                statut = "VERROUILLE";
                verrouilles++;
                VerrouSiege verrou = verrousActifs.stream()
                        .filter(v -> v.getSiegeId().equals(siege.getId()))
                        .findFirst()
                        .orElse(null);
                if (verrou != null) {
                    verrouExpireAt = verrou.getExpiresAt();
                }
            } else {
                statut = "LIBRE";
                libres++;
            }

            siegesDto.add(SiegeDisponibleDto.builder()
                    .siegeId(siege.getId())
                    .numero(siege.getSeatNumber())
                    .type(siege.getSeatType().name())
                    .statut(statut)
                    .verrouExpireAt(verrouExpireAt)
                    .build());
        }

        return SiegesDisponiblesResponse.builder()
                .trajetId(trajetId)
                .totalSieges(allSieges.size())
                .siegesLibres(libres)
                .siegesVerrouilles(verrouilles)
                .siegesReserves(reserves)
                .sieges(siegesDto)
                .build();
    }

    @Transactional
    public VerrouillerResponse verrouillerSiege(Long trajetId, String numeroSiege, UUID sessionId) {
        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new ResourceNotFoundException(TRAJET_NOT_FOUND));

        Bus bus = busRepository.findById(trajet.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException(BUS_NOT_FOUND));

        Siege siege = siegeRepository.findByBusIdAndSeatNumber(bus.getId(), numeroSiege)
                .orElseThrow(() -> new ResourceNotFoundException("Siège non trouvé: " + numeroSiege));

        boolean estReserve = reservationRepository.isSeatReservedForTrajet(trajetId, siege.getId());
        if (estReserve) {
            return VerrouillerResponse.builder()
                    .success(false)
                    .message("Ce siège est déjà réservé")
                    .siegesVerrouilles(new ArrayList<>())
                    .siegesIndisponibles(List.of(numeroSiege))
                    .build();
        }

        boolean estVerrouille = verrouSiegeRepository.existsByTrajetIdAndSiegeIdAndExpiresAtAfter(
                trajetId, siege.getId(), LocalDateTime.now());

        if (estVerrouille) {
            return VerrouillerResponse.builder()
                    .success(false)
                    .message("Ce siège est déjà verrouillé par une autre session")
                    .siegesVerrouilles(new ArrayList<>())
                    .siegesIndisponibles(List.of(numeroSiege))
                    .build();
        }

        VerrouSiege verrou = new VerrouSiege();
        verrou.setTrajetId(trajetId);
        verrou.setSiegeId(siege.getId());
        verrou.setReservationSessionId(sessionId);
        verrou.setLockedAt(LocalDateTime.now());
        verrou.setExpiresAt(LocalDateTime.now().plusMinutes(timeoutMinutes));

        verrouSiegeRepository.save(verrou);

        return VerrouillerResponse.builder()
                .success(true)
                .message("Siège verrouillé avec succès")
                .siegesVerrouilles(List.of(numeroSiege))
                .siegesIndisponibles(new ArrayList<>())
                .expiresAt(verrou.getExpiresAt())
                .build();
    }

    @Transactional
    public VerrouillerResponse verrouillerGroupesSieges(Long trajetId, List<String> numerosSieges, UUID sessionId) {
        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new ResourceNotFoundException(TRAJET_NOT_FOUND));

        Bus bus = busRepository.findById(trajet.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException(BUS_NOT_FOUND));

        List<String> siegesVerrouilles = new ArrayList<>();
        List<String> siegesIndisponibles = new ArrayList<>();

        for (String numero : numerosSieges) {
            VerrouillerResponse response = verrouillerSiege(trajetId, numero, sessionId);
            if (response.isSuccess()) {
                siegesVerrouilles.add(numero);
            } else {
                siegesIndisponibles.add(numero);
            }
        }

        boolean allSuccess = siegesIndisponibles.isEmpty();

        return VerrouillerResponse.builder()
                .success(allSuccess)
                .message(allSuccess ? "Tous les sièges ont été verrouillés" : "Certains sièges n'ont pas pu être verrouillés")
                .siegesVerrouilles(siegesVerrouilles)
                .siegesIndisponibles(siegesIndisponibles)
                .expiresAt(LocalDateTime.now().plusMinutes(timeoutMinutes))
                .build();
    }

    @Transactional
    public void libererVerrou(Long trajetId, String numeroSiege, UUID sessionId) {
        Trajet trajet = trajetRepository.findById(trajetId)
                .orElseThrow(() -> new ResourceNotFoundException(TRAJET_NOT_FOUND));

        Bus bus = busRepository.findById(trajet.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException(BUS_NOT_FOUND));

        Siege siege = siegeRepository.findByBusIdAndSeatNumber(bus.getId(), numeroSiege)
                .orElseThrow(() -> new ResourceNotFoundException("Siège non trouvé: " + numeroSiege));

        verrouSiegeRepository.deleteByTrajetIdAndSessionId(trajetId, sessionId);
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void nettoyerVerrousExpires() {
        int deletedCount = verrouSiegeRepository.deleteAllExpired(LocalDateTime.now());
        if (deletedCount > 0) {
            logger.info("Nettoyage: {} verrous expirés supprimés", deletedCount);
        }
    }
}