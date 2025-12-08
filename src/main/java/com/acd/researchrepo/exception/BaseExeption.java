package com.acd.researchrepo.exception;

import org.apache.http.HttpStatus;

import lombok.Getter;

@Getter
public class BaseExeption extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    public BaseExeption(String message, String errorCode, HttpStatus status) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}
