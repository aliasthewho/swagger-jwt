package com.example.swaggerjwtapi.dto;

import java.time.Instant;

public record RefreshTokenSession(
        String tokenId,
        String username,
        Instant expiresAt
) {
}
