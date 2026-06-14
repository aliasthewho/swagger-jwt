package com.example.swaggerjwtapi.controller;

import com.example.swaggerjwtapi.dto.CurrentUserResponse;
import com.example.swaggerjwtapi.dto.LoginRequest;
import com.example.swaggerjwtapi.dto.LoginResponse;
import com.example.swaggerjwtapi.dto.RefreshTokenRequest;
import com.example.swaggerjwtapi.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return authService.refresh(request);
    }

    @GetMapping("/me")
    public CurrentUserResponse currentUser(
            @RequestHeader(HttpHeaders.AUTHORIZATION)
            String authorizationHeader
    ) {
           String accessToken = authorizationHeader.substring(
                   BEARER_PREFIX.length()
           );
           return authService.getCurrentUser(accessToken);
    }
}