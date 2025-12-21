package com.acd.researchrepo.config;

import java.io.IOException;

import com.acd.researchrepo.dto.external.error.ErrorResponse;
import com.acd.researchrepo.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException ex) throws IOException {
        // Determine precise error code
        ErrorCode errorCode = switch (request.getRequestURI()) {
            case "/api/auth/google" -> ErrorCode.DOMAIN_NOT_ALLOWED;
            case "/api/files/*" -> ErrorCode.ACCESS_DENIED;
            default -> ErrorCode.ACCESS_DENIED;
        };

        // Build canonical response
        ErrorResponse error = ErrorResponse
                .builder()
                .code(errorCode.name())
                .message(errorCode.getDefaultMessage())
                .details(null)
                .traceId(MDC.get("traceId"))
                .build();

        // Never leak details
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
