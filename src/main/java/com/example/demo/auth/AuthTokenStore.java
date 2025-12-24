package com.example.demo.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthTokenStore {
    private final Map<String, AuthSession> sessions = new ConcurrentHashMap<>();
    private final Duration ttl = Duration.ofHours(6);

    public String createToken(Long userId) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, new AuthSession(userId, Instant.now().plus(ttl)));
        return token;
    }

    public Optional<AuthSession> getSession(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        AuthSession session = sessions.get(token);
        if (session == null || session.expiresAt().isBefore(Instant.now())) {
            sessions.remove(token);
            return Optional.empty();
        }
        return Optional.of(session);
    }

    public void revoke(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    public record AuthSession(Long userId, Instant expiresAt) {
    }
}
