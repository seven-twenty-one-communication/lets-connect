# Connecto backend

Spring Boot REST and STOMP backend for Connecto-style voice conversations. The local `dev` profile uses an in-memory H2 database and development-only substitutes for SMS OTP, Agora tokens, and Play purchases.

## Run locally

From this directory in PowerShell:

```powershell
mvn "-Dspring-boot.run.profiles=dev" spring-boot:run
```

The dev database and seeded categories, gifts, and coin packages are reset when the process stops. The fixed OTP is `123456`; it expires after five minutes and is single-use. Authenticate with the JWT returned by `/api/auth/verify-otp` and send it as `Authorization: Bearer <token>` for every other API.

For local purchase testing, use product IDs returned by `GET /api/wallet/packages`, set `signedData` to `MOCK_PURCHASE:<orderId>`, and `signature` to `MOCK`. The development-only `/api/wallet/{userId}/add-test-coins` endpoint is also available.

To try listener calls, create a user, submit `/api/listeners/apply`, then use the dev-only authenticated endpoint `/api/dev/listener/verify` and `/api/listeners/{userId}/toggle-online`. Listener calls are charged one minute when accepted and then once per elapsed minute. Voice room join returns the same mock token format.

For STOMP at `/ws-calling`, send the bearer token in the `CONNECT` frame's `Authorization` header. Subscriptions are limited to the authenticated user's listener/chat channel, call sessions they participate in, and rooms they have joined.

Admin KYC/report endpoints require a user whose `users.role` is `ADMIN`; provision admin accounts out-of-band rather than exposing a public role-change API.

## Implemented API areas

- OTP login and user profile editing
- Listener applications, KYC review, listener discovery, and online presence
- Private calls, call signaling, minute billing, and wallet transaction history
- Public voice-room creation/join/leave and participant signaling
- Chat, gifts, and coin packages
- User blocks, reports, and admin report review

## External services and deployment

The dev profile does not send SMS, connect audio, or verify a real Play purchase. Mock Agora tokens are placeholders and cannot join an actual audio channel. Outside `dev`/`test`, OTP requests return `503` until an OTP provider is configured; real Agora credentials and `GOOGLE_PLAY_PUBLIC_LICENSE_KEY` are required for those integrations. Before release, replace the mock adapters, use Google Play's server-side purchase verification, set `JWT_SECRET` to a strong secret of at least 32 bytes, and provide PostgreSQL connection variables (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASS`).

## Tests

```powershell
mvn test
```
