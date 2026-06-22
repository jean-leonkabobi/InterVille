package com.transport.api.sync.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ConflitDto {
    private String operationId;
    private Long trajetId;
    private String siegeNumber;
    private LocalDateTime localTimestamp;
    private LocalDateTime serverTimestamp;
    private Boolean isWinner;
    private String resolutionMessage;
}