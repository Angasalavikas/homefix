# HomeFix — Setup Guide

## Prerequisites

| Tool       | Minimum Version | Check Command        |
|------------|-----------------|----------------------|
| Java JDK   | 25 (LTS)        | `java --version`     |
| Maven      | 3.9.x           | `mvn --version`      |
| Node.js    | 20.x+           | `node --version`     |
| npm        | 10.x+           | `npm --version`      |
| Git        | 2.x+            | `git --version`      |
| MySQL      | 8.x+            | `mysql --version`    |

> **Windows note:** all commands below use Bash syntax. If using Git Bash, they work as-is.

## Environment Variables (required)

Secrets are **not committed** to Git. Every business service reads its DB credentials from
environment variables, and all services share a single `JWT_SECRET`.

Copy the template and fill it in:

```bash
cp .env.example .env
```

Or export directly in your shell:

```bash
# MySQL credentials — used by every service that owns a database
export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password

# JWT signing key — ONE shared Base64-encoded 256-bit+ key for ALL services
# Generate a fresh one:  openssl rand -base64 64
export JWT_SECRET=$(openssl rand -base64 64)

# Razorpay payment keys (payment-service) — TEST keys from the Razorpay dashboard
# (https://dashboard.razorpay.com/app/keys, Test Mode). Never commit live keys.
export RAZORPAY_KEY_ID=rzp_test_xxxxxxxxxxxxxxxx
export RAZORPAY_KEY_SECRET=xxxxxxxxxxxxxxxx
```

### What happens if they're missing?

| Variable      | Missing behavior |
|---------------|------------------|
| `DB_PASSWORD` | Services default to an empty password (falls back to `${DB_PASSWORD:}`) → MySQL rejects connection at first query |
| `JWT_SECRET`  | `auth-service` fails to start (jjwt requires a non-empty key); all services reject tokens |
| `RAZORPAY_KEY_ID` / `RAZORPAY_KEY_SECRET` | `payment-service` starts, but `POST /api/payments/create-order` returns a clear "Razorpay is not configured" error |

> ⚠️ **All services must use the same `JWT_SECRET`.** If it changes, every previously issued
> token is invalidated and users must log in again.

## Tech Stack

- **Java 25** (LTS) — Runtime & compilation
- **Spring Boot 4.1.0** — Microservices framework
- **Spring Cloud 2025.1.2** (Oakwood) — Service discovery, config, gateway
- **MySQL 8.x** — Primary database per service
- **React 19 + TypeScript** — Frontend
- **Vite 8** — Frontend build tool
- **Tailwind CSS v4** — Utility-first styling

## Project Structure

```
homefix/
├── config-server/            # Centralized config (port 8888)
├── discovery-server/         # Eureka service registry (port 8761)
├── api-gateway/              # Spring Cloud Gateway (port 8080)
├── auth-service/             # Authentication & authorization (port 8081)
├── customer-service/         # Customer management (port 8082)
├── provider-service/         # Service provider management (port 8083)
├── service-catalog-service/  # Service catalog & categories (port 8084)
├── booking-service/          # Booking & scheduling (port 8085)
├── payment-service/          # Payment processing (port 8086)
├── notification-service/     # Notifications (port 8087)
├── admin-service/            # Admin dashboard & management (port 8088)
└── frontend/                 # React + TypeScript client app
```

## Getting Started (Development)

### 1. Database Setup

Create MySQL databases (note: payment service uses `homefix_payments` — plural):

```sql
CREATE DATABASE homefix_auth;
CREATE DATABASE homefix_customer;
CREATE DATABASE homefix_provider;
CREATE DATABASE homefix_service_catalog;
CREATE DATABASE homefix_booking;
CREATE DATABASE homefix_payments;
CREATE DATABASE homefix_notification;
```

### 2. Start Infrastructure Services (in order)

```bash
# Terminal 1 — Config Server
cd config-server && mvn spring-boot:run

# Terminal 2 — Discovery Server (after config-server is up)
cd discovery-server && mvn spring-boot:run

# Terminal 3 — API Gateway (after discovery-server is up; routes are dynamically resolved
# via Eureka, so start it after the business services below if you want it ready immediately)
cd api-gateway && mvn spring-boot:run
```

### 3. Start Business Services

Each in its own terminal (after discovery-server is up):

```bash
cd auth-service && mvn spring-boot:run
cd customer-service && mvn spring-boot:run
cd provider-service && mvn spring-boot:run
cd service-catalog-service && mvn spring-boot:run
cd booking-service && mvn spring-boot:run
cd payment-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
cd admin-service && mvn spring-boot:run
```

### 4. Start Frontend

```bash
cd frontend && npm install && npm run dev
```

The app will be available at **http://localhost:5173**.

## Seeding Admin & Catalog Data

- `service-catalog-service` seeds categories and services on first boot.
- `auth-service` seeds a default **ADMIN** account on first boot (`AdminSeeder`).
  Credentials come from environment variables (dev defaults shown):

  ```bash
  export ADMIN_EMAIL=admin@homefix.com
  export ADMIN_PASSWORD=Admin@1234
  ```

  > ⚠️ Change `ADMIN_PASSWORD` immediately in any real environment. Registration can **never**
  > self-assign the ADMIN role — the seeder is the supported bootstrap path.

## Payments (Razorpay)

Checkout flow: the frontend calls `POST /api/payments/create-order` (creates a Razorpay
Order + a `PENDING` payment record), opens Razorpay Checkout, then calls
`POST /api/payments/verify` with the returned `razorpay_order_id`,
`razorpay_payment_id`, and `razorpay_signature`. The backend verifies the HMAC
signature with `RAZORPAY_KEY_SECRET` **server-side** (never trusts the frontend's
success callback), marks the payment `SUCCESS`, fires a `PAYMENT_SUCCESS`
notification, and marks the booking `PAID` via booking-service.

In **Test Mode** use the Razorpay test cards (any future expiry / any CVV):

| Card / method            | Details                       |
|--------------------------|-------------------------------|
| Visa (success)           | `4111 1111 1111 1111`         |
| Visa (3DS flow)          | `4012 0010 3777 7777`         |
| Mastercard (success)     | `5105 1051 0510 5100`         |
| Mastercard (3DS flow)    | `2223 0000 1000 0016`         |
| UPI success              | `success@razorpay`            |
| UPI failure              | `failure@razorpay`            |
| OTP                     | any 4-digit (e.g. `1234`)     |

## Current-location on the address form

Addresses accept optional `latitude`/`longitude`. The frontend's location pin
button uses the browser Geolocation API and reverse-geocodes via the free
OpenStreetMap **Nominatim** endpoint (no API key; ~1 req/s usage policy — fine
for manual use). Geolocation requires a secure context: `http://localhost` works,
but a plain `http://` LAN IP does not.

## Building

```bash
# Build all Spring Boot services (from project root)
for dir in config-server discovery-server api-gateway auth-service customer-service provider-service service-catalog-service booking-service payment-service notification-service admin-service; do
  (cd "$dir" && mvn clean package -DskipTests)
done

# Run tests for a single service
cd booking-service && mvn test

# Build frontend
cd frontend && npm run build
```

## API Endpoints

All API requests go through the API Gateway at **http://localhost:8080**.

| Service               | Base Path              |
|-----------------------|------------------------|
| Auth Service          | `/api/auth/**`         |
| Customer Service      | `/api/customers/**`    |
| Provider Service      | `/api/providers/**`    |
| Service Catalog       | `/api/services/**`, `/api/categories/**` |
| Booking Service       | `/api/bookings/**`     |
| Payment Service       | `/api/payments/**`     |
| Notification Service  | `/api/notifications/**`|
| Admin Service         | `/api/admin/**`        |

Eureka Dashboard: **http://localhost:8761** — verify all services are registered.

See [README.md](README.md) for the complete endpoint reference and
[TESTING.md](TESTING.md) for the manual test checklist.
