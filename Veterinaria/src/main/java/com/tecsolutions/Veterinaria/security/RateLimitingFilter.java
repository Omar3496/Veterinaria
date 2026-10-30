package com.tecsolutions.Veterinaria.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final long ONE_MINUTE_MS = 60_000L;

    // shortcut: In-memory sliding window rate limiter per JVM node, replace with distributed Redis token bucket when clustering
    private final Map<String, List<Long>> requestLog = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !request.getMethod().equalsIgnoreCase("POST") || (!uri.equals("/token") && !uri.endsWith("/token"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String clientIp = extractClientIp(request);
        long now = System.currentTimeMillis();

        boolean allowed;
        synchronized (requestLog) {
            List<Long> timestamps = requestLog.computeIfAbsent(clientIp, k -> new ArrayList<>());
            timestamps.removeIf(t -> now - t > ONE_MINUTE_MS);
            if (timestamps.size() < MAX_REQUESTS_PER_MINUTE) {
                timestamps.add(now);
                allowed = true;
            } else {
                allowed = false;
            }
        }

        if (!allowed) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"detail\":\"Demasiados intentos. Por favor, intente de nuevo en un minuto.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    public void reset() {
        requestLog.clear();
    }

    private String extractClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
