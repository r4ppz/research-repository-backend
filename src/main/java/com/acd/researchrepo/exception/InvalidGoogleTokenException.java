package com.acd.researchrepo.exception;

import org.springframework.http.HttpStatus;

public class InvalidGoogleTokenException extends BaseException {
    public InvalidGoogleTokenException(String message) {
        super(message, "INVALID_GOOGLE_TOKEN", HttpStatus.UNAUTHORIZED);
    }

    public InvalidGoogleTokenException(String message, Throwable cause) {
        super(message, "INVALID_GOOGLE_TOKEN", HttpStatus.UNAUTHORIZED);
        this.initCause(cause);
    }
}
