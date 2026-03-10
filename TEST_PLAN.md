# Smart Tourism Backend E2E Verification Plan (curl/Postman)

Date: 2026-03-02  
Target: Spring Boot 3 backend (`http://localhost:8080`)

## 0) Preconditions

- Backend is running.
- Database is available and migrated.
- Swagger endpoints are public:
  - `/v3/api-docs`
  - `/swagger-ui/index.html`
- Tooling:
  - `curl`
  - optional: `jq` for JSON token extraction

Set base URL:

```bash
BASE_URL=http://localhost:8080
```

---

## 1) Create 3 Accounts + Obtain JWT Tokens

## 1.1 Create TOURIST account

```bash
curl -i -X POST "$BASE_URL/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Tourist",
    "lastName": "One",
    "email": "tourist1@example.com",
    "password": "Tourist@123",
    "role": "TOURIST"
  }'
```

Expected: `201 Created`  
Response contains token at `data.token`.

## 1.2 Create GUIDE account

```bash
curl -i -X POST "$BASE_URL/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Guide",
    "lastName": "One",
    "email": "guide1@example.com",
    "password": "Guide@12345",
    "role": "GUIDE",
    "idProofUrl": "https://example.com/docs/guide1-id.pdf",
    "selfieUrl": "https://example.com/docs/guide1-selfie.jpg"
  }'
```

Expected: `201 Created`  
Response contains token at `data.token`.

## 1.3 Obtain TOURIST and GUIDE tokens via login (recommended repeatable flow)

```bash
curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"tourist1@example.com","password":"Tourist@123"}'
```

```bash
curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"guide1@example.com","password":"Guide@12345"}'
```

With `jq`:

```bash
TOURIST_TOKEN=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"tourist1@example.com","password":"Tourist@123"}' | jq -r '.data.token')

GUIDE_TOKEN=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"guide1@example.com","password":"Guide@12345"}' | jq -r '.data.token')
```

## 1.4 Obtain ADMIN token

### Option A (preferred): existing admin user

```bash
ADMIN_TOKEN=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"Admin@12345"}' | jq -r '.data.token')
```

Expected: `200 OK`

### Option B (safe test seed approach, non-production)

If no admin exists, promote a dedicated test user in DB (test env only):

1. Register/login `admin_seed@example.com` as TOURIST (or GUIDE).
2. In DB, run:

```sql
UPDATE users
SET role = 'ADMIN'
WHERE email = 'admin_seed@example.com';
```

3. Login again to receive ADMIN token claim:

```bash
ADMIN_TOKEN=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin_seed@example.com","password":"Seed@12345"}' | jq -r '.data.token')
```

---

## 2) Endpoint-by-Endpoint Authorization Verification

Use these headers:

```bash
AUTH_TOURIST="Authorization: Bearer $TOURIST_TOKEN"
AUTH_GUIDE="Authorization: Bearer $GUIDE_TOKEN"
AUTH_ADMIN="Authorization: Bearer $ADMIN_TOKEN"
```

## 2.1 Public endpoints (Auth)

### `POST /api/v1/auth/register`
- No token: **201**
- Invalid DTO: **400**
- Duplicate email: **409**

### `POST /api/v1/auth/login`
- No token: **200**
- Wrong password: **401**

## 2.2 Users endpoints

### `GET /api/v1/users/{id}`

1) Without token
```bash
curl -i "$BASE_URL/api/v1/users/<USER_ID>"
```
Expected: **401**

2) With token (current config allows authenticated roles)
```bash
curl -i "$BASE_URL/api/v1/users/<USER_ID>" -H "$AUTH_TOURIST"
```
Expected today: **200** (or 404)  
Expected secure target (owner/admin only): **403** for non-owner.

### `GET /api/v1/users/by-email?email=...`

1) Without token
```bash
curl -i "$BASE_URL/api/v1/users/by-email?email=tourist1@example.com"
```
Expected: **401**

2) With token (current config allows authenticated roles)
```bash
curl -i "$BASE_URL/api/v1/users/by-email?email=guide1@example.com" -H "$AUTH_TOURIST"
```
Expected today: **200** (or 404)  
Expected secure target (admin-only or self-only): **403** for non-admin/non-self.

## 2.3 Guide verification endpoints

### `POST /api/v1/guides/verification/apply`

1) Without token
```bash
curl -i -X POST "$BASE_URL/api/v1/guides/verification/apply" \
  -H "Content-Type: application/json" \
  -d '{"documentType":"AADHAR","documentNumber":"A123456789","documentUrl":"https://example.com/doc-a.pdf"}'
```
Expected: **401**

2) Wrong role (TOURIST)
```bash
curl -i -X POST "$BASE_URL/api/v1/guides/verification/apply" \
  -H "Content-Type: application/json" \
  -H "$AUTH_TOURIST" \
  -d '{"documentType":"AADHAR","documentNumber":"A123456789","documentUrl":"https://example.com/doc-a.pdf"}'
```
Expected: **403**

3) Correct role (GUIDE)
```bash
curl -i -X POST "$BASE_URL/api/v1/guides/verification/apply" \
  -H "Content-Type: application/json" \
  -H "$AUTH_GUIDE" \
  -d '{"documentType":"AADHAR","documentNumber":"A123456789","documentUrl":"https://example.com/doc-a.pdf"}'
```
Expected: **201** (first valid application) or **409** if pending already exists.

### `GET /api/v1/guides/verification/me`

1) Without token
```bash
curl -i "$BASE_URL/api/v1/guides/verification/me"
```
Expected: **401**

2) Wrong role (TOURIST)
```bash
curl -i "$BASE_URL/api/v1/guides/verification/me" -H "$AUTH_TOURIST"
```
Expected: **403**

3) Correct role (GUIDE)
```bash
curl -i "$BASE_URL/api/v1/guides/verification/me" -H "$AUTH_GUIDE"
```
Expected: **200**

## 2.4 Admin verification endpoints

### `GET /api/v1/admin/verifications`

- Without token: **401**
- Wrong role (`TOURIST`/`GUIDE`): **403**
- Correct role (`ADMIN`): **200**

```bash
curl -i "$BASE_URL/api/v1/admin/verifications?status=PENDING&page=0&size=10" -H "$AUTH_ADMIN"
```

### `PUT /api/v1/admin/verifications/{id}/approve`

- Without token: **401**
- Wrong role: **403**
- Correct role: **200**

```bash
curl -i -X PUT "$BASE_URL/api/v1/admin/verifications/<VERIFICATION_ID>/approve" -H "$AUTH_ADMIN"
```

After approve, old GUIDE JWT becomes stale and must be replaced with a new login token.

```bash
curl -i "$BASE_URL/api/v1/guides/verification/me" -H "$AUTH_GUIDE"
```

Expected with stale token: **401** with `errorCode=TOKEN_STALE` and message similar to `Token stale. Please login again.`

```bash
GUIDE_TOKEN=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"guide1@example.com","password":"Guide@12345"}' | jq -r '.data.token')
```

### `PUT /api/v1/admin/verifications/{id}/reject`

- Without token: **401**
- Wrong role: **403**
- Correct role: **200**

```bash
curl -i -X PUT "$BASE_URL/api/v1/admin/verifications/<VERIFICATION_ID>/reject" \
  -H "$AUTH_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"rejectionReason":"Document mismatch"}'
```

## 2.5 Places endpoints

### `GET /api/v1/places`
- Without token: **401**
- With `TOURIST/GUIDE/ADMIN`: **200**

```bash
curl -i "$BASE_URL/api/v1/places?page=0&size=10" -H "$AUTH_TOURIST"
```

### `GET /api/v1/places/{id}`
- Without token: **401**
- With `TOURIST/GUIDE/ADMIN`: **200** (or 404 if missing)

### `POST /api/v1/admin/places`
- Without token: **401**
- Wrong role (`TOURIST`/`GUIDE`): **403**
- Correct role (`ADMIN`): **201**

```bash
curl -i -X POST "$BASE_URL/api/v1/admin/places" \
  -H "$AUTH_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Marina Beach","description":"City beach","address":"Chennai","mapUrl":"https://maps.example/marina"}'
```

### `PUT /api/v1/admin/places/{id}`
- Without token: **401**
- Wrong role: **403**
- Correct role: **200**

### `DELETE /api/v1/admin/places/{id}`
- Without token: **401**
- Wrong role: **403**
- Correct role: **200**

## 2.6 Booking endpoints

### `POST /api/v1/bookings` (tourist create)
- Without token: **401**
- Wrong role (`GUIDE`/`ADMIN`): **403**
- Correct role (`TOURIST`): **201**

```bash
curl -i -X POST "$BASE_URL/api/v1/bookings" \
  -H "$AUTH_TOURIST" \
  -H "Content-Type: application/json" \
  -d '{"guideId":"<GUIDE_ID>","placeId":"<PLACE_ID>","scheduledAt":"2026-03-03T10:00:00Z"}'
```

### `GET /api/v1/bookings/my`
- Without token: **401**
- With tourist/guide token: **200** (own bookings)
- With admin token + `all=true`: **200** (all bookings)

```bash
curl -i "$BASE_URL/api/v1/bookings/my?page=0&size=10" -H "$AUTH_TOURIST"
curl -i "$BASE_URL/api/v1/bookings/my?all=true&page=0&size=10" -H "$AUTH_ADMIN"
```

### `GET /api/v1/bookings/{id}`
- Without token: **401**
- Non-owner non-admin: **403**
- Owner/admin: **200**

### `PUT /api/v1/bookings/{id}/accept`
- Without token: **401**
- Tourist/admin token: **403**
- Wrong guide owner: **403**
- Assigned guide with verification gate satisfied: **200**

### `PUT /api/v1/bookings/{id}/reject`
- Without token: **401**
- Tourist/admin token: **403**
- Wrong guide owner: **403**
- Assigned guide on REQUESTED: **200**

### `PUT /api/v1/bookings/{id}/confirm`
- Without token: **401**
- Guide/admin token: **403**
- Non-owner tourist: **403**
- Owner tourist on ACCEPTED: **200**

### `PUT /api/v1/bookings/{id}/cancel`
- Without token: **401**
- Guide/admin token: **403**
- Non-owner tourist: **403**
- Owner tourist on REQUESTED/ACCEPTED/CONFIRMED: **200**

### Booking transition conflict checks (`409 INVALID_STATE`)

```bash
# Accept twice -> second call should be 409
curl -i -X PUT "$BASE_URL/api/v1/bookings/<BOOKING_ID>/accept" -H "$AUTH_GUIDE"
curl -i -X PUT "$BASE_URL/api/v1/bookings/<BOOKING_ID>/accept" -H "$AUTH_GUIDE"

# Reject after accepted -> 409
curl -i -X PUT "$BASE_URL/api/v1/bookings/<BOOKING_ID>/reject" -H "$AUTH_GUIDE"

# Confirm before accepted -> 409
curl -i -X PUT "$BASE_URL/api/v1/bookings/<BOOKING_ID>/confirm" -H "$AUTH_TOURIST"
```

---

## 3) Ownership Tests (Critical)

## 3.1 `/api/v1/users/by-email` non-admin access to other user

Test:
```bash
curl -i "$BASE_URL/api/v1/users/by-email?email=guide1@example.com" -H "$AUTH_TOURIST"
```

Expected secure behavior: **403** (or limited self-only behavior).  
Current likely behavior: **200/404** based on current implementation.

## 3.2 `/api/v1/users/{id}` non-admin access to other user id

1) Get guide id from admin/by-email response (or DB).
2) Query as tourist:

```bash
curl -i "$BASE_URL/api/v1/users/<GUIDE_ID>" -H "$AUTH_TOURIST"
```

Expected secure behavior: **403**.  
Current likely behavior: **200/404** based on current implementation.

## 3.3 `/api/v1/guides/verification/me` should return only logged-in guide record

- Login as `guide1@example.com` → call `/me`.
- Login as another guide account `guide2@example.com` → call `/me`.
- Verify each response has its own `guideId` matching token subject.

```bash
curl -i "$BASE_URL/api/v1/guides/verification/me" -H "$AUTH_GUIDE"
```

Expected: **200** and record bound to current token user only.

---

## 4) Error Semantics Verification

## 4.1 Duplicate email register => `409`

```bash
curl -i -X POST "$BASE_URL/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName":"Tourist",
    "lastName":"One",
    "email":"tourist1@example.com",
    "password":"Tourist@123",
    "role":"TOURIST"
  }'
```

Expected: **409 Conflict** with error code `CONFLICT`.

## 4.2 Invalid DTO => `400` + field-level message

Example (missing/invalid fields):
```bash
curl -i -X POST "$BASE_URL/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName":"",
    "email":"not-an-email",
    "password":"123",
    "role":null
  }'
```

Expected: **400 Bad Request** with `VALIDATION_ERROR` and field messages.

## 4.3 Invalid approve/reject state => `409` (if transition guards are enforced)

Scenario:
1) Approve once as admin (`200`).
2) Reject same verification after approval.

```bash
curl -i -X PUT "$BASE_URL/api/v1/admin/verifications/<VERIFICATION_ID>/reject" \
  -H "$AUTH_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"rejectionReason":"Late invalid transition test"}'
```

Expected target behavior: **409 Conflict**.  
Current behavior may still be **200** until transition guards are implemented.

---

## 5) Smoke checks for docs/security edge

### Public API docs

```bash
curl -i "$BASE_URL/v3/api-docs"
```
Expected: **200** JSON

```bash
curl -i "$BASE_URL/swagger-ui/index.html"
```
Expected: **200** HTML

### Protected endpoint without token

```bash
curl -i "$BASE_URL/api/v1/places"
```
Expected: **401**

---

## 6) Postman Mapping (quick)

Create a Postman environment:
- `baseUrl`
- `touristToken`
- `guideToken`
- `adminToken`
- `guideVerificationId`
- `placeId`
- `guideId`

Use pre-request script per protected request:

```javascript
pm.request.headers.upsert({ key: 'Authorization', value: `Bearer ${pm.environment.get('adminToken')}` });
```

(Use corresponding token variable for tourist/guide/admin tests.)

---

## 7) Pass/Fail Exit Criteria

- Public endpoints (`/auth/*`, `/v3/api-docs`, Swagger UI) behave as expected.
- Protected endpoints return **401** without token.
- Admin endpoints return **403** for non-admin tokens.
- Correct roles return **200/201**.
- Booking ownership and state machine checks return expected **403/409** outcomes.
- Duplicate register returns **409**.
- DTO validation failures return **400**.
- Ownership tests fail for non-admin users (target: **403** for cross-user lookups).
- Invalid state transitions return **409** once guards are implemented.
