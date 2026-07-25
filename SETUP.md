# HomeFix — Setup Guide

## Prerequisites

Ensure the following tools are installed on your system:

| Tool       | Minimum Version | Check Command        |
|------------|-----------------|----------------------|
| Java JDK   | 25 (LTS)        | `java --version`     |
| Maven      | 3.9.x           | `mvn --version`      |
| Node.js    | 20.x+           | `node --version`     |
| npm        | 10.x+           | `npm --version`      |
| Git        | 2.x+            | `git --version`      |
| MySQL      | 8.x+            | `mysql --version`    |

## Version Checks

```bash
# Java
java --version
# Expected: java 25.x.x

# Maven
mvn --version
# Expected: Apache Maven 3.9.x

# Node.js
node --version
# Expected: v20.x.x or higher

# npm
npm --version
# Expected: 10.x.x or higher

# Git
git --version
# Expected: git 2.x.x
```

## Tech Stack

- **Java 25** (LTS) — Runtime & compilation
- **Spring Boot 4.1.0** — Microservices framework
- **Spring Cloud 2025.1.2** (Oakwood) — Service discovery, config, gateway
- **MySQL 8.x** — Primary database per service
- **React 19 + TypeScript** — Frontend
- **Vite** — Frontend build tool
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
├── notification-service/     # Email/SMS/push notifications (port 8087)
├── admin-service/            # Admin dashboard & management (port 8088)
└── frontend/                 # React + TypeScript client app
```

## Getting Started (Development)

### 1. Database Setup

Create MySQL databases for each service:

```sql
CREATE DATABASE homefix_auth;
CREATE DATABASE homefix_customer;
CREATE DATABASE homefix_provider;
CREATE DATABASE homefix_service_catalog;
CREATE DATABASE homefix_booking;
CREATE DATABASE homefix_payment;
CREATE DATABASE homefix_notification;
CREATE DATABASE homefix_admin;
```

### 2. Start Infrastructure Services

Start in this order:

```bash
# Terminal 1 — Config Server
cd config-server && mvn spring-boot:run

# Terminal 2 — Discovery Server (after config-server is up)
cd discovery-server && mvn spring-boot:run

# Terminal 3 — API Gateway (after discovery-server is up)
cd api-gateway && mvn spring-boot:run
```

### 3. Start Business Services

Each in its own terminal (order doesn't matter):

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

## Building

To build all services:

```bash
# Build all Spring Boot services (from project root)
for dir in config-server discovery-server api-gateway auth-service customer-service provider-service service-catalog-service booking-service payment-service notification-service admin-service; do
  (cd "$dir" && mvn clean package -DskipTests)
done

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
| Service Catalog       | `/api/services/**`     |
| Booking Service       | `/api/bookings/**`     |
| Payment Service       | `/api/payments/**`     |
| Notification Service  | `/api/notifications/**`|
| Admin Service         | `/api/admin/**`        |

Eureka Dashboard: **http://localhost:8761**
