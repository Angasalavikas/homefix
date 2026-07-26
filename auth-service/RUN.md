# Auth Service — Run & Test Guide

## Prerequisites

1. **MySQL** — ensure MySQL 8+ is running with the `homefix_auth` database created:
   ```sql
   CREATE DATABASE IF NOT EXISTS homefix_auth;
   ```

2. **Java 25 + Maven** — verify:
   ```bash
   java --version    # Should be 25.x
   mvn --version     # Should be 3.9.x
   ```

---

## Start Services (in order)

### Terminal 1 — Config Server
```bash
cd config-server
mvn spring-boot:run
```
Wait until you see: `Started ConfigServerApplication in X.XX seconds`

### Terminal 2 — Discovery Server
```bash
cd discovery-server
mvn spring-boot:run
```
Wait until you see: `Started DiscoveryServerApplication in X.XX seconds`

### Terminal 3 — Auth Service
```bash
cd auth-service
mvn spring-boot:run
```
Wait until you see: `Started AuthServiceApplication in X.XX seconds`

The auth-service is now running on **http://localhost:8081**.

### (Optional) Terminal 4 — API Gateway
```bash
cd api-gateway
mvn spring-boot:run
```
Routes `/api/auth/**` → auth-service. Gateway runs on **http://localhost:8080**.

---

## Test End-to-End

### Option A: Using the .http file (VS Code REST Client)
Open `auth-service/src/test/rest/auth-service.http` and click **Send Request** above each request block.

1. **Register** → `POST /register` (returns JWT)
2. **Login** → `POST /login` (returns JWT)
3. **Profile** → `GET /profile` (requires Bearer token from Login)

### Option B: Using curl

```bash
# 1. Register a new user
curl -X POST http://localhost:8081/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "phone": "+12025551234",
    "password": "SecurePass1!",
    "role": "CUSTOMER"
  }'

# 2. Login
curl -X POST http://localhost:8081/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "SecurePass1!"
  }'

# Save the token from the response, then:
TOKEN="eyJhbGciOiJIUzI1NiJ9..."

# 3. Get Profile (authenticated)
curl http://localhost:8081/profile \
  -H "Authorization: Bearer $TOKEN"

# 4. Try admin endpoint (will fail for non-ADMIN users)
curl http://localhost:8081/admin/users \
  -H "Authorization: Bearer $TOKEN"
```

### Option C: Via API Gateway (port 8080)
Prefix all paths with `/api/auth`:
```bash
# Register via gateway
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"John Doe","email":"john.doe@example.com","phone":"+12025551234","password":"SecurePass1!","role":"CUSTOMER"}'

# Login via gateway
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john.doe@example.com","password":"SecurePass1!"}'
```

---

## Expected Test Flow

```
1. Register  → 201 Created  + JWT token + user info
2. Register again (same email) → 409 Conflict
3. Login     → 200 OK       + JWT token + user info
4. Login (wrong password) → 401 Unauthorized
5. Profile   → 200 OK       + user profile (no password)
6. Profile (no token) → 403 Forbidden
7. /admin/users (CUSTOMER token) → 403 Forbidden
```

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| `com.mysql.cj.jdbc.exceptions.CommunicationsException` | MySQL not running. Start MySQL service. |
| `java.lang.IllegalArgumentException: Unable to find config-server` | Config-server isn't up yet. Start it first. |
| `Whitelabel Error Page` on direct auth-service call | Make sure you're using port 8081, not 8080. |
