# Connecto — Testing Strategy

## Test pyramid and environments

Unit tests for domain rules; repository/database integration tests against PostgreSQL-compatible behavior; API/WebSocket contract tests; React Native unit/component tests; device E2E for OTP-to-call; provider sandbox tests; load, security, resilience and operational drills. H2-only tests are insufficient for PostgreSQL locking/index/transaction behavior. Separate local, CI, staging and production credentials/data; production user data is never copied unredacted into lower environments.

## Unit tests

State machine allowed/forbidden transitions; access/visibility/block policy; profile validation; session/token policy; message/rate limits; ledger balance invariant; idempotency; call interval computation under configured policy; host split/config effective date; retention/deletion and moderation transitions. Use fixture values only; don't turn them into production policy.

## Integration and contract tests

- REST authn/authz, validation, pagination, errors, idempotent retries, API version compatibility.
- PostgreSQL migrations from empty DB and representative prior schema; constraints, transaction rollback, parallel debit/settlement, uniqueness.
- STOMP CONNECT/SUBSCRIBE/SEND authorization, cross-user denial, reconnect, duplicate event and multi-instance topology.
- OTP provider sandbox, Agora token/RTC provider callbacks, FCM delivery, object upload/download isolation, payment purchase/webhook and payout sandbox.
- OpenAPI and mobile/backend contract compatibility.

## E2E scenarios

Sign-up/session restore; profile visibility; discovery and block filtering; connection consent if approved; message send/retry; online/offline and incoming call; accept/decline/timeout; two real devices join/leave; mute/audio route; network switch/reconnect; background/notification; history. If paid: purchase verification→wallet ledger→call settlement→host earnings→payout sandbox→refund/reconciliation. Report/block must prevent subsequent interactions.

## Financial invariants

For every posted ledger transaction, entries sum to zero per asset. Replaying provider event, client request, scheduler tick or callback does not change totals twice. Concurrent last-unit charges never create negative spendable balance. Aggregate projections equal ledger. Refund reverses original posting and is itself idempotent. Test transaction rollback at each write boundary and crash/restart recovery.

## Performance and resilience

Define traffic, concurrency, latency and availability SLOs before test. Load discovery, OTP throttling, WebSocket fan-out, call signaling, concurrent settlement, wallet reads and event consumers. Include soak and burst tests, provider rate-limit/outage, DB failover, Redis outage if used, delayed callbacks, app restart, backup restore, queue backlog and deployment rollback. Never load-test real payment or emergency channels.

## Security tests

Threat model and abuse cases; dependency/SAST/secret/container scans; API IDOR/role escalation; JWT expiry/revocation; WebSocket topic spoofing; CORS/origin; injection and oversized payloads; OTP enumeration/brute force; upload malware/type/size/authorization; payment replay/signature; location/PII leakage; log redaction; admin audit/access; account deletion/retention.

## Mobile/device matrix

Supported Android versions/devices finalized at release planning. Physical devices and emulators; Wi-Fi/cellular/poor network; microphone permission denied/revoked; Bluetooth/headset/speaker; incoming push foreground/background/killed; process death; battery saver; rotation and accessibility. Confirm no hardcoded localhost/emulator endpoints, demo identities, mock tokens, simulated balance/chat or test purchase path in release.

## Release gates

All critical flows pass; no unresolved critical/high security issue without formal risk acceptance; financial invariants and reconciliation pass; migration and backup restore demonstrated; call reliability meets owner-approved targets; privacy/UGC/payment policy and support materials reviewed; alerts/runbooks and rollback exercised. Fail closed if money/call authorization is ambiguous.
