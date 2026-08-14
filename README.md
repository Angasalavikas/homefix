# 🔧 HomeFix — Home Services Platform

A full-stack, microservices-based home services marketplace where customers book verified
professionals (plumbers, electricians, cleaners, painters, AC technicians, carpenters) and
providers manage their availability and bookings — built over an 8-day sprint.

**Stack:** Spring Boot 4 (Java 25) · Spring Cloud · MySQL · React 19 · TypeScript · Tailwind CSS 4

---

## 1. Project Overview

HomeFix connects **Customers** with verified **Providers** through a service catalog and a
booking + mock-payment pipeline. It is built as **11 cooperating Spring Boot services**
behind a Spring Cloud Gateway, with a single-page React frontend.

### Key flows

1. **Auth** — register/login with JWT; role-based access (`CUSTOMER`, `PROVIDER`, `ADMIN`).
2. **Catalog** — admins manage categories & services; customers search/browse.
3. **Provider onboarding** — providers register a profile (starts `PENDING`), admins verify it
   (`VERIFIED`), and only verified + available providers appear in booking results.
4. **Booking** — customers pick a service/provider/date/address; providers advance status
   (`PENDING → ACCEPTED → ON_THE_WAY → STARTED → COMPLETED`); customers can cancel from
   `PENDING`/`ACCEPTED`.
5. **Payment** — Razorpay-powered checkout (orders + server-side signature verification), invoices, and revenue aggregation.
6. **Notifications** — created automatically on booking/payment events via Feign; the frontend
   bell shows unread counts and supports mark-as-read.
7. **Admin dashboard** — live counts aggregated across services via Feign.

### Error handling convention

Every service returns the same error shape from a `@RestControllerAdvice`:

```json
{
  "timestamp": "2026-08-04T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Referenced resource not found in the system",
  "path": "/api/bookings/999/cancel"
}
```

Validation failures return `400` with an additional `details` map of field-level messages.
The generic `500` fallback never leaks exception messages or stack traces.

---

## 2. Architecture

```
                         ┌──────────────────────────┐
                         │  React Frontend (:5173)   │
                         └────────────┬─────────────┘
                                      │  /api/**  (Vite proxy)
                         ┌────────────▼─────────────┐
                         │  API Gateway (:8080)      │
                         └────────────┬─────────────┘
                                      │  routed by service name
        ┌───────────────┬─────────────┼──────────────┬────────────────┐
        │               │             │              │                │
   ┌────▼────┐   ┌──────▼───┐   ┌────▼─────┐   ┌─────▼─────┐   ┌──────▼────┐
   │ auth    │   │ customer │   │ provider │   │ catalog   │   │ booking   │
   │ (:8081) │   │ (:8082)  │   │ (:8083)  │   │ (:8084)   │   │ (:8085)   │
   └────┬────┘   └──────┬───┘   └────┬─────┘   └─────┬─────┘   └──────┬────┘
        │               │            │               │                │
   ┌────▼────┐   ┌──────▼───┐   ┌────▼─────┐   ┌─────▼─────┐          │
   │ payment │   │ notif    │   │ admin     │   │           │          │
   │ (:8086) │   │ (:8087)  │   │ (:8088)   │   │           │          │
   └─────────┘   └──────────┘   └───────────┘   │           │          │
                                                │           │          │
   All services register with ──► Eureka Discovery Server (:8761)
   All services pull config ────► Config Server (:8888, native profile)
   Services call each other ─────► OpenFeign (via Eureka service names)
```

| Service              | Port | Responsibilities |
|----------------------|------|------------------|
| **config-server**    | 8888 | Centralized YAML config (native profile, `classpath:/config/`) |
| **discovery-server** | 8761 | Eureka registry — all services self-register |
| **api-gateway**      | 8080 | Spring Cloud Gateway — single entry point, routes `/api/**` |
| **auth-service**     | 8081 | Register/login, JWT issue & validation, user profile |
| **customer-service** | 8082 | Customer profiles & addresses |
| **provider-service** | 8083 | Provider profiles, skills, availability, verification |
| **service-catalog-service** | 8084 | Categories & service items, search |
| **booking-service**  | 8085 | Booking lifecycle & status transition validation |
| **payment-service**  | 8086 | Razorpay payments, history, invoices, revenue |
| **notification-service** | 8087 | Notifications (internal creation + user-facing queries) |
| **admin-service**    | 8088 | Admin dashboard aggregation & provider verification |
| **frontend**         | 5173 | React SPA (Vite dev server proxies `/api` → `:8080`) |

**How they connect:** every business service imports config from **Config Server**,
registers with **Eureka**, and talks to sibling services through **OpenFeign clients**
resolved by Eureka service name (e.g. `booking-service` → `customer-service`,
`provider-service`, `service-catalog-service`, `notification-service`).

---

## 3. Installation

### Prerequisites

| Tool     | Minimum    | Check              |
|----------|------------|--------------------|
| Java JDK | 25 (LTS)   | `java --version`   |
| Maven    | 3.9.x      | `mvn --version`    |
| Node.js  | 20.x+      | `node --version`   |
| npm      | 10.x+      | `npm --version`    |
| MySQL    | 8.x        | `mysql --version`  |

### Environment variables

Secrets are **not** committed to the repo. Set them before starting any service
(see [SETUP.md](SETUP.md) for details):

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password
export JWT_SECRET=$(openssl rand -base64 64)   # one shared key for ALL services
```

> ⚠️ All services share **one** `JWT_SECRET`. If you change it, previously issued tokens
> are invalidated. Use the same value on every service.

### Database setup

Create one MySQL database per service:

```sql
CREATE DATABASE homefix_auth;
CREATE DATABASE homefix_customer;
CREATE DATABASE homefix_provider;
CREATE DATABASE homefix_service_catalog;
CREATE DATABASE homefix_booking;
CREATE DATABASE homefix_payments;
CREATE DATABASE homefix_notification;
```

### Startup order (exact)

Open one terminal per step and wait for the previous service to be up:

```bash
# 1. Config Server
cd config-server && mvn spring-boot:run

# 2. Discovery Server (Eureka) — after config-server is up
cd discovery-server && mvn spring-boot:run

# 3. Business services — after discovery-server is up
cd auth-service && mvn spring-boot:run
cd customer-service && mvn spring-boot:run
cd provider-service && mvn spring-boot:run
cd service-catalog-service && mvn spring-boot:run
cd booking-service && mvn spring-boot:run
cd payment-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
cd admin-service && mvn spring-boot:run

# 4. API Gateway — after business services are registered in Eureka
cd api-gateway && mvn spring-boot:run

# 5. Frontend
cd frontend && npm install && npm run dev
```

Open **http://localhost:5173** (frontend) and **http://localhost:8761** (Eureka dashboard).

> **Seed data:** `service-catalog-service` auto-seeds categories and services on first boot
> (`DataSeeder`). `auth-service` seeds a default **ADMIN** account on first boot
> (`AdminSeeder`, credentials from `ADMIN_EMAIL`/`ADMIN_PASSWORD` env vars — see SETUP.md).
> Registration can never self-assign the ADMIN role.

---

## 4. Technologies Used

| Layer        | Technology                              | Version |
|--------------|-----------------------------------------|---------|
| Language     | Java (JDK LTS)                          | 25      |
| Framework    | Spring Boot                              | 4.1.0   |
| Cloud        | Spring Cloud                             | 2025.1.2 (Oakwood) |
| Gateway      | Spring Cloud Gateway (api-gateway)       | —       |
| Discovery    | Spring Cloud Netflix Eureka Server       | —       |
| Config       | Spring Cloud Config Server (native)      | —       |
| Inter-service| OpenFeign                                | —       |
| Security     | Spring Security + JJWT (io.jsonwebtoken) | —       |
| Persistence  | Spring Data JPA + Hibernate              | —       |
| Database     | MySQL (dev) / H2 (tests)                 | 8.x     |
| Validation   | Jakarta Bean Validation (Hibernate Validator) | — |
| API Docs     | springdoc-openapi                        | —       |
| Frontend     | React + TypeScript + Vite                | React 19, Vite 8, TS 6 |
| Styling      | Tailwind CSS                             | v4      |
| HTTP client  | Axios                                    | ^1.18   |
| Build        | Maven / npm                              | 3.9.x / 10.x+ |

---

## 5. API Endpoint Reference

All public endpoints are reached through the **API Gateway at `http://localhost:8080`**.
Internal endpoints (`/internal/**`) are called service-to-service over Feign and are not
exposed through the gateway. `🔒` = requires JWT, `🔐` = admin only.

### Auth Service — `/api/auth/**`

| Method | Path                       | Auth | Description |
|--------|----------------------------|------|-------------|
| POST   | `/api/auth/register`       | —    | Register (CUSTOMER/PROVIDER), creates customer profile via Feign, returns JWT |
| POST   | `/api/auth/login`          | —    | Login, returns JWT |
| GET    | `/api/auth/profile`        | 🔒   | Current user profile |
| GET    | `/api/auth/admin/users`    | 🔐   | List all users |
| GET    | `/api/auth/health`         | —    | Health check |

### Customer Service — `/api/customers/**`

| Method | Path                                  | Auth | Description |
|--------|---------------------------------------|------|-------------|
| GET    | `/api/customers/me`                   | 🔒   | My customer profile |
| PUT    | `/api/customers/me`                   | 🔒   | Update profile |
| GET    | `/api/customers/me/addresses`         | 🔒   | List my addresses (default first) |
| POST   | `/api/customers/me/addresses`         | 🔒   | Add address |
| PUT    | `/api/customers/me/addresses/{id}`    | 🔒   | Update address |
| DELETE | `/api/customers/me/addresses/{id}`    | 🔒   | Delete address |
| GET    | `/internal/users/{userId}`            | —    | Internal: profile by userId |
| POST   | `/internal/customers`                 | —    | Internal: create customer (from auth) |
| GET    | `/internal/customers`                 | —    | Internal: list all customers (admin) |
| GET    | `/internal/customers/count`           | —    | Internal: customer count (admin) |

### Provider Service — `/api/providers/**`

| Method | Path                            | Auth | Description |
|--------|---------------------------------|------|-------------|
| GET    | `/api/providers/available`      | —    | Verified providers (booking dropdown) |
| POST   | `/api/providers/profile`        | 🔒   | Register provider profile (status `PENDING`) |
| GET    | `/api/providers/me`             | 🔒   | My provider profile (404 if not registered) |
| PUT    | `/api/providers/me/availability`| 🔒   | Update availability (AVAILABLE/BUSY/OFFLINE) |
| GET    | `/api/providers/profile/{id}`   | —    | Public provider profile |
| GET    | `/internal/{id}`                | —    | Internal: provider by DB id |
| GET    | `/internal/by-user/{userId}`    | —    | Internal: provider by auth userId |
| GET    | `/internal/providers`           | —    | Internal: all providers (admin) |
| GET    | `/internal/providers/count`     | —    | Internal: provider count (admin) |
| PUT    | `/internal/providers/{id}/verify`| —   | Internal: verify provider (admin) |

### Service Catalog — `/api/services/**`, `/api/categories/**`

| Method | Path                          | Auth | Description |
|--------|-------------------------------|------|-------------|
| GET    | `/api/categories`             | —    | All categories |
| GET    | `/api/categories/{id}`        | —    | Category by id |
| POST   | `/api/categories`             | 🔐   | Create category |
| PUT    | `/api/categories/{id}`        | 🔐   | Update category |
| DELETE | `/api/categories/{id}`        | 🔐   | Delete category |
| GET    | `/api/services/search`        | —    | Search services (`categoryId`, `keyword`) |
| GET    | `/api/services`               | —    | All services |
| GET    | `/api/services/{id}`          | —    | Service by id |
| POST   | `/api/services`               | 🔐   | Create service |
| PUT    | `/api/services/{id}`          | 🔐   | Update service |
| DELETE | `/api/services/{id}`          | 🔐   | Delete service |
| GET    | `/internal/services/{id}`     | —    | Internal: service by id |

### Booking Service — `/api/bookings/**`

| Method | Path                        | Auth | Description |
|--------|-----------------------------|------|-------------|
| POST   | `/api/bookings`             | 🔒   | Create booking (validates customer/provider/service via Feign) |
| GET    | `/api/bookings`             | 🔒   | List by `role=customer\|provider`, optional `status` filter |
| PUT    | `/api/bookings/{id}/status` | 🔒   | Provider/Admin status transition (validated state machine) |
| PUT    | `/api/bookings/{id}/cancel` | 🔒   | Customer cancels (only PENDING/ACCEPTED) |
| GET    | `/internal/bookings`        | —    | Internal: all bookings (admin) |
| GET    | `/internal/bookings/{id}`   | —    | Internal: booking by id |
| GET    | `/internal/bookings/stats`  | —    | Internal: counts by status (admin) |

**Valid transitions:** `PENDING→ACCEPTED|CANCELLED`, `ACCEPTED→ON_THE_WAY|CANCELLED`,
`ON_THE_WAY→STARTED`, `STARTED→COMPLETED`; `COMPLETED`/`CANCELLED` are terminal.

### Payment Service — `/api/payments/**`

| Method | Path                        | Auth | Description |
|--------|-----------------------------|------|-------------|
| POST   | `/api/payments`             | 🔒   | Process (mock) payment for a booking |
| GET    | `/api/payments/history`     | 🔒   | My payment history |
| GET    | `/api/payments/{id}/invoice`| 🔒   | Invoice for a payment |
| GET    | `/internal/payments/revenue`| —    | Internal: total revenue (admin) |

### Notification Service — `/api/notifications/**`

| Method | Path                            | Auth | Description |
|--------|---------------------------------|------|-------------|
| GET    | `/api/notifications`            | 🔒   | My notifications (`?unread=true` filter) |
| PUT    | `/api/notifications/{id}/read`  | 🔒   | Mark one as read |
| POST   | `/internal/notifications`       | —    | Internal: create notification (from booking/payment) |
| GET    | `/api/notifications/health`     | —    | Health check |

### Admin Service — `/api/admin/**`

| Method | Path                              | Auth | Description |
|--------|-----------------------------------|------|-------------|
| GET    | `/api/admin/dashboard`            | 🔐   | Aggregated counts (customers, providers, bookings by status, revenue) |
| GET    | `/api/admin/customers`            | 🔐   | All customers |
| GET    | `/api/admin/providers`            | 🔐   | All providers |
| GET    | `/api/admin/bookings`             | 🔐   | All bookings |
| PUT    | `/api/admin/providers/{id}/verify`| 🔐   | Verify a provider |
| GET    | `/api/admin/health`               | —    | Health check |

---

## 6. Folder Structure

```
homefix/
├── config-server/            # Central config (port 8888)
├── discovery-server/         # Eureka registry (port 8761)
├── api-gateway/              # Spring Cloud Gateway (port 8080)
├── auth-service/             # JWT auth (port 8081)
├── customer-service/         # Customer profiles & addresses (port 8082)
├── provider-service/         # Provider profiles & verification (port 8083)
├── service-catalog-service/  # Categories & services (port 8084)
├── booking-service/          # Booking lifecycle (port 8085)
├── payment-service/          # Razorpay payments & invoices (port 8086)
├── notification-service/     # Notifications (port 8087)
├── admin-service/            # Admin aggregation (port 8088)
├── frontend/
│   ├── src/
│   │   ├── components/       # Button, Card, Modal, StatusBadge, EmptyState, …
│   │   ├── context/          # AuthContext (JWT state)
│   │   ├── pages/            # Home, Categories, Booking, Profile, Admin, …
│   │   ├── services/         # Axios API clients (auth, catalog, booking, …)
│   │   ├── types/            # Shared TypeScript interfaces
│   │   └── utils/            # format.ts helpers
│   └── package.json
├── SETUP.md                  # Detailed setup & env vars
├── TESTING.md                # Manual test checklist
└── .env.example              # Template for required env vars
```

---

## 7. Future Enhancements

- **Real payment gateway integration** — replace the mock in `PaymentService.simulatePaymentGateway`
  with Stripe/Razorpay and webhook verification.
- **WebSocket-based live notifications** — push booking/payment events to the browser in real time.
- **Provider ratings & reviews** — post-completion feedback and average ratings.
- **Geolocation-based provider matching** — distance-aware provider ranking.
- **Refresh tokens** — short-lived access + rotating refresh token flow.
- **Rate limiting on the gateway** — Redis-based `RequestRateLimiter` filter.
- **Distributed tracing** — Micrometer + Zipkin across services.
- **Containerization** — Docker Compose for MySQL + all services.

---

## License

See [LICENSE](LICENSE).
