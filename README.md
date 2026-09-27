# swagger-jwt-api

A Spring Boot 3.5 / Java 21 REST API that demonstrates JWT authentication with rotating, single-use refresh tokens, login rate limiting, and Swagger/OpenAPI documentation.

This is a **learning project**: all storage is in-memory (`ConcurrentHashMap`-backed repositories) — there is no database.

## Features

- **JWT authentication** with separate access and refresh tokens (HS256), each carrying a `token_type` claim so one can't be used in place of the other.
- **Rotating, single-use refresh tokens** — every `/api/auth/refresh` call issues a new token pair and invalidates the old refresh token, so it can't be replayed.
- **Login rate limiting** (5 attempts / 15 minutes per username) via Bucket4j.
- **Role-based authorization** (`ROLE_ADMIN`, `ROLE_USER`) enforced with Spring Security and `@PreAuthorize` method security.
- **HTTPS enforcement** toggle (`security.require-https`) with HSTS support, configurable per profile.
- **Swagger / OpenAPI docs** via springdoc-openapi.
- A small **Product CRUD** demo API to showcase authenticated/role-protected endpoints.

## Requirements

- Java 21
- Maven (or use the bundled `./mvnw` wrapper — no local Maven install required)

## Getting Started

```bash
# Run the app (default profile, http://localhost:8080)
./mvnw spring-boot:run

# Run with a specific profile (e.g. dev)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Build (compile + test + package)
./mvnw clean package

# Run all tests
./mvnw test
```

Once running, the API is available at `http://localhost:8080`.

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI spec:** `http://localhost:8080/v3/api-docs`
- Custom static docs pages are also served from `src/main/resources/static/docs/`.

## Default Users

The in-memory `UserRepository` seeds two users on startup (passwords are BCrypt-hashed):

| Username | Password | Role        |
|----------|----------|-------------|
| `admin`  | `12345`  | `ROLE_ADMIN`|
| `user`   | `12345`  | `ROLE_USER` |

## API Overview

### Auth (`/api/auth`)

| Method | Path              | Description                                             |
|--------|-------------------|----------------------------------------------------------|
| POST   | `/api/auth/login`   | Authenticate with username/password, returns an access + refresh token pair. |
| POST   | `/api/auth/refresh`  | Exchange a valid refresh token for a new token pair (rotates the refresh token). |
| GET    | `/api/auth/me`       | Return the current user, resolved from the `Authorization: Bearer` access token. |
| POST   | `/api/auth/logout`   | Revoke a refresh token session (does not blacklist the access token). |

### Products (`/api/products`) — requires a valid access token

| Method | Path                  | Required role       | Description          |
|--------|-----------------------|----------------------|----------------------|
| GET    | `/api/products`         | `USER` or `ADMIN`     | List all products    |
| GET    | `/api/products/{id}`    | `USER` or `ADMIN`     | Get a product by id   |
| POST   | `/api/products`         | `ADMIN`               | Create a product      |
| PUT    | `/api/products/{id}`    | `ADMIN`               | Update a product      |
| DELETE | `/api/products/{id}`    | `ADMIN`               | Delete a product      |

## Configuration

Configuration lives under `src/main/resources/` and is split by Spring profile:

- `application.yml` — default/local profile, `require-https: false`.
- `application-dev.yml` — local dev, DEBUG logging.
- `application-test.yml` — used by most tests (`@ActiveProfiles("test")`).
- `application-prod-test.yml` — used by `HttpsEnforcementTest` (`require-https: true`).

Key properties:

| Property                          | Default                          | Description                                   |
|-----------------------------------|-----------------------------------|------------------------------------------------|
| `app.jwt.secret`                   | placeholder (override in prod)    | HMAC signing secret, override via `JWT_SECRET` env var. |
| `app.jwt.expiration-ms`            | `3600000` (1h)                    | Access token lifetime.                          |
| `app.jwt.refresh-expiration-ms`    | `604800000` (7d)                  | Refresh token lifetime.                         |
| `security.require-https`           | `false`                           | Redirects HTTP → HTTPS and enables HSTS when `true`. |

**Important:** always override `JWT_SECRET` with a real secret outside of local development.

## Architecture

See [CLAUDE.md](./CLAUDE.md) for a detailed breakdown of the request flow, token model, storage, error handling, and rate limiting design.

## Testing

```bash
# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=AuthSecurityTest

# Run a single test method
./mvnw test -Dtest=AuthSecurityTest#testLoggingSuccess

# Run a nested test class
./mvnw test -Dtest=HttpsEnforcementTest\$WithHttpsRequired
```

## Tech Stack

- Spring Boot 3.5.14 (Web, Security, Validation)
- Java 21
- [jjwt](https://github.com/jwtk/jjwt) 0.13.0 for JWT issuing/parsing
- [springdoc-openapi](https://springdoc.org/) 2.8.17 for Swagger/OpenAPI
- [Bucket4j](https://github.com/bucket4j/bucket4j) for rate limiting

## License

No license has been specified for this project.
