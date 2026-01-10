package com.acd.researchrepo.config;

import java.io.IOException;
import java.security.SecureRandom;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // runs first before spring MVC
public class TraceIdFilter implements Filter {
    private static final String TRACE_ID_HEADER = "Research-Repo-Trace-Id";
    private static final int TRACE_ID_LENGTH = 12;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String traceId = generateTraceId();

        try {
            // Propagate to MDC for logging
            MDC.put("traceId", traceId);

            // Attach to request for internal application use
            request.setAttribute("traceId", traceId);

            // Set in response so client knows the ID for debugging
            httpResponse.setHeader(TRACE_ID_HEADER, traceId);

            chain.doFilter(request, response);
        } finally {
            MDC.remove("traceId");
        }
    }

    private String generateTraceId() {
        byte[] bytes = new byte[TRACE_ID_LENGTH / 2];
        RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(TRACE_ID_LENGTH);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
