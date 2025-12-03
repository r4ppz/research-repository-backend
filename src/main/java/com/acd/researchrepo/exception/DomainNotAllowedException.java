package com.acd.researchrepo.exception;

public class DomainNotAllowedException extends RuntimeException {

    public DomainNotAllowedException(String message) {
        super(message);
    }
}
