package com.acd.researchrepo.dto.external.error;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
    private final String code;
    private final String message;
    private final Object details;
    private final String traceId;

    @Getter
    @Builder
    public static class FieldError {
        private final String field;
        private final String message;
    }
}
