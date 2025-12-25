package com.acd.researchrepo.exception;

import java.util.List;

import com.acd.researchrepo.dto.external.error.ErrorResponse;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object details;
    private final String customMessage;

    // For business/auth errors with DEFAULT message
    public ApiException(ErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage(), null, null);
    }

    // For business/auth errors with CUSTOM message
    public ApiException(ErrorCode errorCode, String customMessage) {
        this(errorCode, customMessage, null, null);
    }

    // For validation errors (uses default message but has details)
    public ApiException(ErrorCode errorCode, List<ErrorResponse.FieldError> details) {
        this(errorCode, errorCode.getDefaultMessage(), details, null);
    }

    // Private constructor that does the real work
    private ApiException(ErrorCode errorCode, String message, Object details, String customMessage) {
        super(message); // This is what gets logged server-side
        this.errorCode = errorCode;
        this.details = details;
        this.customMessage = customMessage; // This is what goes to frontend
    }

    // Getter that prioritizes custom message over default
    public String getDisplayMessage() {
        return (customMessage != null) ? customMessage : errorCode.getDefaultMessage();
    }
}
