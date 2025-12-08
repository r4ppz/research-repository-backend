package com.acd.researchrepo.exception;

import com.acd.researchrepo.dto.external.error.ErrorResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(
            BaseException exception, WebRequest request) {
        log.error("BaseException just happened woah. Message: {}", exception.getMessage());

        ErrorResponse error = new ErrorResponse(exception.getErrorCode(), exception.getMessage());
        return new ResponseEntity<>(error, exception.getStatus());
    }

    @ExceptionHandler(DomainNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleDomainNotAllowedException(
            DomainNotAllowedException exception,
            WebRequest request) {
        log.error("DomainNotAllowedException just happened woah. Message: {}", exception.getMessage());

        ErrorResponse error = new ErrorResponse(exception.getErrorCode(), exception.getMessage());
        return new ResponseEntity<>(error, exception.getStatus());
    }

    @ExceptionHandler(InvalidGoogleTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidGoogleTokenException(
            InvalidGoogleTokenException exception,
            WebRequest request) {
        log.error("InvalidGoogleTokenException just happened woah. Message: {}", exception.getMessage());

        ErrorResponse error = new ErrorResponse(exception.getErrorCode(), exception.getMessage());
        return new ResponseEntity<>(error, exception.getStatus());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(
            InvalidTokenException exception,
            WebRequest request) {
        log.error("InvalidTokenException just happened woah. Message: {}", exception.getMessage());

        ErrorResponse error = new ErrorResponse(exception.getErrorCode(), exception.getMessage());
        return new ResponseEntity<>(error, exception.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception exception, WebRequest request) {
        log.error("Generic exception just happened woah. Message: {}", exception.getMessage());

        ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred");
        return new ResponseEntity<>(error, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
