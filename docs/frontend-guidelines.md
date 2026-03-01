# Frontend Guidelines — Coimbatore Smart Tourism Platform

This document defines **non-negotiable frontend rules** to prevent folder collapse and future rework.

## Architecture (Must Follow)
- Stack: **React + Vite + TypeScript**
- Style: **Modular Monolith (Module-first, Layer-second vertical slices)**
- Only `common/` is shared across modules.
- No global `pages/`, `services/`, `apis/`, `components/` at root (except inside `common/`).

## Folder Structure (Exact)
Inside `frontend/src`:

src/
  app/
    App.tsx
    router.tsx
    routes.tsx
    layouts/
      PublicLayout.tsx
      DashboardLayout.tsx
    providers/
      AuthProvider.tsx
  common/
    api/
      http.ts
      apiTypes.ts
      errors.ts
    components/
    hooks/
    utils/
  modules/
    auth/
      pages/
      api/
      types/
      components/
    user/
      pages/
      api/
      types/
      components/
    guide/
    admin/
    place/
    package/
    booking/
    chat/
    rating/
    recommendation/
    sos/
  main.tsx

**Rule:** a module may import from `common/*`, but must not import from other modules.

## API Calling Rule (No Chaos)
- No axios calls inside pages/components.
- All API calls must be in: `modules/<module>/api/<module>Api.ts`
- All request/response types must be in: `modules/<module>/types/*.ts`
- One axios instance only: `common/api/http.ts`

## Backend Contract Assumptions

### Base URL
- Use env:
  - `VITE_API_BASE_URL=http://localhost:8080/api/v1`
- Do not hardcode URLs.

### Standard Response Envelope
All API responses must be treated as:

```ts
export type ApiSuccess<T> = {
  status: "SUCCESS";
  data: T;
  message?: string;
  timestamp: string;
};

export type ApiError = {
  status: "ERROR";
  errorCode: string;
  message: string;
  timestamp: string;
};

export type ApiResponse<T> = ApiSuccess<T> | ApiError;

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  isFirst: boolean;
  isLast: boolean;
};



Auth & JWT Handling (UI MVP)

Store JWT in memory + localStorage (MVP).

Axios interceptor attaches:

Authorization: Bearer <token>

Use route guards:

RequireAuth

RequireRole(["ADMIN"]), etc.

Key Domain Rules
Roles (Exact Strings)

ADMIN

GUIDE

TOURIST

Booking Status (Exact Strings)

REQUESTED

ACCEPTED

CONFIRMED

IN_PROGRESS

COMPLETED

RATED

Guide Verification Status

PENDING

APPROVED

REJECTED

Chat Gate

Chat UI allowed only when booking status is:

CONFIRMED or IN_PROGRESS

OTP Trip Start

Guide triggers “arrived” → backend generates OTP

Tourist shares OTP → guide submits OTP to start trip

Minimum MVP Pages (Scope)
Tourist

Places list/details

Packages list/details + filters (budget/category)

Request booking

My bookings

Booking details (chat + OTP)

Rate trip

Guide

Verification upload

Package CRUD

Booking requests (accept/reject)

Start trip via OTP

Mark trip complete

Admin

Approve guides

Manage places

Monitor bookings

Git / PR Discipline

Frontend work stays inside frontend/ only.

Do not modify backend/infra without explicit approval.

Keep commits small and scoped.

API Endpoints (UI Target Contract)

Auth:

POST /api/v1/auth/register

POST /api/v1/auth/login

User:

GET /api/v1/users/{id}

GET /api/v1/users/by-email?email=...

Guide Verification:

POST /api/v1/guides/verification

GET /api/v1/admin/guides/pending

POST /api/v1/admin/guides/{id}/approve

POST /api/v1/admin/guides/{id}/reject

Places:

GET /api/v1/places?page=&size=

GET /api/v1/places/{id}

POST /api/v1/admin/places

PUT /api/v1/admin/places/{id}

DELETE /api/v1/admin/places/{id}

Packages:

GET /api/v1/packages?page=&size=&category=&budgetMax=

GET /api/v1/packages/{id}

POST /api/v1/guides/packages

PUT /api/v1/guides/packages/{id}

DELETE /api/v1/guides/packages/{id}

Bookings:

POST /api/v1/bookings

GET /api/v1/bookings/my

GET /api/v1/bookings/{id}

POST /api/v1/bookings/{id}/accept

POST /api/v1/bookings/{id}/reject

POST /api/v1/bookings/{id}/arrived

POST /api/v1/bookings/{id}/start

POST /api/v1/bookings/{id}/complete

Chat (Polling):

GET /api/v1/bookings/{id}/messages?after=timestamp

POST /api/v1/bookings/{id}/messages

Ratings:

POST /api/v1/bookings/{id}/ratings

GET /api/v1/guides/{id}/ratings/summary

Recommendation:

POST /api/v1/recommendations