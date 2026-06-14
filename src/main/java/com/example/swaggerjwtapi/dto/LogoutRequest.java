package com.example.swaggerjwtapi.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank(message = "refresh token is required")
        String refreshToken
) {
}
