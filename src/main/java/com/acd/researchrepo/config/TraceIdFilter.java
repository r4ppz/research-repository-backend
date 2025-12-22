package com.acd.researchrepo.config;

import java.io.IOException;
import java.security.SecureRandom;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // runs first before spring MVC
public class TraceIdFilter implements Filter {
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final int TRACE_ID_LENGTH = 12;

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String traceId = httpRequest.getHeader(TRACE_ID_HEADER);

        if (StringUtils.isBlank(traceId)) {
            traceId = generateTraceId();
        }

        // Propagate to logs and downstream services
        MDC.put("traceId", traceId);

        ((HttpServletResponse) response).setHeader(TRACE_ID_HEADER, traceId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("traceId");
        }
    }

    private String generateTraceId() {
        byte[] bytes = new byte[TRACE_ID_LENGTH / 2];
        new SecureRandom().nextBytes(bytes);
        StringBuilder sb = new StringBuilder(TRACE_ID_LENGTH);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
