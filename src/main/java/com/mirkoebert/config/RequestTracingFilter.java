package com.mirkoebert.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Order(1) // Run very early
public class RequestTracingFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String TRACE_ID_MDC_KEY = "traceId";
    static final int MAX_TRACE_ID_LENGTH = 32;
    private static final Pattern ALLOWED_TRACE_ID = Pattern.compile("[A-Za-z0-9_-]{1," + MAX_TRACE_ID_LENGTH + "}");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (!isValidTraceId(traceId)) {
            // Short hex id; charset matches ALLOWED_TRACE_ID
            traceId = UUID.randomUUID().toString().substring(0, 8);
        }

        MDC.put(TRACE_ID_MDC_KEY, traceId);

        try {
            // Echo trace ID back to client (very useful for debugging)
            response.setHeader(TRACE_ID_HEADER, traceId);

            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_MDC_KEY);
        }
    }

    static boolean isValidTraceId(String traceId) {
        return traceId != null && ALLOWED_TRACE_ID.matcher(traceId).matches();
    }
}
