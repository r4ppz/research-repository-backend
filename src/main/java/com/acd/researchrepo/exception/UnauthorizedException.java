package com.acd.researchrepo.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseException {

    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        initCause(cause);
    }
}
