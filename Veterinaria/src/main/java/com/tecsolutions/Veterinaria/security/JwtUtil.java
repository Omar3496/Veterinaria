package com.tecsolutions.Veterinaria.security;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Component
public class JwtUtil {

    private final String secretKey;
    private final long expirationMinutes;
    private final ObjectMapper objectMapper;

    public JwtUtil(
            @Value("${jwt.secret:${SECRET_KEY:fallback-dev-sirbull-2026}}") String secretKey,
            @Value("${jwt.expiration-minutes:1440}") long expirationMinutes,
            ObjectMapper objectMapper) {
        this.secretKey = (secretKey == null || secretKey.isBlank()) ? "fallback-dev-sirbull-2026" : secretKey;
        this.expirationMinutes = expirationMinutes <= 0 ? 1440 : expirationMinutes;
        this.objectMapper = objectMapper;
    }

    public String generateToken(String username, String role) {
        try {
            long now = Instant.now().getEpochSecond();
            long exp = now + (expirationMinutes * 60);

            String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            String payloadJson = objectMapper.writeValueAsString(Map.of(
                    "sub", username,
                    "rol", role,
                    "exp", exp
            ));

            String headerEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
            String payloadEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

            String content = headerEncoded + "." + payloadEncoded;
            String signatureEncoded = sign(content);

            return content + "." + signatureEncoded;
        } catch (Exception e) {
            throw new RuntimeException("Error al generar JWT", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            if (token == null || token.isBlank()) {
                return false;
            }
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }

            String content = parts[0] + "." + parts[1];
            String expectedSignature = sign(content);
            if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
                return false;
            }

            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            JsonNode payload = objectMapper.readTree(payloadBytes);
            if (!payload.has("exp") || !payload.has("sub")) {
                return false;
            }

            long exp = payload.get("exp").asLong();
            long now = Instant.now().getEpochSecond();
            return now < exp;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsername(String token) {
        try {
            String[] parts = token.split("\\.");
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            JsonNode payload = objectMapper.readTree(payloadBytes);
            return payload.has("sub") ? payload.get("sub").asText() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public String getRole(String token) {
        try {
            String[] parts = token.split("\\.");
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            JsonNode payload = objectMapper.readTree(payloadBytes);
            return payload.has("rol") ? payload.get("rol").asText() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String sign(String content) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] rawHmac = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
    }
}
