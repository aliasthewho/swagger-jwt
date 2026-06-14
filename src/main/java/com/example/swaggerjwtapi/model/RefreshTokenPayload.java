package com.example.swaggerjwtapi.model;

import java.time.Instant;

public record RefreshTokenPayload(
        String tokenId,
        String username,
        Instant expiresAt
) {
}
