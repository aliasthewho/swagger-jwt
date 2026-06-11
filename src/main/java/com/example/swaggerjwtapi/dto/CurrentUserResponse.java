package com.example.swaggerjwtapi.dto;

import java.time.Instant;
import java.util.List;

public record CurrentUserResponse(
        Long userId,
        String username,
        List<String> roles,
        String tokenType,
        String issuer,
        String tokenId,
        Instant issuedAt,
        Instant expiresAt
) {
}
