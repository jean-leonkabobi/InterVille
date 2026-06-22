package com.transport.api.sync.dto;

import lombok.Data;

import java.util.List;

@Data
public class SyncRequest {
    private Long agenceId;
    private List<SyncOperationDto> operations;
}