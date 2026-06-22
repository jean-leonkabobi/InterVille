package com.transport.api.sync.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SyncResultDto {
    private String operationId;
    private Boolean success;
    private String message;
    private Boolean conflict;
    private String conflictResolution;
}