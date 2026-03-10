# Smart Tourism Backend Assessment (Spring Boot 3 + Java 17 + PostgreSQL)

Date: 2026-03-02
Scope: backend implementation audit for Auth, Users, Guide Verification, Places + cross-cutting security/integrity review.

---

## 1) Implementation Inventory

Legend: **Implemented** / **Partially** / **Missing**

### Module: Auth

**Entities**
- `auth.entity.GuideVerificationEntity` (`guide_verification` table) — **Implemented** (legacy model)

**Controllers**
- `AuthController`
  - `POST /api/v1/auth/register` — **Implemented**
  - `POST /api/v1/auth/login` — **Implemented**

**Service methods**
- `AuthService.register(...)` — **Implemented**
- `AuthService.login(...)` — **Implemented**
- `validateGuideVerificationInput(...)` — **Implemented**

**Repositories**
- `AuthGuideVerificationRepository` — **Implemented**

**DTOs**
- `RegisterRequestDto`, `LoginRequestDto`, `AuthTokenResponseDto`, `RegisterRole` — **Implemented**

**Validation**
- Registration: `@NotBlank`, `@Email`, `@Size`, `@NotNull role` — **Implemented**
- Guide-only registration docs check (`idProofUrl`, `selfieUrl`) — **Implemented**
- URL format/strength rules (password complexity, URL shape) — **Partially**

**Feature status**
- Registration/login with JWT issuance — **Implemented**
- Conflict-safe duplicate email handling (409) — **Implemented**
- Auth flow aligned to new guide verification domain (`guide_verifications`) — **Missing** (currently writes to legacy `guide_verification`)

---

### Module: Users

**Entities**
- `user.entity.UserEntity`, `UserRole` — **Implemented**

**Controllers**
- `UserController`
  - `GET /api/v1/users/{id}` — **Implemented**
  - `GET /api/v1/users/by-email` — **Implemented**

**Service methods**
- `getById(...)`, `getByEmail(...)`, `getAuthByEmail(...)`, `create(...)` — **Implemented**

**Repositories**
- `UserRepository` (`findByEmail`, `existsByEmail`) — **Implemented**

**DTOs**
- `CreateUserCommandDto`, `UserResponseDto`, `UserAuthDto` — **Implemented**

**Validation**
- Controller-level `@Email/@NotBlank` for `by-email` — **Implemented**
- Ownership/self-access constraints — **Missing**

**Feature status**
- Basic user lookup APIs — **Implemented**
- Secure user profile access policy (self-only except admin) — **Missing**

---

### Module: Guide Verification

**Entities**
- `guide.entity.GuideVerificationEntity` (`guide_verifications`) — **Implemented**
- `GuideVerificationAuditEntity` — **Implemented**
- Enums: `VerificationStatus`, `VerificationLevel`, `DocumentType`, `VerificationAuditAction` — **Implemented**

**Controllers**
- `GuideVerificationController`
  - `POST /api/v1/guides/verification/apply` — **Implemented**
  - `GET /api/v1/guides/verification/me` — **Implemented**
- `AdminVerificationController`
  - `GET /api/v1/admin/verifications` — **Implemented**
  - `PUT /api/v1/admin/verifications/{id}/approve` — **Implemented**
  - `PUT /api/v1/admin/verifications/{id}/reject` — **Implemented**

**Service methods**
- `apply(...)`, `getMy(...)`, `listByStatus(...)`, `approve(...)`, `reject(...)` — **Implemented**
- `assertGuideEligibleForBookingAcceptance(...)`, `getGuideProfile(...)` — **Implemented**

**Repositories**
- `GuideVerificationRepository`, `GuideVerificationAuditRepository` — **Implemented**

**DTOs**
- `ApplyGuideVerificationRequestDto`, `RejectGuideVerificationRequestDto`, `GuideVerificationResponseDto`, `GuideProfileResponseDto` — **Implemented**

**Validation**
- Request validation with `@Valid`, `@NotNull`, `@NotBlank`, `@Size` — **Implemented**
- Transition/state machine guards (e.g., reject approved, approve only pending) — **Missing**

**Feature status**
- Submission/review/audit persistence — **Implemented**
- Strict lifecycle integrity for status transitions — **Partially**
- Domain consistency with Auth registration flow — **Missing**

---

### Module: Places

**Entities**
- `place.entity.PlaceEntity` — **Implemented**

**Controllers**
- `GET /api/v1/places` — **Implemented**
- `GET /api/v1/places/{id}` — **Implemented**
- `POST /api/v1/admin/places` — **Implemented**
- `PUT /api/v1/admin/places/{id}` — **Implemented**
- `DELETE /api/v1/admin/places/{id}` — **Implemented**

**Service methods**
- `list(...)`, `getById(...)`, `create(...)`, `update(...)`, `delete(...)` — **Implemented**

**Repositories**
- `PlaceRepository` — **Implemented**

**DTOs**
- `CreatePlaceRequestDto`, `UpdatePlaceRequestDto`, `PlaceResponseDto` — **Implemented**

**Validation**
- `title` constraints for create/update — **Implemented**
- URL and text sanitization constraints for `mapUrl/description/address` — **Partially**
- Delete policy (soft delete/audit trail) — **Missing**

**Feature status**
- Full CRUD endpoints available — **Implemented**
- Business-grade deletion/audit strategy — **Missing**

---

## 2) Security Assessment (Strict)

## Security architecture review

- `SecurityFilterChain` is active, stateless, JWT filter inserted before `UsernamePasswordAuthenticationFilter` — **Implemented**
- Custom `401` and `403` JSON handlers (`RestAuthenticationEntryPoint`, `RestAccessDeniedHandler`) — **Implemented**
- Method security annotations (`@PreAuthorize`, etc.) — **Missing**
- Role mapping from JWT claim `role` to `ROLE_*` authority — **Implemented**

### Endpoint protection matrix

| Endpoint | Access | Required roles | Ownership rule | Status |
|---|---|---|---|---|
| `POST /api/v1/auth/register` | Public | None | N/A | Implemented |
| `POST /api/v1/auth/login` | Public | None | N/A | Implemented |
| `GET /api/v1/users/{id}` | Protected | ADMIN/GUIDE/TOURIST | **No self/admin check** (any authenticated user can fetch any id) | **Security Risk** |
| `GET /api/v1/users/by-email` | Protected | ADMIN/GUIDE/TOURIST | **No self/admin check** (any authenticated user can query any email) | **Security Risk** |
| `POST /api/v1/guides/verification/apply` | Protected | GUIDE | Current user only (derived from token subject) | Implemented |
| `GET /api/v1/guides/verification/me` | Protected | GUIDE | Current user only | Implemented |
| `GET /api/v1/admin/verifications` | Protected | ADMIN | Admin-only | Implemented |
| `PUT /api/v1/admin/verifications/{id}/approve` | Protected | ADMIN | Admin-only | Implemented |
| `PUT /api/v1/admin/verifications/{id}/reject` | Protected | ADMIN | Admin-only | Implemented |
| `GET /api/v1/places` | Protected | ADMIN/GUIDE/TOURIST | Any authenticated user | Implemented |
| `GET /api/v1/places/{id}` | Protected | ADMIN/GUIDE/TOURIST | Any authenticated user | Implemented |
| `POST /api/v1/admin/places` | Protected | ADMIN | Admin-only | Implemented |
| `PUT /api/v1/admin/places/{id}` | Protected | ADMIN | Admin-only | Implemented |
| `DELETE /api/v1/admin/places/{id}` | Protected | ADMIN | Admin-only | Implemented |

### Key security flaws identified

1. **User data overexposure / enumeration**
   - Any authenticated user can query arbitrary user by id/email.
   - No owner-or-admin policy.

2. **Dual guide verification domains create authorization blind spots**
   - Auth registration writes legacy table/entity (`guide_verification`).
   - Guide verification workflows and booking gate use new table/entity (`guide_verifications`).

3. **JWT trust model is minimal**
   - Request auth trusts token `role` claim without DB re-check of role/active status per request.
   - User deactivation after token issuance is not enforced until token expiry.

4. **JWT claims hardening gaps**
   - No issuer/audience validation strategy.
   - No token revocation/rotation mechanism.

5. **Filter skip logic uses deprecated matcher API**
   - `AntPathRequestMatcher` currently works, but is marked for removal and should be replaced to avoid future security regressions on framework upgrades.

---

## 3) Logic / Data Integrity Assessment

### Validation quality

- DTO `@Valid` usage is broadly present in controllers.
- Standard constraints (`@NotBlank`, `@Email`, `@Size`, `@Min`) are present on many key inputs.
- Gaps:
  - URL fields (`documentUrl`, `mapUrl`, auth doc URLs) are not format-validated.
  - No upper bound on pagination `size` (potential abuse/perf risk).

### Duplicate email handling

- `UserService.create(...)` checks `existsByEmail` and throws `CONFLICT (409)` via `AppException`.
- This is correct at service level.
- Remaining integrity risk: race window without explicit DB exception-to-409 mapping for unique constraint collisions under concurrent requests.

### Guide verification workflow rules

- Current states represented: `PENDING`, `APPROVED`, `REJECTED`.
- Level represented: `BASIC`, `ID_VERIFIED`, `ADDRESS_VERIFIED`, `FULLY_VERIFIED`.
- Gaps:
  - No strict transition guard (e.g., approve/reject should generally require current `PENDING`).
  - No guard preventing repeated approve/reject churn or reasonless invalid transitions.
  - Auth registration writes to legacy verification table, disconnected from workflow used by guide/admin APIs.

### Places CRUD integrity

- Role restriction for admin CRUD is enforced in `SecurityConfig`.
- Delete is currently hard delete (`placeRepository.delete(place)`) with no soft delete or audit metadata.
- Update allows partial updates but lacks normalization for non-title text fields.

---

## 4) Gap List (Top 10)

### A) Blocking Bugs

1. **Split guide verification persistence model (legacy vs new table/entity)**
   - High functional breakage risk; guide registration and admin/booking eligibility can diverge.

2. **Guide verification transition rules not enforced (approve/reject idempotency/state guards missing)**
   - Can produce inconsistent review history and invalid lifecycle states.

### B) Security Risks (Highest Priority)

3. **`GET /users/{id}` lacks owner/admin restriction**
   - Any authenticated user can access another user’s profile.

4. **`GET /users/by-email` enables user enumeration among authenticated users**
   - Sensitive discovery vector; should be admin-only or self-only.

5. **JWT authorization not revalidating user active/role state per request**
   - Deactivated users may keep access with valid token.

6. **No token revocation strategy (logout/compromise response)**
   - Increases impact window for stolen tokens.

### C) Logic/Data Integrity Issues

7. **No URL semantics validation for key fields (`mapUrl`, `documentUrl`, etc.)**
   - Increases bad data and potential abuse risk.

8. **Pagination size has no maximum cap**
   - Potential resource abuse and degraded performance.

9. **Places delete is hard delete without recoverability/audit**
   - Weak operational integrity for admin actions.

### D) Missing Core Product Modules

10. **Core modules missing/incomplete in API layer: booking APIs, OTP flow, chat, ratings**
   - Booking exists only as entity/repository/service fragment; no controller/API contract.
   - Chat/rating/recommendation/package modules have schema presence but no implementation.

---

## 5) Prioritized Next Implementation Plan

## Phase 1 — Stabilize + Security Fixes (Immediate)

1. Unify guide verification domain
   - Remove legacy `auth.entity.GuideVerificationEntity` usage in registration flow.
   - Create initial record in `guide_verifications` model (or remove auto-create at register and require apply endpoint only).

2. Enforce user endpoint ownership policies
   - `GET /users/{id}`: allow self or ADMIN only.
   - `GET /users/by-email`: ADMIN-only, or remove from public API and replace with `/me`.

3. Harden JWT authorization
   - Validate user active status from DB (cache if needed).
   - Add issuer/audience checks in JWT validation.

4. Guide lifecycle integrity
   - Allow approve/reject only from `PENDING`.
   - Record invalid transition attempts as `409` with clear error code.

5. Defensive request constraints
   - Add max page size (e.g., 100).
   - Add URL validation (`@Pattern` or URI validation) for URL fields.

## Phase 2 — Core Product (Booking)

1. Implement booking API surface
   - Create booking, list bookings by actor, get booking detail, accept/reject/cancel/complete transitions.

2. Enforce booking ownership and transition state machine
   - Tourist/Guide/Admin permissions per action.
   - Transition guards and optimistic locking/versioning.

3. Integrate verification gate properly
   - Guide acceptance must require approved verification level (`ID_VERIFIED` minimum).

## Phase 3 — OTP + Chat Gating

1. OTP lifecycle for trip start/end proof
   - Issue, verify, expiry, retry limits, audit.

2. Chat access controls
   - Booking-participant-only messaging.
   - Optional moderation/abuse controls and payload validation.

## Phase 4 — Audit + Rate Limiting + Observability

1. Audit trails
   - Admin actions (verification decisions, place deletes/updates, booking overrides).

2. Rate limiting and abuse protection
   - Login/register throttling, sensitive endpoint limits (`/auth/*`, `/users/by-email`, verification actions).

3. Observability baseline
   - Structured logs, security events, error metrics, endpoint latency, correlation IDs.

---

## 6) Concise Checklist

- [ ] Remove legacy `guide_verification` write path from auth registration
- [ ] Unify guide verification data model on `guide_verifications`
- [ ] Enforce self-or-admin rule on `GET /api/v1/users/{id}`
- [ ] Restrict or redesign `GET /api/v1/users/by-email`
- [ ] Add JWT issuer/audience validation
- [ ] Add active-user revalidation for authenticated requests
- [ ] Implement token revocation/rotation strategy
- [ ] Add strict verification state transition guards
- [ ] Add max pagination limits and URL validation constraints
- [ ] Implement booking controller/service workflow end-to-end
- [ ] Implement OTP flow for booking lifecycle gates
- [ ] Implement chat + rating modules with booking-based authorization
- [ ] Add admin audit logging and rate limiting
- [ ] Add security and integration tests for authorization matrix
