package com.acd.researchrepo.exception;

import java.util.List;
import java.util.stream.Collectors;

import com.acd.researchrepo.dto.external.error.ErrorResponse;

import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(
            ApiException exception,
            HttpServletRequest request) {
        String traceId = MDC.get("traceId");
        log.warn("Business error [{}]: {}", exception.getErrorCode(), exception.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .code(exception.getErrorCode().name())
                .message(exception.getDisplayMessage())
                .details(exception.getDetails())
                .traceId(traceId)
                .build();

        return ResponseEntity
                .status(exception.getErrorCode().getHttpStatus())
                .body(error);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String traceId = MDC.get("traceId");

        List<ErrorResponse.FieldError> fieldErrors = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.FieldError
                        .builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse
                .builder()
                .code(ErrorCode.VALIDATION_ERROR.name())
                .message("Invalid request data")
                .details(fieldErrors)
                .traceId(traceId)
                .build();

        log.warn("Validation failed for {}: {}", request.getDescription(false), fieldErrors);

        return ResponseEntity.badRequest().body(errorResponse);
    }

    // Ultimate fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandledException(Exception exception) {
        String traceId = MDC.get("traceId");
        log.error("UNEXPECTED ERROR [{}]: {}", traceId, exception.getMessage(), exception);

        ErrorResponse errorResponse = ErrorResponse
                .builder()
                .code(ErrorCode.INTERNAL_ERROR.name())
                .message("Internal server error")
                .details(null)
                .traceId(traceId)
                .build();

        return ResponseEntity.internalServerError().body(errorResponse);
    }

    // Prevent information leakage
    @Override
    public ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        // Force all unhandled exceptions through the proper format
        if (body == null || !(body instanceof ErrorResponse)) {
            String traceId = MDC.get("traceId");
            return ResponseEntity.status(status)
                    .body(ErrorResponse
                            .builder()
                            .code(ErrorCode.INTERNAL_ERROR.name())
                            .message("Internal server error")
                            .details(null)
                            .traceId(traceId)
                            .build());

        }
        return super.handleExceptionInternal(ex, body, headers, status, request);
    }
}
