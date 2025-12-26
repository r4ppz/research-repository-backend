package com.acd.researchrepo.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // HTTPS 400
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Invalid request data"),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Malformed request"),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "Authentication failed"),

    // HTTP 401
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "Missing or invalid credentials"),
    REFRESH_TOKEN_REVOKED(HttpStatus.UNAUTHORIZED, "Refresh token expired or missing"),

    // HTTP 403
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access denied"),
    DOMAIN_NOT_ALLOWED(HttpStatus.FORBIDDEN, "Email domain not allowed"),

    // HTTP 404
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    RESOURCE_NOT_AVAILABLE(HttpStatus.NOT_FOUND, "Resource not available"),

    // HTTP 409
    DUPLICATE_REQUEST(HttpStatus.CONFLICT, "Duplicate active request exists"),
    REQUEST_ALREADY_FINAL(HttpStatus.CONFLICT, "Request in terminal state"),

    // HTTP 413/415
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "File exceeds 20MB limit"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported file type"),

    // HTTP 429
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded"),

    // HTTP 500
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    FILE_STORAGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "File storage error"),

    // HTTP 503
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Service unavailable");

    private final HttpStatus httpStatus;
    private final String defaultMessage;
}
