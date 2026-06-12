package com.transport.api.trajet.controller;

import com.transport.api.trajet.dto.*;
import com.transport.api.trajet.enums.StatutTrajet;
import com.transport.api.trajet.service.TrajetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trajets")
@RequiredArgsConstructor
public class TrajetController {

    private final TrajetService trajetService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrajetDto> createTrajet(@Valid @RequestBody TrajetCreateRequest request) {
        return new ResponseEntity<>(trajetService.createTrajet(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<List<TrajetDto>> getAllTrajets() {
        return ResponseEntity.ok(trajetService.getAllTrajets());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CLIENT')")
    public ResponseEntity<TrajetDto> getTrajetById(@PathVariable Long id) {
        return ResponseEntity.ok(trajetService.getTrajetById(id));
    }

    @GetMapping("/recherche")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CLIENT')")
    public ResponseEntity<List<TrajetResultDto>> searchTrajets(
            @RequestParam String departureCity,
            @RequestParam String arrivalCity,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        RechercheTrajetRequest request = new RechercheTrajetRequest();
        request.setDepartureCity(departureCity);
        request.setArrivalCity(arrivalCity);
        request.setDate(date);

        return ResponseEntity.ok(trajetService.searchTrajets(request));
    }

    @GetMapping("/recherche/public")
    public ResponseEntity<List<TrajetResultDto>> searchTrajetsPublic(
            @RequestParam String departureCity,
            @RequestParam String arrivalCity,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return ResponseEntity.ok(trajetService.searchTrajetsPublic(departureCity, arrivalCity, date));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrajetDto> updateTrajet(@PathVariable Long id, @Valid @RequestBody TrajetCreateRequest request) {
        return ResponseEntity.ok(trajetService.updateTrajet(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTrajet(@PathVariable Long id) {
        trajetService.deleteTrajet(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHAUFFEUR')")
    public ResponseEntity<Void> updateTrajetStatus(@PathVariable Long id, @RequestParam StatutTrajet status) {
        trajetService.updateTrajetStatus(id, status);
        return ResponseEntity.noContent().build();
    }
}