# Backend SRS - La Vela Pool Booking

## 1. Document Info
- Product: La Vela Pool Booking
- Scope: Backend only
- Module path: backends/auth-service
- Version: 1.0.0
- Last updated: 2026-03-13
- Language/framework: Java 17, Spring Boot 3.2.x

## 2. Objectives and Principles
### Objectives
- Build a stable, maintainable backend for auth, booking, payment, and operations.
- Reuse existing Firebase auth capabilities (email/password + email verification, phone OTP).
- Keep server as single source of truth for booking/payment state.

### Engineering principles
- Prioritize stability and maintainability over premature optimization.
- Every technical decision must have business justification.
- Never trust client-side state for business-critical decisions.
- Payment is asynchronous; backend must verify provider callbacks/webhooks.
- Never log sensitive data (password, OTP, token, card data).

## 3. Scope
### In scope
- Auth and account APIs (Firebase integration and local user profile mapping).
- Pool and slot catalog APIs.
- Booking lifecycle with slot reservation and expiration TTL.
- Payment lifecycle (create payment, webhook handling, state update).
- Staff/admin operational APIs (check-in, booking management, user role management).
- Audit logging for inventory and payment/booking transitions.

### Out of scope (phase 1)
- Loyalty points, advanced voucher engine.
- Complex multi-tenant architecture.
- Non-essential analytics experiments.

## 4. Stakeholders and Roles
- Customer: register/login, browse pools/slots, create booking, pay, view bookings, check-in.
- Staff: manage pools/slots/pricing, booking operations, check-in, reconciliation/refund operations.
- Admin: user/role administration and account lock/unlock.
- DevOps: environment, monitoring, alerting, runtime operations.

## 5. Backend Architecture Requirements
- Layered architecture: Controller -> Service -> Domain -> Repository.
- Use DTOs for all API contracts.
- Enforce validation and business rules on server.
- Use transactions for inventory-sensitive flows.
- Keep idempotency for callback/check-in/payment update operations.

## 6. Functional Requirements

### 6.1 Auth and Account
- FR-1.1: Support email/password registration.
- FR-1.2: Send/require email verification after email registration.
- FR-1.3: Block booking features for unverified email accounts (except verify/resend flow).
- FR-1.4: Support email/password login.
- FR-1.5: Support phone OTP login/registration (Firebase Phone Auth).
- FR-1.6: Support logout with clean session semantics on backend side.
- FR-1.7: Support forgot password workflow (email reset trigger/integration).
- FR-1.8: User profile includes fullName/displayName, email, phone, verification status.
- FR-1.9: Map firebase_uid to local users table for authorization and profile.

### 6.2 Pools and Slots
- FR-2.1: GET pools list with name/address/images/description/open hours.
- FR-2.2: GET pool detail including policy/rules.
- FR-2.3: GET slot list by date with start/end/capacity_available/price.
- FR-2.4: Prevent booking for full/inactive slot.

### 6.3 Booking
- FR-3.1: Create booking with slotId and qty.
- FR-3.2: Initial booking status must be PENDING_PAYMENT.
- FR-3.3: Apply booking expiration TTL (example: 10 minutes).
- FR-3.4: On TTL expiration, set booking EXPIRED and release inventory.
- FR-3.5: Support my bookings list and booking detail endpoints.
- FR-3.6: Support cancel/reschedule by server-enforced policy.

### 6.4 Payment (asynchronous)
- FR-4.1: Create payment order/intent from backend.
- FR-4.2: Client must never mark payment success by itself.
- FR-4.3: Backend must validate webhook signature/providerTxnId.
- FR-4.4: On payment success, booking transitions to CONFIRMED.
- FR-4.5: On payment failed/canceled/expired, booking transitions per policy and release slot if required.
- FR-4.6: Refund is backend-driven; client is display-only.

### 6.5 Check-in
- FR-5.1: Generate booking_code and signed QR payload for confirmed booking.
- FR-5.2: Staff check-in API validates signed token/hash and booking state.
- FR-5.3: Check-in operation must be idempotent (no duplicate check-in).

### 6.6 Notifications (backend integration level)
- FR-6.1: Emit notification events for booking confirmed/canceled/refunded.
- FR-6.2: Support reminder notifications (phase-based rollout).

## 7. Non-Functional Requirements

### 7.1 Security
- NFR-SEC-1: HTTPS-only APIs.
- NFR-SEC-2: Short-lived access token model; refresh strategy if applicable.
- NFR-SEC-3: Role-based authorization (USER/STAFF/ADMIN).
- NFR-SEC-4: Do not log sensitive PII/secrets.
- NFR-SEC-5: Rate limit for auth/OTP endpoints.

### 7.2 Data consistency and reliability
- NFR-DATA-1: No overbooking under concurrency.
- NFR-DATA-2: Every inventory mutation must be audited.
- NFR-DATA-3: Booking/payment state transitions must be explicit and idempotent.

### 7.3 Performance
- NFR-PERF-1: List/slot APIs should meet practical latency target for 4G usage.
- NFR-PERF-2: Avoid premature optimization; optimize based on measured bottlenecks.

### 7.4 Operations
- NFR-OPS-1: Environment separation (dev/staging/prod).
- NFR-OPS-2: Monitor error rate, payment failures, booking failures.
- NFR-OPS-3: Structured logging with correlationId for critical flows.

## 8. Domain Model and Data Requirements

### 8.1 Core tables
- users: id, firebase_uid, full_name, email, phone, status, created_at, updated_at.
- pools: id, name, address, description, status, created_at, updated_at.
- slots: id, pool_id, start_time, end_time, capacity_total, capacity_available, price, status, created_at, updated_at.
- bookings: id, user_id, pool_id, slot_id, qty, amount, status, payment_status, expires_at, booking_code, qr_payload, created_at, updated_at.
- payments: id, booking_id, provider, provider_txn_id, amount, currency, status, raw_payload_ref, created_at, updated_at.
- inventory_logs: id, slot_id, booking_id (nullable), payment_id (nullable), delta, reason, actor, created_at.

### 8.2 Mandatory constraints
- Unique: users.firebase_uid, users.email, users.phone (nullable unique), bookings.booking_code, payments.provider_txn_id.
- Foreign keys between pool/slot/booking/payment/inventory_logs.
- booking_id in payments: unique for MVP one-booking-one-payment assumption.

### 8.3 Inventory and audit requirements
- Slot capacity must be updated inside transaction when reserve/release/confirm/cancel/expire occurs.
- Every capacity change must insert one inventory_logs record.
- reasons set: RESERVE, RELEASE, CONFIRM, CANCEL, EXPIRE.

## 9. State Machines

### 9.1 Booking.status
- PENDING_PAYMENT -> CONFIRMED -> CHECKED_IN -> COMPLETED
- Error branches from PENDING_PAYMENT: EXPIRED, CANCELED, FAILED

### 9.2 Payment.status
- CREATED -> PENDING -> SUCCESS
- Failure branches: FAILED, REFUNDED

## 10. Required API Surface (minimum)

### 10.1 Auth
- POST /api/auth/register
- GET /api/auth/me

### 10.2 Pools and slots
- GET /pools
- GET /pools/{id}
- GET /pools/{id}/slots?date=YYYY-MM-DD

### 10.3 Booking
- POST /bookings
- GET /bookings?me=true
- GET /bookings/{id}
- POST /bookings/{id}/cancel
- POST /bookings/{id}/checkin

### 10.4 Payment
- POST /payments
- POST /webhooks/payment/{provider}

### 10.5 Staff/admin (minimum)
- Staff list bookings by date/slot.
- Admin manage user roles and account status.

## 11. API Contract Rules
- Use standard API response wrapper:
  - success: boolean
  - data: object or null
  - error: string or null
- Input validation errors return 400.
- Unauthorized/forbidden follows 401/403.
- Business conflicts should use 409 where appropriate.
- Client must always read final state from server responses.

## 12. Module Ownership (as agreed)

### BE1 - User + Pool/Slot/Booking + Inventory (Owner: Khiet)
- Tables: users, pools, slots, bookings, inventory_logs.
- APIs:
  - Auth integration and profile mapping.
  - GET /pools, GET /pools/{id}, GET /pools/{id}/slots.
  - POST /bookings, GET /bookings, GET /bookings/{id}.
  - POST /bookings/{id}/checkin.
- Logic focus:
  - Anti-overbooking transaction lock.
  - Capacity update and inventory audit log.
  - Booking expiration TTL.

### BE2 - Payment + Staff/Admin operations (Owner: Tin)
- Table: payments.
- APIs:
  - POST /payments.
  - POST /webhooks/payment/{provider}.
  - Staff booking operation support by day/slot.
  - Refund/reconciliation basic workflow.
- Logic focus:
  - Payment state update -> booking state transition.
  - Idempotent webhook handling.
  - Cancel/refund effects on booking and inventory.

## 13. Acceptance Criteria (backend)
- Email registration and verification gating works end-to-end.
- Phone OTP auth flow integrates successfully.
- Booking creation fails when slot unavailable.
- No overbooking under concurrent requests.
- Payment success recognized only via server verification/webhook.
- Inventory and payment transitions are auditable.
- Check-in is server-verified and idempotent.

## 14. Risks and Mitigations
- Overbooking risk -> transactional lock + strict inventory logs + TTL expiration job.
- Payment mismatch -> webhook verification + idempotency key/providerTxnId uniqueness.
- OTP abuse -> rate limiting and abuse control.
- Maintainability regression -> strict layering, DTO contracts, integration tests.

## 15. Open Decisions
- Is phone mandatory for all customers?
- Source of truth for email: Firebase token email only, or request fallback?
- Exact booking TTL value by environment (example 10 minutes).
- Refund policy matrix by booking state/time window.
- Single payment per booking for all phases, or extend to retry/multi-attempt later?

## 16. Suggested Pre-Implementation Artifacts
- Use case diagram for Customer/Staff/Admin.
- Sequence diagram for booking + asynchronous payment + webhook.
- State diagram for Booking and Payment.
- ERD for users/pools/slots/bookings/payments/inventory_logs.

## 17. Change Log
- 2026-03-13: Replaced auth-only template with standardized backend SRS aligned to project-wide requirements and BE1/BE2 ownership.
