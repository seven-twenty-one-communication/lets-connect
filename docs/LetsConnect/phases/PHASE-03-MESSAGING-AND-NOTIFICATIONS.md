# Phase 03 — Messaging and Notifications

**Status:** Deferred pending Phase 00 live reference verification. Existing backend has chat APIs and Flutter chat screen currently simulates messages; production messaging is not verified.

## 1. Phase objective

Implement authorized, persistent one-to-one messaging and reliable notification hints with abuse and privacy controls.

## 2. Business requirements

BR-009, BR-010; message consent rules and delivery/read behavior require investigation and approval.

## 3. Scope and exclusions

In: text messages, conversation authorization, unread/delivery state, push/in-app notifications, block/report integration. Out: audio/video attachments, encryption claims, message editing/deletion/retention policy until decided, automated mental-health responses.

## 4. Dependencies on previous phases

Phase 00 message/connection consent, retention and push decisions; Phase 01 identity/session; Phase 02 if connection gating/blocking applies.

## 5. Architecture and design decisions

Conversation/message module in monolith; PostgreSQL canonical ordering; WebSocket for transient delivery hint; REST cursor history and send command. Outbox for push; clients reconcile by message ID. No claim of end-to-end encryption unless designed and verified.

## 6. Database entities and relationships

`conversations` and `conversation_members` connect users; `messages` references conversation/sender with client idempotency ID, body, server time and moderation/delivery state; `outbox_events` for push. Foreign keys and unique `(sender_id, client_message_id)`.

## 7. Database migration requirements

Create conversation/message/member tables and cursor indexes; do not migrate simulated chat content. Define retention/deletion and content moderation treatment before production data. Ensure message body length and encoding.

## 8. Backend implementation tasks

List conversations, fetch cursor pages, send idempotently, authorize membership and connection/consent policy, enforce block/suspension and rate limits, update read receipt if approved; outbox notification with opaque IDs; admin access/audit only as approved.

## 9. Frontend/mobile implementation tasks

Replace local mock replies with API/WebSocket; persist drafts according to privacy decision; handle send pending/sent/failed/retry/dedup; offline reconnect/paging; notification permission context and deep link reauthorization; report/block.

## 10. API endpoint specifications

`GET /api/v1/conversations`; `GET /api/v1/conversations/{id}/messages?before=`; `POST /api/v1/conversations/{id}/messages`; optional `POST /api/v1/conversations/{id}/read`. WebSocket `message.created` is notification only.

## 11. Request and response examples

```http
POST /api/v1/conversations/uuid/messages
Idempotency-Key: 2fb8...
{"clientMessageId":"c94...","text":"Hello"}
```
```json
{"messageId":"uuid","conversationId":"uuid","senderId":"uuid","text":"Hello","createdAt":"UTC timestamp","deliveryState":"ACCEPTED"}
```
Reject unknown recipient fields; do not include phone, push token or private moderation metadata.

## 12. Validation requirements

Body non-empty, configured maximum length, Unicode normalization and allowed content policy; client ID unique; conversation active; rate and spam limits; request size cap.

## 13. Authentication and authorization requirements

Bearer token required; sender derived from principal; membership checked on every read/send/read receipt; block/consent check at command time; push deep link requires fresh authorization.

## 14. Security considerations

Prevent IDOR, spam, abusive content and push-preview leakage. Redact message text from logs/analytics. TLS, at-rest access controls, retention and deletion; encryption model must not claim E2EE without key architecture.

## 15. Error handling

401/403/404 concealed conversation; 409 idempotency/body conflict; 413 oversize; 422 policy rejection; 429 rate; push failure does not invalidate saved message. Client shows failed state and retry.

## 16. Event processing

`message.created`, `message.read` (if approved), `push.requested`; outbox committed with message, consumer retries with dedupe. Realtime event includes IDs/time only; client fetches message if needed.

## 17. External service integrations

FCM is the selected push provider. Provider tokens must be registered by authenticated device endpoints, protected from cross-account mutation, rotated/removed when invalid, and used with retry/backoff. Keep payloads minimal and require the app to refetch authorized state. No analytics vendor without privacy review.

## 18. Unit testing

Membership, consent, block, message policy, idempotency and paging cursor rules.

## 19. Integration testing

PostgreSQL ordering/uniqueness/retention, WebSocket cross-user isolation, outbox retry/duplicate, FCM sandbox and mobile reconnect.

## 20. Performance testing

Concurrent send/read, conversation pagination and push backlog at approved workload; cap fan-out and queue age.

## 21. Failure scenarios

Client retries after lost response; push outage; WebSocket disconnect; duplicate/out-of-order notification; account blocked/deleted during open chat; outbox backlog. DB remains source of truth.

## 22. Acceptance criteria

Messages persist once, only members can read/send, blocks/consent enforced, no fake replies, retries deduplicate, push outage is recoverable, content and retention policy is documented.

## 23. Completion checklist

- [ ] Consent, retention and notifications approved.
- [ ] APIs, schema and outbox implemented.
- [ ] Flutter online/offline/error states verified.
- [ ] Abuse/privacy/access tests pass.

## 24. Deployment considerations

Configure WSS and FCM per environment; monitor delivery latency/outbox backlog; staged rollout; retention job dry-run before activation.

## 25. Documentation update requirements

Update API, database, security, UI, testing, evidence, traceability and DEVELOPMENT-STATUS.
