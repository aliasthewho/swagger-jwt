# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Spring Boot 3.5.14 / Java 21 REST API demonstrating JWT authentication with refresh-token rotation, rate limiting, and Swagger/OpenAPI docs. This is a learning project (see user's global CLAUDE.md rules on explanation-before-code) — storage is in-memory only, there is no database.

## Commands

```bash
# Run the app (default profile, http://localhost:8080)
./mvnw spring-boot:run

# Run with a specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Build (compile + test + package)
./mvnw clean package

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=AuthSecurityTest

# Run a single test method
./mvnw test -Dtest=AuthSecurityTest#testLoggingSuccess

# Run a nested test class (HttpsEnforcementTest uses @Nested inner classes)
./mvnw test -Dtest=HttpsEnforcementTest\$WithHttpsRequired
```

Swagger UI: `http://localhost:8080/swagger-ui.html` — OpenAPI spec: `/v3/api-docs`. Custom static docs pages live at `src/main/resources/static/docs/`.

## Architecture

### Request flow
`JwtAuthenticationFilter` (a `OncePerRequestFilter` registered before `UsernamePasswordAuthenticationFilter` in `SecurityConfig`) reads the `Authorization: Bearer` header, validates the token via `JwtService`, loads the user via `AuthService.loadUserByUsername` (which implements Spring's `UserDetailsService`), and populates `SecurityContextHolder`. Endpoint-level authorization is enforced both in `SecurityConfig.securityFilterChain` (path matchers) and via `@PreAuthorize` method security on `ProductController` (`@EnableMethodSecurity`).

### Token model
`JwtService` issues two HS256 JWTs per login, both carrying a `token_type` claim (`access` or `refresh`) so one can't be used as the other:
- **Access token** — short-lived (`app.jwt.expiration-ms`, default 1h), carries `user_id` and `roles`, validated by `JwtAuthenticationFilter` on every request.
- **Refresh token** — long-lived (`app.jwt.refresh-expiration-ms`, default 7d), only accepted by `/api/auth/refresh` and `/api/auth/logout`.

`AuthService.issueTokenPair` mints both tokens and stores a `RefreshTokenSession` in `RefreshTokenRepository` (in-memory `ConcurrentHashMap`, keyed by the refresh token's JWT ID). Refresh is **rotating and single-use**: `RefreshTokenRepository.consume(tokenId)` removes the session on read, so a refresh token cannot be replayed — a second `/api/auth/refresh` call with the same token fails because the session is gone (`isRevoked` just checks absence from the map). `AuthService.validateRefreshSession` additionally cross-checks the token's own claims (username, expiry) against the stored session before trusting it. `logout` revokes by deleting the session for the refresh token's ID; it does not blacklist the still-valid access token.

### Storage
`UserRepository` and `RefreshTokenRepository` are in-memory (`ConcurrentHashMap`), not JPA — there is no database in this project. `UserRepository` seeds two hardcoded users on construction: `admin`/`12345` (`ROLE_ADMIN`) and `user`/`12345` (`ROLE_USER`), passwords BCrypt-hashed at startup. `ProductRepository` backs the CRUD demo (`ProductController`/`ProductService`) the same way.

### Error handling
`GlobalExceptionHandler` (`@RestControllerAdvice`) maps `JwtException` (thrown for malformed/invalid/revoked tokens, rate-limit exceeded) to `401`. Invalid login credentials and invalid refresh sessions in `AuthService` throw Spring Security's `BadCredentialsException` instead, which is handled by Spring Security's own filter chain (not `GlobalExceptionHandler`) and currently surfaces as `403`.

### Rate limiting
`LoginRateLimiter` uses Bucket4j (token-bucket, 5 attempts / 15 min) keyed per-username in an in-memory map; `AuthService.login` checks it before verifying credentials and throws `JwtException` when exceeded.

### Configuration profiles
`security.require-https` (bound in `SecurityConfig.securityFilterChain`) toggles `http.requiresChannel(...).requiresSecure()`, which redirects HTTP→HTTPS. The `Strict-Transport-Security` header is configured unconditionally but is only written by Spring Security on requests it sees as secure (`request.isSecure()`) — it is absent on plain HTTP responses regardless of `require-https` (verified: `MockMvc.perform(post(...))` without `.secure(true)` returns no `Strict-Transport-Security` header). Profiles under `src/main/resources/`:
- `application.yml` — default/local, `require-https: false`.
- `application-dev.yml` — local dev, DEBUG logging for `com.example.swaggerjwtapi`.
- `application-test.yml` — used by `@ActiveProfiles("test")` in most tests; `require-https: false`, fixed JWT secret (no env var) for reproducibility.
- `application-prod-test.yml` — used by `@ActiveProfiles("prod-test")` in `HttpsEnforcementTest`; `require-https: true`, exercises the HTTPS-redirect path.

`app.jwt.secret` defaults to a placeholder via `${JWT_SECRET:...}` in `application.yml`/`application-dev.yml` — override with the `JWT_SECRET` env var outside local dev.
