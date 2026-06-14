package com.example.swaggerjwtapi.service;

import com.example.swaggerjwtapi.dto.*;
import com.example.swaggerjwtapi.model.AppUser;
import com.example.swaggerjwtapi.model.RefreshTokenPayload;
import com.example.swaggerjwtapi.repository.RefreshTokenRepository;
import com.example.swaggerjwtapi.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public LoginResponse refresh(RefreshTokenRequest request) {
        RefreshTokenPayload payload =
                jwtService.extractRefreshToken(request.refreshToken());

        RefreshTokenSession session = refreshTokenRepository
                .consume(payload.tokenId())
                .orElseThrow(this::invalidRefreshToken);

        validateRefreshSession(payload, session);

        AppUser user = userRepository
                .findByUsername(payload.username())
                .orElseThrow(this::invalidRefreshToken);

        return issueTokenPair(user);
    }

    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        AppUser user = userRepository.findByUsername(request.username())
                .orElseThrow(this::invalidCredentials);

        if (!passwordEncoder.matches(request.password(), user.password())) {
            throw invalidCredentials();
        }

        return issueTokenPair(user);
    }

    public CurrentUserResponse getCurrentUser(String accessToken) {
        return jwtService.extractCurrentUser(accessToken);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found")
                );

        return User.withUsername(user.username())
                .password(user.password())
                .authorities(user.roles().toArray(String[]::new))
                .build();
    }

    private LoginResponse issueTokenPair(AppUser user) {
        String accessTokenId = UUID.randomUUID().toString();
        String refreshTokenId = UUID.randomUUID().toString();

        Instant now = Instant.now();
        Instant accessExpiresAt = now.plusMillis(jwtService.getAccessExpirationMs());
        Instant refreshExpiresAt = now.plusMillis(jwtService.getRefreshExpirationMs());

        String accessToken = jwtService.generateAccessToken(
                user,
                accessTokenId,
                accessExpiresAt
        );

        String refreshToken = jwtService.generateRefreshToken(
                user,
                refreshTokenId,
                refreshExpiresAt
        );

        refreshTokenRepository.save(
                new RefreshTokenSession(
                        refreshTokenId,
                        user.username(),
                        refreshExpiresAt
                )
        );

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessExpirationSeconds(),
                jwtService.getRefreshExpirationSeconds()
        );
    }

    private void validateRefreshSession(
            RefreshTokenPayload payload,
            RefreshTokenSession session
    ) {
        long sessionExpirationSeconds = session.expiresAt().getEpochSecond();
        long payloadExpirationSeconds = payload.expiresAt().getEpochSecond();


        boolean usernameMatches =
                session.username().equals(payload.username());

        boolean expirationMatches =
                sessionExpirationSeconds == payloadExpirationSeconds;

        boolean sessionExpired =
                session.expiresAt().isBefore(Instant.now());

        if (!usernameMatches || !expirationMatches || sessionExpired) {
            throw invalidRefreshToken();
        }
    }

    private BadCredentialsException invalidRefreshToken() {
        return new BadCredentialsException(
                "Username or password is invalid"
        );
    }

    private BadCredentialsException invalidCredentials() {
        return new BadCredentialsException(
                "Username or password is invalid"
        );
    }
}