# Phase 01 — Foundation, Identity and Profiles

**Status:** Not started. Contract is proposed; exact onboarding fields require Phase 00 approval.

## 1. Phase objective

Implement a secure account/session foundation and minimal privacy-safe profile shared by User and optional Host roles.

## 2. Business requirements

BR-007; E-004 only at public-claim level. User can authenticate, recover an existing session, manage approved profile fields, avatar and account lifecycle. Do not imply phone identity is public.

## 3. Scope and exclusions

In: OTP/session integration, self profile, role assignments, avatar upload intent, consent/version capture where approved, account deletion workflow. Out: social discovery, nearby location, host KYC docs, generated avatar vendor choice until decided, email/password or unapproved demographic fields.

## 4. Dependencies on previous phases

Phase 00 must approve target market, age gate/legal approach, identity provider, profile fields, retention/deletion policy and access-token/session approach. Otherwise implement only approved minimal fields.

## 5. Architecture and design decisions

Backend identity/profile modules in existing Spring Boot monolith; Flutter secure session repository, typed API client, environment-based API URL. OIDC/OTP adapter boundary isolates provider. User is principal; roles are server-assigned. New API namespace `/api/v1`.

## 6. Database entities and relationships

`users` 1:1 `user_profiles`; 1:N `sessions`, `user_roles`, `devices`; profile optionally references private `media_assets`. Phone/provider subject unique and normalized. No age, gender, location or identity document columns without approved purpose.

## 7. Database migration requirements

Add reviewed versioned migration; constraints for normalized identity, role/status, session expiry/revocation. Migrate existing user/profile fields via additive mapping; redact/unlink data according to approved policy. Production Hibernate DDL disabled; migration tested from blank and current schema.

## 8. Backend implementation tasks

OTP challenge/create/verify; rate-limit and generic responses; session issue/refresh/revoke; `GET/PATCH /me`; avatar upload intent if approved; role guard; account deletion state machine; audit consent and privileged role changes. Remove debug OTP exposure outside test profile.

## 9. Frontend/mobile implementation tasks

Replace hard-coded endpoint with flavor/env config; implement typed auth client, secure token storage, refresh/revoke, session restore, login/loading/error states, minimal profile/avatar and settings/deletion UI. Remove fixed user IDs and fake balance; never store token in shared preferences.

## 10. API endpoint specifications

`POST /api/v1/auth/otp-challenges`; `POST /api/v1/auth/sessions`; `POST /api/v1/auth/sessions/refresh`; `DELETE /api/v1/auth/sessions/current`; `GET/PATCH /api/v1/me`; `DELETE /api/v1/me`; optional `POST /api/v1/me/avatar-upload-intents`. Actor comes from token.

## 11. Request and response examples

```json
POST /api/v1/auth/otp-challenges
{"phoneNumber":"+<country-code><number>"}
// 202 {"challengeId":"uuid","expiresAt":"UTC timestamp","resendAfterSeconds":0}
POST /api/v1/auth/sessions
{"challengeId":"uuid","code":"<user-entered-code>"}
// 200 {"accessToken":"<redacted>","expiresAt":"UTC timestamp","user":{"id":"uuid","profileState":"INCOMPLETE"}}
GET /api/v1/me
// 200 {"id":"uuid","displayName":"...","avatar":null,"languages":[],"visibility":"PRIVATE"}
```

Placeholder values are schema examples, not defaults; never include actual OTP/token in logs.

## 12. Validation requirements

Normalize phone per market; strict OTP length/provider response; challenge expiry/attempt count; profile field allowlist, length and Unicode; avatar file type/size/signature; session expiry and refresh reuse. Avoid account enumeration.

## 13. Authentication and authorization requirements

OTP routes rate-limited; `/me` requires current principal. Profile updates only self. Role elevation is administrative and audited; Host is never self-granted by changing request body. Admin uses separate role/scopes.

## 14. Security considerations

JWT issuer/audience/algorithm and key rotation; secure OS storage; TLS; OTP brute-force protection; replay prevention; private media; no PII in logs/analytics; deletion and retention reviewed with counsel.

## 15. Error handling

Standard problem response; 401 expired session, 409 challenge consumed/replay, 422 invalid profile, 429 throttled. Provider outage returns retryable unavailable without claiming OTP sent. No silent null session or fake login.

## 16. Event processing

Optional durable `user.created`, `profile.updated`, `session.revoked`, `account.deletion.requested` outbox events. Consumers idempotent; avoid phone/profile payload in events unless necessary.

## 17. External service integrations

OTP provider selected for market; secure media storage only if avatar upload approved; secure key storage; optional FCM device registration after consent/policy. Sandbox secrets isolated by environment.

## 18. Unit testing

Identity normalization, challenge expiry/replay/attempt limits, role policy, profile allowlist, session rotation/revocation, deletion states, no enumeration.

## 19. Integration testing

PostgreSQL constraints/migrations, OTP sandbox/stub, secure storage behavior, refresh replay, profile self-only access, avatar upload isolation and API contract tests.

## 20. Performance testing

OTP burst/abuse limits and session read/write under agreed launch profile. Do not load production SMS provider.

## 21. Failure scenarios

OTP provider timeout, repeated request, DB commit failure, token refresh race, device reinstall, lost device, object upload interruption, deletion/legal hold conflict. Return explicit state and allow safe retry/revocation.

## 22. Acceptance criteria

User can complete verified authentication and restore/revoke session; only self profile mutation; no hardcoded identity; OTP secrets absent from release; approved deletion flow; schema migration and tests pass.

## 23. Completion checklist

- [ ] Market/age/fields/retention decisions approved.
- [ ] Session security and provider adapter implemented.
- [ ] Mobile secure token lifecycle implemented.
- [ ] Migrations and rollback/recovery tested.
- [ ] Auth/privacy E2E and abuse tests pass.
- [ ] No secrets or demo identity in release.

## 24. Deployment considerations

Configure per-environment OTP/secret store, TLS, allowed origins, rate limits, database migration job and alert on delivery/error rates. Roll out additive migrations before client.

## 25. Documentation update requirements

Update API, security, database, UI, evidence, traceability and DEVELOPMENT-STATUS. Mark avatar/age/profile details as unresolved if not approved.
