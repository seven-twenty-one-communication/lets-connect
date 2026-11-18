# Phase 04 — One-to-One Audio Calling

**Status:** Not started. Listing confirms broad private audio-call claim; repository Agora integration and demo flows do not establish production readiness.

## 1. Phase objective

Deliver one-to-one voice lifecycle with server-owned state, authorized RTC credentials, reliable reconnect/end, and settlement hooks that remain disabled until monetization is approved.

## 2. Business requirements

BR-002; exact invitation, notification, timeout, pricing and eligibility are unresolved. Apply user consent, block and safety rules.

## 3. Scope and exclusions

In: initiate, ring, accept, decline, cancel, timeout, connect, disconnect, reconnect, end, history and report/block. Out: recording, video, group rooms, client-authoritative billing, unapproved price or monetization.

## 4. Dependencies on previous phases

Phase 00 call semantics/provider/market decisions; Phase 01 auth; Phase 02 visibility/consent; Phase 03 notification and invitation delivery as applicable.

## 5. Architecture and design decisions

Call orchestration module; Agora adapter baseline; RTC directly between clients/provider; Spring REST commands + participant-scoped STOMP hints; PostgreSQL authoritative state; outbox for invitations; Redis not authoritative. Multi-instance signaling needs broker/relay decision.

## 6. Database entities and relationships

`calls` references caller/callee; `call_participants` records provider join/leave evidence; `call_events` immutable transition audit; optional `call_settlements` only under Phase 06. Unique scoped idempotency key and provider event ID; encrypted/private provider metadata as needed.

## 7. Database migration requirements

Add state/version/timestamps/deadline/idempotency and participant-event tables; migrate old call statuses through explicit mapping; preserve history. Constraints prevent duplicate active pair/settlement where supported; test concurrency with PostgreSQL.

## 8. Backend implementation tasks

Call state machine and transition service; caller/callee/block/availability authorization; atomic availability claim; event outbox; server expiry; RTC provider token adapter with scoped TTL; provider callback verification; canonical status/history; idempotent end/reconnect; configured settlement interface disabled until approval.

## 9. Frontend/mobile implementation tasks

Integrate real auth/API/STOMP, microphone permission, call invite UI, RTC state and controls, audio route/focus, reconnect UI, explicit failure/end, report/block and history. Remove demo tokens, forced connected fallback, local billing, fixed users and fake balance. Never auto-start timer from UI-only callback as settlement evidence.

## 10. API endpoint specifications

`POST /api/v1/calls`; `GET /api/v1/calls/{id}`; `POST /{id}/accept|decline|end`; authorized credential endpoint; `GET /api/v1/calls` history. STOMP events `call.invited`, `call.state.changed`, `call.ended`; subscription access checked per participant.

## 11. Request and response examples

```json
POST /api/v1/calls
{"calleeUserId":"uuid"}
// 201 {"callId":"uuid","state":"RINGING","expiresAt":"UTC","eventId":"uuid"}
POST /api/v1/calls/uuid/accept
// 200 {"callId":"uuid","state":"ACCEPTED","rtcCredentialExpiresAt":"UTC"}
```
RTC token is returned separately, non-cacheable and redacted in logs. No price unless owner-approved.

## 12. Validation requirements

Allowed state transition; caller != callee; target eligible and not blocked; idempotency; expiry; only participants; provider UID/channel mapping; token TTL; reject replayed/late provider callbacks.

## 13. Authentication and authorization requirements

All commands authenticated; caller can initiate/end; callee can accept/decline; participant can fetch only own call; STOMP CONNECT requires token, SUBSCRIBE participant membership, SEND schema+role+state check.

## 14. Security considerations

Short-lived least-privilege RTC tokens, no client channel selection, secret rotation, rate-limited calls, abuse/block enforcement, no phone leakage, explicit origin allowlist, no recording by default.

## 15. Error handling

409 stale/duplicate transition; 403 ineligible; 404 conceal call; 429 abuse; provider errors return explicit retryable failure and do not claim connected. Reconciliation state for ambiguous provider status.

## 16. Event processing

Persist call transition/outbox atomically. Event ID dedupe, ordered by call ID/version; outbox retry. Provider callback signature and event ID verified. WebSocket is hint; state fetch restores truth.

## 17. External service integrations

Agora current baseline; OTP/session inherited; FCM if background ring approved. Record account credentials in secret manager. Evaluate provider capabilities without assuming switch.

## 18. Unit testing

State machine, authorization matrix, timeout, reconnect window (configured), idempotency, token scope/expiry and interval calculation independent of UI.

## 19. Integration testing

PostgreSQL accept/timeout race, concurrent availability claim, STOMP isolation, provider token and callbacks, mobile two-device real audio, event retry and state recovery.

## 20. Performance testing

Measure request-to-ring, accept-to-audio, connection success, signaling fan-out and reconnect under owner-approved concurrency; test provider quotas and network regions.

## 21. Failure scenarios

No push, WS disconnect, callee busy, late accept, caller app killed, RTC join denied, one-sided audio, token expiry, network switch, duplicate end, provider callback delayed, DB unavailable. State stays reconcilable; no fabricated success or charge.

## 22. Acceptance criteria

Two authenticated physical clients can complete real call; invalid participants denied; all terminal outcomes release availability; retries don't duplicate; app reconnect fetches canonical state; no billing unless approved policy and Phase 06 ledger pass.

## 23. Completion checklist

- [ ] User flow/provider evidence reviewed.
- [ ] Real provider adapter, token controls and state machine tested.
- [ ] WebSocket authorization and multi-instance topology addressed.
- [ ] E2E poor-network/background tests pass.
- [ ] Demo path removed from release.
- [ ] Billing hook remains disabled absent decision.

## 24. Deployment considerations

WSS/TLS, explicit origins, provider secrets, webhook allow/signature verification, timeout worker, dashboard/alerts, staged provider rollout and call kill switch.

## 25. Documentation update requirements

Update audio, API, database, security, testing, UI, traceability and status. Reconcile pre-existing launch docs without treating unverified claims as fact.
