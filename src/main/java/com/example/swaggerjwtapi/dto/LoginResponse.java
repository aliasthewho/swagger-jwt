package com.example.swaggerjwtapi.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}