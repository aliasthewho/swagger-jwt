package com.example.swaggerjwtapi.service;

import com.example.swaggerjwtapi.dto.CurrentUserResponse;
import com.example.swaggerjwtapi.model.AppUser;
import com.example.swaggerjwtapi.model.RefreshTokenPayload;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
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
import java.util.function.Function;

@Service
public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final SecretKey signingKey;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;
    private final String issuer;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}")
            long accessExpirationMs,
            @Value("${app.jwt.refresh-expiration-ms}")
            long refreshExpirationMs,
            @Value("${app.jwt.issuer}") String issuer
    ) {
        this.signingKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
        this.issuer = issuer;
    }

    public String generateAccessToken(AppUser user, String tokenId, Instant expiresAt) {
        Instant issuedAt = Instant.now();

        return Jwts.builder()
                .id(tokenId)
                .subject(user.username())
                .issuer(issuer)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claim("user_id", user.id())
                .claim("roles", user.roles())
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(AppUser user, String tokenId, Instant expiresAt) {
        Instant issuedAt = Instant.now();

        return Jwts.builder()
                .id(tokenId)
                .subject(user.username())
                .issuer(issuer)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public RefreshTokenPayload extractRefreshToken(String token) {
        Claims claims = extractAllClaims(token);
        String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);

        if (!REFRESH_TOKEN_TYPE.equals(tokenType)) {
            throw new JwtException("Token is not a refresh token");
        }

        return new RefreshTokenPayload(
                claims.getId(),
                claims.getSubject(),
                claims.getExpiration().toInstant()
        );
    }

    public CurrentUserResponse extractCurrentUser(String token) {
        Claims claims = extractAllClaims(token);

        return new CurrentUserResponse(
                claims.get("user_id", Long.class),
                claims.getSubject(),
                extractRoles(claims),
                claims.get(TOKEN_TYPE_CLAIM, String.class),
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

    public long getAccessExpirationMs() {
        return accessExpirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    public long getAccessExpirationSeconds() {
        return accessExpirationMs / 1000;
    }

    public long getRefreshExpirationSeconds() {
        return refreshExpirationMs / 1000;
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