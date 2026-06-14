package com.example.swaggerjwtapi.repository;

import com.example.swaggerjwtapi.dto.RefreshTokenSession;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class RefreshTokenRepository {

    private final ConcurrentHashMap<String, RefreshTokenSession> sessions =
            new ConcurrentHashMap<>();

    public void save(RefreshTokenSession session) {
        sessions.put(session.tokenId(), session);
    }

    public Optional<RefreshTokenSession> consume(String tokenId) {
        return Optional.ofNullable(sessions.remove(tokenId));
    }
}
