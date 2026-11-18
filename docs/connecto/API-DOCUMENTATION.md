# Connecto — API Contract

**Status:** proposed v1 contract; not yet implemented. Existing backend endpoints are unversioned under `/api`; do not silently relabel them as compatible with this contract.

## Conventions

- Base: `/api/v1`; JSON UTF-8; UTC ISO-8601 timestamps; UUID strings; cursor pagination.
- Auth: `Authorization: Bearer <access-token>` except explicitly public OTP bootstrap/health routes. Every user-scoped command derives actor from token; never trust a `userId` supplied as authority.
- Mutation requests use `Idempotency-Key` (opaque client-generated UUID) when retry can create side effects.
- Return stable problem format: `{ "type": "about:blank", "title": "Conflict", "status": 409, "code": "CALL_NOT_AVAILABLE", "detail": "The requested person is unavailable.", "traceId": "..." }`. Never expose stack traces, provider secrets, OTPs, or sensitive identifiers.
- Errors: 400 invalid input; 401 missing/expired authentication; 403 unauthorized/policy denied; 404 absent or concealed resource; 409 state/idempotency conflict; 422 business rule; 429 rate limited; 5xx retry only according to idempotency semantics.

## Proposed endpoint catalog

| Area | Endpoint | Purpose / authorization |
|---|---|---|
| Auth | `POST /auth/otp-challenges`; `POST /auth/sessions`; `POST /auth/sessions/refresh`; `DELETE /auth/sessions/current` | OTP integration, create/rotate/revoke session. Public challenge endpoints rate-limited. |
| Profile | `GET/PATCH /me`; `DELETE /me`; `POST /me/avatar-upload-intents` | Self-only; private upload intent, no arbitrary public URL. |
| Discovery | `GET /discover?cursor=&language=&interest=`; `GET /profiles/{id}` | Public projection and visibility/blocks enforced. Nearby endpoint only after explicit approval. |
| Connections | `POST /connections`; `GET /connections`; `POST /connections/{id}/accept`; `POST /connections/{id}/decline`; `DELETE /connections/{id}` | Actor from token; state-checked/idempotent. Exact consent requirement unresolved. |
| Messaging | `GET /conversations`; `GET /conversations/{id}/messages?before=`; `POST /conversations/{id}/messages` | Participant-only; bounded body; rate-limited; safety enforcement. |
| Calls | `POST /calls`; `GET /calls/{id}`; `POST /calls/{id}/accept`; `POST /calls/{id}/decline`; `POST /calls/{id}/end`; `GET /calls?cursor=` | Participant/role authorization; transitions and idempotency; price omitted unless approved. |
| Rooms | `GET/POST /rooms`; `POST /rooms/{id}/join`; `POST /rooms/{id}/leave`; `GET /rooms/{id}/members` | Public/private room policy and membership authorization. |
| Wallet | `GET /me/wallet`; `GET /me/wallet/transactions`; `POST /payment-orders`; `POST /payment-orders/{id}/verify` | Only if monetization approved; provider verification and ledger authority. |
| Safety | `POST /users/{id}/blocks`; `DELETE /users/{id}/blocks/{blockedId}`; `POST /reports` | Self as actor; reason enum; no cross-user mutation. |
| Host | `POST /host/applications`; `PATCH /host/availability`; `GET /host/earnings`; `POST /host/payout-requests` | Proposed; host role/status and legal policy gates. |
| Admin | `/admin/**` case, user, host, finance, config APIs | Proposed least-privilege scopes, MFA; current code only covers listener KYC status and report review. |

## Example: create call (unpriced baseline)

```http
POST /api/v1/calls
Authorization: Bearer eyJ...
Idempotency-Key: 8b60094a-44c5-4e31-bc45-fb00db794a32
Content-Type: application/json

{"calleeUserId":"d47d93ef-d8e4-4fd8-a632-12c96fcb680e"}
```

```json
{
  "callId": "18b79698-f12d-4113-9c2a-1408e3056fd8",
  "state": "RINGING",
  "expiresAt": "2026-11-18T12:15:00Z",
  "eventId": "75f7bd4c-9e03-4751-8d35-af2c48616b14"
}
```

This example intentionally specifies no timeout value or price; configuration/policy must provide it. RTC credentials are returned only to an authorized participant via a short-lived endpoint after state validation, never in room events.

## Example: accept call

```http
POST /api/v1/calls/18b79698-f12d-4113-9c2a-1408e3056fd8/accept
Authorization: Bearer <callee-token>
Idempotency-Key: 9590a98b-e922-419e-8d50-51a22e6b7b68
```

```json
{"callId":"18b79698-f12d-4113-9c2a-1408e3056fd8","state":"ACCEPTED","rtcCredentialExpiresAt":"2026-11-18T12:20:00Z"}
```

Never include actual token in logs or analytics. Return credentials through an authenticated, non-cacheable response specific to the participant.

## Validation and authorization

Validate DTOs at boundary; canonicalize IDs; cap page size/body length; reject unknown state transitions; protect against IDOR by loading resource and checking principal membership. WebSocket CONNECT authenticates; SUBSCRIBE authorizes exact topic/resource; SEND validates schema and actor for every command. Disallow arbitrary `/user`/`/topic` destinations. Rate-limit OTP, calls, messages, reports, and sensitive admin operations.

## Versioning and compatibility

All new APIs use `/api/v1`. Migrate clients explicitly; keep existing `/api/**` behind compatibility adapters during a documented transition. Additive response changes only where clients tolerate unknown fields. Breaking changes require new version, migration plan, deprecation window, metrics and rollback.

## OpenAPI and contract tests

Generate/maintain OpenAPI 3 spec alongside code; CI validates examples, schema compatibility and auth requirements. Contract tests cover 401/403/404 concealment, idempotent retries, error envelope, paging and WebSocket destinations. Do not mark draft routes implemented until backed by controller/service and tests.
