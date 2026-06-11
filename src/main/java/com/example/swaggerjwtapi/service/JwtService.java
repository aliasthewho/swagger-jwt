package com.example.swaggerjwtapi.service;

import com.example.swaggerjwtapi.dto.CurrentUserResponse;
import com.example.swaggerjwtapi.model.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    private  final SecretKey signingKey;
    private  final long expirationMs;
    private  final String issuer;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs,
            @Value("${app.jwt.issuer}") String issuer
    ) {
        this.signingKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
        this.expirationMs = expirationMs;
        this.issuer = issuer;
    }

    public String generateAccessToken(AppUser user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusMillis(expirationMs);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.username())
                .issuer(issuer)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claim("user_id", user.id())
                .claim("roles", user.roles())
                .claim("token_type", "access")
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public CurrentUserResponse extractCurrentUser(String token) {
        Claims claims = extractAllClaims(token);

        return new CurrentUserResponse(
                claims.get("user_id", Long.class),
                claims.getSubject(),
                extractRoles(claims),
                claims.get("token_type", String.class),
                claims.getIssuer(),
                claims.getId(),
                claims.getIssuedAt().toInstant(),
                claims.getExpiration().toInstant()
        );
    }

    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {
        Claims claims = extractAllClaims(token);

        return claims.getSubject().equals(userDetails.getUsername())
                && issuer.equals(claims.getIssuer())
                && "access".equals(
                        claims.get("token_type", String.class)
                )
                && claims.getExpiration().after(new Date());
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }

    private List<String> extractRoles(Claims claims) {
        Object rolesClaim = claims.get("roles");

        if (!(rolesClaim instanceof List<?> roles)) {
            return List.of();
        }

        return roles.stream()
                .map(String::valueOf)
                .toList();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }

    private <T> T extractClaim(
            String token,
            Function<Claims, T> resolver
    ) {
        return resolver.apply(extractAllClaims(token));
    }
}