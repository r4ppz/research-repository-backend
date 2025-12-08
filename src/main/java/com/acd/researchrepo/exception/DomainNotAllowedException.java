package com.acd.researchrepo.exception;

import org.springframework.http.HttpStatus;

public class DomainNotAllowedException extends BaseException {

    public DomainNotAllowedException(String message) {
        super(message, "DOMAIN_NOT_ALLOWED", HttpStatus.FORBIDDEN);
    }

    public DomainNotAllowedException(String message, Throwable cause) {
        super(message, "DOMAIN_NOT_ALLOWED", HttpStatus.FORBIDDEN);
        this.initCause(cause);
    }
}
