# Connecto — Audio Calling Architecture

**Evidence:** Public listing claims private one-to-one calls and voice rooms. Exact behavior, provider and billing are not confirmed by the listing. Existing repository uses Agora; keep it as baseline pending evaluation.

## Component responsibilities

- Mobile client handles permission UX, RTC SDK, mute/audio routing and user-visible connection state; it does not authoritatively set duration, participant identity, price or settlement.
- Spring call module owns authorization, state machine, deadlines, idempotency, participant membership and canonical call metadata.
- RTC adapter creates/validates provider room/channel and short-lived participant-scoped token. Provider credentials remain server-only.
- PostgreSQL persists lifecycle and settlement references. Redis may support ephemeral presence/leases only if correctness does not depend on it.
- STOMP/push delivers hints/invitations; client refetches canonical call state. FCM background delivery behavior is TBD.

## Call state machine

Proposed states: `REQUESTED → RINGING → ACCEPTED → CONNECTING → CONNECTED → RECONNECTING → ENDED`; terminal alternatives `DECLINED`, `TIMED_OUT`, `CANCELLED`, `FAILED`. Only explicit allowed transitions are accepted; every transition includes actor, server timestamp, idempotency key and audit/event record. Late accept after timeout returns conflict. No charge before approved billable connection policy begins.

| Event | Required server behavior |
|---|---|
| Initiate | Authenticate caller; verify target is an admin-approved Host currently online/available, plus visibility/consent, block/suspension and concurrency; create idempotent request; notify target. Recheck availability atomically to handle presence/call races. |
| Accept | Authenticate callee; atomically claim ringing request; issue separate short-lived RTC credentials only after commit/authorization. |
| Decline/cancel | Authorized participant only; terminal state once; release availability and notify peer. |
| Timeout | Server scheduler/worker expires ringing call idempotently; exact timeout configured after product decision. |
| Connect | Validate provider callback or participant evidence; persist join evidence and server time. Do not trust client timestamp alone for money. A call without confirmed audio connection creates no user charge and no Host earning. |
| Disconnect | Persist provider/client signal; mark reconnecting during policy window; finalize on confirmed end/timeout. |
| Reconnect | Reauthorize participant; issue fresh expiring credential; retain same call only if policy permits; deduplicate joins. |
| Bill/settle | No monetization unless approved. Settlement consumes canonical connected intervals and effective policy; ledger transaction is idempotent and balanced. |

## Signaling contract

REST creates state and performs commands. WebSocket events are versioned, minimal, uniquely identified hints, e.g. `call.invited {eventId, callId, expiresAt, callerPublicProfile}`; never include bearer/RTC token in broadcast. Subscriptions are participant-scoped; each SEND validates schema, ownership, state and rate limit. On reconnect, fetch `GET /api/v1/calls/{id}`; process events with deduplication.

Do not use the current simple broker for multi-instance production without choosing a cross-instance relay/broker or single-instance documented limit. Sticky routing alone does not broadcast between application nodes.

## RTC credentials and provider

Compare current Agora implementation with any alternative only against requirements: Android support, connection quality, region/data handling, cost, recording defaults, webhook/quality telemetry, token granularity, background behavior, operations and exit cost. No provider switch is approved. Tokens: minimum privileges, participant identity binding, short TTL, server secret, no client-chosen channel, revoke/expire on end, refresh through authorized endpoint.

## Billing policy interface

Keep call metering separate from UI timer. Record `connectedAt`, participant intervals, disconnect/reconnect events, authoritative end, provider event IDs and evidence quality. Owner decisions: charge first minute on confirmed connection (even if a normal hang-up is under 60 seconds), charge each additional minute when it starts, and exclude interruption time from billable time; no confirmed connection means no charge or Host earning. A policy computes units from server/provider-confirmed intervals; exact interruption evidence/grace and provider-failure refunds remain unresolved. Concurrent settlement uses transaction lock and unique period keys. Host earns the approved per-minute amount for the same user-billed units. Client receives authoritative balance/status refresh, not trusted deductions.

## Network/audio failure handling

Permission denied: explain and do not request call. RTC join fail: bounded retry, no false connected state, no charge or Host earning unless connection is confirmed. WebSocket loss: keep RTC if healthy but reconcile canonical state; reconnect signaling and state fetch. Network interruption: `RECONNECTING`, pause further billing and matching Host earnings until audio resumes per owner decision; the already-triggered first-minute charge/earning stands once connected. Exact interruption evidence and grace/finalization deadline remain unresolved. App process death: server timeout/provider evidence finalizes. One party hangs up: server idempotently ends and revokes access. Provider webhook delay: pending reconciliation, alert, no duplicate billing.

No audio recording by default. Any recording requires separate legal, consent, retention, access and product approval.

## Observability and tests

Measure request-to-ring, accept-to-RTC-join, connection success, audio quality/drop, reconnect, timeout, token errors, settlement delay/mismatch and provider status. Avoid raw audio/PII. Test state transition races, accept/timeout race, double-end, duplicate callbacks, invalid participant, expired token, reconnect, client clock skew, provider outage, background/foreground, Bluetooth/audio focus and poor networks.
