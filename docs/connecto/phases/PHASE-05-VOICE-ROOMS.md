# Phase 05 — Voice Rooms

**Status:** Not started. Public listing claims voice chat rooms; exact interaction and moderation are unknown.

## 1. Phase objective

Implement only owner-approved voice-room lifecycle and audio participation with explicit membership, role, privacy and moderation semantics.

## 2. Business requirements

BR-003 and BR-006 at public-claim level. Room discovery, capacity, public/private behavior, speaker queue, host/moderator powers and safety response need reference validation.

## 3. Scope and exclusions

In: approved room browse/create/join/leave and participant roles, RTC access, mute/kick/report if approved. Out: recording, live streaming, gifts, monetized rooms, open chat or arbitrary room discovery absent decisions.

## 4. Dependencies on previous phases

Phase 00 confirms room behavior; Phase 01 identity; Phase 02 visibility/block policy; Phase 04 RTC provider and token contracts; Phase 08 moderation if operational tools needed.

## 5. Architecture and design decisions

Room module owns persisted state and membership; Agora adapter provisions RTC channel; REST mutates membership; STOMP distributes participant metadata. Server authorizes role changes. Room audio provider event is not equivalent to application authorization.

## 6. Database entities and relationships

`voice_rooms` owner/state/visibility/config; `room_memberships` user, role, state, joined/left; `room_events` state audit; optional `room_moderation_actions`. Foreign keys, unique active membership and provider event dedupe. Do not retain location or audio.

## 7. Database migration requirements

Review current `voice_rooms`/participant tables against approved lifecycle; additive forward migrations for role/state/event IDs; backfill only reliable fields. Define cleanup/retention for ended rooms and memberships.

## 8. Backend implementation tasks

List eligible rooms, create if approved, join atomically with capacity/ban checks, issue room-scoped RTC token, leave/revoke, fetch authorized members, moderator actions, timeouts and durable events. Enforce blocks, account status and room visibility.

## 9. Frontend/mobile implementation tasks

Room list/detail, participant state, join/leave/mute, audio focus, clear role affordance, reconnect and report/block. No fake participants, simulated online count or unbacked chat.

## 10. API endpoint specifications

`GET/POST /api/v1/rooms`; `POST /api/v1/rooms/{id}/join`; `POST /api/v1/rooms/{id}/leave`; `GET /api/v1/rooms/{id}/members`; moderator actions only after approval. `room.member.joined/left` events carry minimal public data.

## 11. Request and response examples

```http
POST /api/v1/rooms/uuid/join
Authorization: Bearer <token>
Idempotency-Key: <uuid>
{}
```
```json
{"roomId":"uuid","membershipId":"uuid","role":"LISTENER","state":"JOINED","rtcCredentialExpiresAt":"UTC"}
```
Role names/capacity are illustrative pending decision; token is separate or redacted and participant-scoped.

## 12. Validation requirements

Room state/visibility, capacity, role, active membership uniqueness, owner permissions, request idempotency, token expiry, banned/blocked account and provider callbacks.

## 13. Authentication and authorization requirements

Authenticated join; owner/moderator-only room configuration; member-only roster where room is private; participant-specific RTC privilege. WebSocket SUBSCRIBE verifies active membership; SEND rechecks role.

## 14. Security considerations

Prevent room enumeration, unauthorized join, role escalation, token reuse after leave, harassment and personal data leakage. WSS/TLS, provider secret isolation, no recording by default.

## 15. Error handling

404 hidden/nonexistent room; 409 full/closed/already changed; 403 role/policy; 429 join spam; provider unavailable does not create false membership/audio success. Cleanup partial RTC allocation.

## 16. Event processing

Room state and outbox write atomically; events have room sequence/version, ID, timestamp; clients dedupe and refresh after reconnect. Provider callbacks verified and idempotent.

## 17. External service integrations

Reuse approved RTC provider from Phase 04; push for room invitations only if approved. No new broker/provider absent documented requirement.

## 18. Unit testing

Membership state transitions, capacity race, role controls, visibility/block policy, token scope and duplicate callback handling.

## 19. Integration testing

PostgreSQL concurrent joins/leave, STOMP isolation, two/three-device real room audio, provider callback, moderator actions and reconnection.

## 20. Performance testing

Room fan-out and concurrent participants at configured approved capacity; monitor provider quotas, event backlog and signaling latency.

## 21. Failure scenarios

Owner disconnect, room closure during join, token expiry, duplicate join, WS loss, provider outage, participant process death, capacity race, moderator action delayed. Server reconciles presence and revokes access.

## 22. Acceptance criteria

Only approved room journeys; no unauthorized joins/actions; duplicate operations do not create members; leave/ban revokes future access; no fake roster/audio; recovery tested.

## 23. Completion checklist

- [ ] Reference/owner room semantics approved.
- [ ] Membership and role model/migrations reviewed.
- [ ] Token authorization and moderation covered.
- [ ] Real device and capacity/failure tests pass.
- [ ] Recording absent unless separately approved.

## 24. Deployment considerations

Deploy migrations before code; monitor room count/join failures/provider health; cap usage according to provider and approved config; provide kill switch and moderation escalation.

## 25. Documentation update requirements

Update audio architecture, API, database, security, UI, testing, evidence, traceability and development status.
