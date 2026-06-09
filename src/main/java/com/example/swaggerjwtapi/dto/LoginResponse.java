package com.example.swaggerjwtapi.dto;

public record LoginResponse(
        String token,
        String type,
        long expiresIn
) {}