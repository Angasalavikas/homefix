# 🧪 HomeFix — Manual Test Checklist (Day 8)

Run this against the **frontend** at `http://localhost:5173` (API Gateway `:8080` must be up,
all services registered in Eureka at `:8761`). For each case, record ✔ passed / ✘ failed /
⚠ observed quirk, plus the browser console status.

> Setup before testing: create `admin@homefix.com` (role ADMIN), one CUSTOMER account, and
> one PROVIDER account. Seed categories/services are created automatically on catalog boot.

---

## 1. Auth

### Happy path
- [ ] **Register** — new CUSTOMER account with valid data → redirected home, logged in.
- [ ] **Register as PROVIDER** → redirected to provider dashboard.
- [ ] **Login** with the same credentials → lands on role-appropriate page.
- [ ] **Protected route with valid token** — `/bookings` loads booking history.

### Edge cases
- [ ] **Duplicate email** — register the same email twice → inline error, no account created.
- [ ] **Wrong password** — login with correct email + wrong password → 401 message, no redirect.
- [ ] **Invalid email format** — register with `not-an-email` → field-level error on Email.
- [ ] **Weak password** — password without uppercase/digit/special → field-level error.
- [ ] **Phone format** — `abc` → E.164 field-level error.
- [ ] **Expired token** — set `auth_token` to an expired JWT (see SETUP.md) → redirect to `/login`.
- [ ] **Tampered token** — flip a character in `auth_token` → rejected, redirect to `/login`.
- [ ] **No token** — clear `auth_token`, open `/bookings` → redirected to `/login` (ProtectedRoute).

## 2. Customer

### Happy path
- [ ] **Profile update** — change full name/phone on Profile page → "Profile saved ✓".
- [ ] **Add address** — add "Home" address → appears in the list, marked default if selected.
- [ ] **Edit address** — change street via modal → updated in place.
- [ ] **Delete address** — delete → removed from list.

### Edge cases
- [ ] **Default flag ordering** — mark a second address default → first address's default clears.
- [ ] **Empty address list** — new customer sees "No saved addresses" empty state.
- [ ] **Address list ordering** — default address shown first in the booking dropdown.
- [ ] **Validation** — submit empty address form → field errors, nothing saved.
- [ ] **Delete only address** — list becomes empty state again.

## 3. Provider

### Happy path
- [ ] **Register profile** — provider registers on Profile page → appears in "Register as provider"
      flow, status shown **PENDING**.
- [ ] **Update availability** — toggle AVAILABLE → BUSY → OFFLINE → reflected on provider
      dashboard header.
- [ ] **Admin approves** — admin verifies provider → status becomes **VERIFIED**.

### Edge cases
- [ ] **Duplicate registration** — try registering again → "already exists" error.
- [ ] **Experience range** — enter `-1` or `99` → field-level error (0–60).
- [ ] **Skills parsing** — `plumbing, faucet repair` → two skill chips.
- [ ] **Unverified provider in booking** — a PENDING provider is NOT listed in the booking dropdown
      (`/available` returns VERIFIED only).
- [ ] **Offline provider disabled** — OFFLINE/BUSY provider shows disabled option in dropdown.

## 4. Catalog (admin)

### Happy path
- [ ] **Create category** — new category appears in Home + Categories pages.
- [ ] **Create service** — new service appears under its category with price/duration.
- [ ] **Edit service** — change price → reflects on detail page.
- [ ] **Delete category/service** — disappears from listings.

### Edge cases
- [ ] **Non-admin write rejected** — CUSTOMER token hitting POST `/api/services` → 403.
- [ ] **Duplicate category name** → 400 error message.
- [ ] **Negative price** → validation error (`price >= 0`).
- [ ] **Search with no results** — search `zzzzz` → "No services found" empty state.
- [ ] **Missing category** — GET `/api/services/{unknown}` → 404 with error shape.

## 5. Booking

### Happy path
- [ ] **Create booking** — pick service, available provider, future date, saved/new address →
      "Booking confirmed" with PENDING status + confirmation notification.
- [ ] **List by role** — customer sees own bookings; provider dashboard sees same bookings as
      provider (own only).
- [ ] **Full status flow** — provider: Accept → On the way → Start work → Mark complete; status
      badge updates on both sides.

### Edge cases
- [ ] **Invalid transition rejected** — provider tries STARTED→ACCEPTED → 400, status unchanged.
- [ ] **Cancel allowed** — cancel from PENDING and from ACCEPTED → CANCELLED.
- [ ] **Cancel disallowed** — try cancel from STARTED/COMPLETED → 400, no change.
- [ ] **Nonexistent provider** — booking with `providerId=99999` → 404 via Feign upstream.
- [ ] **Nonexistent service** — `serviceId=99999` → 404.
- [ ] **Unavailable provider** — booking a BUSY/OFFLINE provider → 400 "not available".
- [ ] **Unverified provider** — booking a PENDING provider → 400 "not verified".
- [ ] **Past date** — pick a past datetime → field-level error (must be future).
- [ ] **Double-submit** — rapidly click "Confirm booking" → button disabled while in flight, one
      booking created.

## 6. Payment

### Happy path
- [ ] **Successful mock payment** — pay for a booking → SUCCESS panel with transaction ID.
- [ ] **Payment history** — paid bookings appear under history.
- [ ] **Invoice** — open invoice for a payment → contains service, provider, amount, status.

### Edge cases
- [ ] **Nonexistent booking** — POST payment with `bookingId=99999` → 404 "Booking not found".
- [ ] **Wrong owner** — pay for another customer's booking → 400 "does not belong".
- [ ] **Amount mismatch** — pay an amount ≠ booking price → decision: accept or reject (note which).
- [ ] **Negative/zero amount** → validation error.
- [ ] **Invoice for nonexistent payment** → 404.

## 7. Notifications

### Happy path
- [ ] **Booking event triggers notification** — create a booking → bell badge increments for
      customer.
- [ ] **Payment event triggers notification** — pay → "Payment successful" notification.
- [ ] **Status event** — provider accepts → customer gets "accepted" notification.
- [ ] **Mark as read** — click a notification → badge decrements, item loses highlight.

### Edge cases
- [ ] **Unread filter** — `GET /api/notifications?unread=true` returns only unread.
- [ ] **Empty state** — fresh account → "No notifications yet".
- [ ] **Cross-user isolation** — customer A cannot see/mark customer B's notifications.

## 8. Admin

### Happy path
- [ ] **Dashboard counts match reality** — customers/providers/revenue/bookings match the data
      you created in tests above.
- [ ] **Verify provider** — PENDING → VERIFIED from admin table; status updates in place.
- [ ] **Bookings table** — all bookings with correct status badges.

### Edge cases
- [ ] **Non-admin blocked** — CUSTOMER/PROVIDER visiting `/admin` → Unauthorized page; direct
      `GET /api/admin/**` with non-admin JWT → 403.
- [ ] **Provider count** — verify + unverified both counted in dashboard total.
- [ ] **Revenue** — equals sum of SUCCESS payments.

---

## 9. Frontend polish checklist (cross-cutting)

- [ ] **Loading spinners** visible on every page's first data fetch.
- [ ] **Empty states** — no bookings / no services in category / no notifications.
- [ ] **Error banners** (toast-style) shown on failed API calls (login, booking, payment, verify).
- [ ] **Submit buttons disabled while in flight** — booking creation, payment, login, register,
      save-profile, add-address, verify-provider.
- [ ] **Inline field errors** render under the offending input (no generic alert).

---

## Bug log

| # | Module | Scenario | Expected | Actual | Severity | Fixed? |
|---|--------|----------|----------|--------|----------|--------|
| 1 |        |          |          |        |          |        |
