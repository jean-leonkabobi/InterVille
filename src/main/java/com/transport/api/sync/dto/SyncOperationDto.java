package com.transport.api.sync.dto;

import com.transport.api.sync.enums.OperationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SyncOperationDto {
    private String id;  // UUID local
    private OperationType type;
    private String payload;  // JSON
    private LocalDateTime localTimestamp;
}