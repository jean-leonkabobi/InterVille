package com.transport.api.sync.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ConflitResolutionResponse {
    private Boolean success;
    private String message;
    private List<ConflitDto> conflitsResolus;
    private List<ConflitDto> conflitsPerdants;
}