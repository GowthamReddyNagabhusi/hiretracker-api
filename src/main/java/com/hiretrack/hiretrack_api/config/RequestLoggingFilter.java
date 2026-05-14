package com.hiretrack.hiretrack_api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Logs every HTTP request with timing, status code, and a correlation ID.
 *
 * Example log output:
 *   HTTP POST /api/auth/login → 200 (45ms) [corr-id: a1b2c3]
 *
 * The correlation ID (X-Request-Id) is added to MDC so all logs
 * within the same request share the same ID — essential for
 * tracing issues in production.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Skip static resources and actuator noise
        String path = request.getRequestURI();
        if (path.startsWith("/actuator") || path.contains("swagger") || path.contains("api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Generate or reuse correlation ID
        String requestId = request.getHeader("X-Request-Id");
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }

        MDC.put("requestId", requestId);
        response.setHeader("X-Request-Id", requestId);

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            if (status >= 500) {
                log.error("HTTP {} {} → {} ({}ms) [{}]",
                        request.getMethod(), path, status, duration, requestId);
            } else if (status >= 400) {
                log.warn("HTTP {} {} → {} ({}ms) [{}]",
                        request.getMethod(), path, status, duration, requestId);
            } else {
                log.info("HTTP {} {} → {} ({}ms) [{}]",
                        request.getMethod(), path, status, duration, requestId);
            }

            MDC.remove("requestId");
        }
    }
}
