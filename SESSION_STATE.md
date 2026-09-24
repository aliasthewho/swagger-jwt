# Estado de Sesión - JWT Security Hardening

## ✅ Completado (Sesión 2)

### Rate Limiting
- Agregada dependencia Bucket4j en pom.xml
- Creado `LoginRateLimiter.java` (5 intentos en 15 minutos)
- Integrado en `AuthService.login()`
- Cambiado a `JwtException` para devolver HTTP 401 correctamente

### Tests de Integración
- Creado `AuthSecurityTest.java` con `@SpringBootTest`
- Pruebas de:
  - Login exitoso
  - Credenciales inválidas
  - Logout revoca refresh token
  - Acceso con token válido
  - Acceso rechazado sin token
- Todos los tests pasan ✓

### Conceptos Aprendidos
- JWT: Access Token (corta) + Refresh Token (larga)
- Revocación: logout invalida refresh token, access token sigue válido 1h
- HS256: Simétrico (vulnerable si clave se expone)
- RS256: Asimétrico (seguro para microservicios)
- Rate Limiting: Token Bucket algorithm (Bucket4j)

---

## ⏳ Pendiente (Sesión 3+)

### 2. HTTPS Enforcement
- [ ] Configurar `HttpsEnforcementConfig.java`
- [ ] Agregar header HSTS (max-age=31536000)
- [ ] Redirigir HTTP → HTTPS en producción

### 3. Headers de Seguridad
- [ ] X-Content-Type-Options: nosniff
- [ ] X-Frame-Options: DENY
- [ ] X-XSS-Protection
- [ ] Content-Security-Policy

### 4. CORS Configuration
- [ ] Restringir orígenes permitidos
- [ ] Configurar métodos y headers

### 5. Validación Estricta de Tokens
- [ ] Validar claims obligatorios
- [ ] Rechazar tokens sin firma
- [ ] Validar issuer explícitamente

### 6. Logging de Seguridad
- [ ] Auditar intentos fallidos de login
- [ ] Registrar logouts
- [ ] Registrar accesos denegados

---

## 📝 Notas Importantes

- **CLAUDE.md**: Guardado en `~/.claude/CLAUDE.md` con reglas duras (explicaciones obligatorias, validación paso a paso, etc)
- **Branch actual**: `test` (para testing y security hardening)
- **Main branch**: Tiene logout + revocación funcional
- **Context window**: En sesión 2 llegamos cerca del límite (~85%)

---

## 🎯 Próxima Sesión
Continuar con **HTTPS Enforcement** (punto 2 de Security Hardening).
