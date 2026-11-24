# Connecto — Database Design

**Status:** target logical model. Existing `database/schema.sql` is evidence of current structures, not the target migration contract.

## Principles

PostgreSQL is the authoritative transactional store. Use UUID primary keys for user-owned records, UTC `timestamptz`, explicit foreign keys, unique constraints for idempotency, bounded text fields, and indexed query paths. Do not store secrets/tokens in ordinary tables or log payloads. Version schema with reviewed Flyway/Liquibase migrations (tool selection is a Phase 00/09 decision); disable production `ddl-auto` mutation. Never edit an applied migration; add a forward migration and tested rollback/forward-recovery procedure.

## Logical entities and relationships

| Entity | Key fields/relationships | Notes |
|---|---|---|
| `users` | id, normalized phone/provider subject, status, created/deleted timestamps | Unique identity; PII minimization and deletion policy required. |
| `user_profiles` | user_id, display name, avatar asset id, bio, locale, visibility | Public projection excludes phone and private attributes. |
| `user_preferences` | user_id, language/interests/privacy settings | Do not infer gender/age or collect without approved need. |
| `user_roles` | user_id, role, status | User/Host/Admin; privilege separation; role changes audited. |
| `sessions` | id, user_id, token hash/rotation family, expiry/revocation | Store refresh-token hashes only; token policy documented in Security. |
| `connections` | requester_id, recipient_id, status, timestamps | Unique normalized pair; transitions pending/accepted/declined/blocked require policy. |
| `blocks` | blocker_id, blocked_id, created_at | Unique directed pair; enforced in discovery, chat and call authorization. |
| `conversations`, `messages` | participant membership; sender, body, sent/delivered/read state | Authorization by membership; retention/edit/delete policy unresolved. |
| `devices`, `push_tokens` | user_id, token hash/value encrypted as appropriate, platform, active | Token lifecycle and cleanup; do not expose to clients. |
| `calls` | id, caller_id, callee_id, state, timestamps, policy snapshot, provider channel ref, idempotency key | Provider secret/token not persisted in readable form; state transitions auditable. |
| `call_participants` | call_id, user_id, provider uid, join/leave/reconnect timestamps | Evidence used for server-authoritative duration and reconciliation. |
| `voice_rooms`, `room_memberships` | room state/visibility; membership role and timestamps | Capacity/roles/moderation rules remain unverified. |
| `wallet_accounts` | id, owner, currency/asset code, account type | No balance as sole evidence; derive or cache materialized balance. |
| `ledger_transactions`, `ledger_entries` | transaction idempotency key/status; account, signed integer amount | Immutable; entries per transaction sum to zero within asset; correction is reversal. |
| `payment_orders`, `provider_events` | provider, order/token hash, product, state, unique provider transaction id | Verify server-side; persist dedupe key and sanitized receipt evidence. |
| `call_settlements`, `refunds`, `payouts` | call/payment reference, status, ledger transaction id | Unique per settlement step; explicit state machines/reconciliation. |
| `reports`, `moderation_actions` | reporter, subject, reason, restricted evidence refs, status/action | Need-to-know access; audit all access/action. |
| `verification_cases`, `private_documents` | subject, status, provider reference, private object key | Only if legally/product-approved; encrypted/private access and retention/deletion. |
| `audit_events`, `outbox_events` | actor/action/target/correlation; event state/retries | Append-only access controls; no credentials or message/audio content by default. |

## Constraints and indexes

- Unique normalized account identifier; unique connection pair as applicable; no self-connection; no self-block.
- Calls: indexes on `(caller_id, created_at desc)`, `(callee_id, created_at desc)`, state/expiry; unique idempotency key scoped to actor; validate legal state transitions in transaction.
- Messages: `(conversation_id, created_at, id)` cursor index. Discovery: selective active/visibility/language filters. Add geospatial index only after approved product and privacy policy.
- Ledger: unique transaction idempotency key; indexes on account/time and external reference; check transaction entries balance by asset in transaction service/DB-safe design. Never rely on a non-atomic application-only check.
- Provider events: unique `(provider, event_id)`; payout/payment external references unique when supplied.
- Retention indexes/partitioning only after measured volume and deletion/legal policy.

## Transaction boundaries

Connection transition + outbox; call initiation + availability claim + event outbox; accept + state transition; call settlement + all balanced ledger entries + call aggregate update; purchase verification result + ledger posting + provider event dedupe; payout reserve + payout state; moderation action + audit event. External HTTP/RTC/store calls are not run while holding DB locks; use pending states, outbox, and reconciliation.

## Existing schema gap

The current SQL has `wallets.coin_balance` and `earned_coins_balance`, plus `coin_transactions`; this provides history but not a double-entry accounting journal. Production JPA validation and Flyway migrations are the baseline; the H2-only dev profile has schema recreation for local isolation. Migration to the target accounting model must reconcile legacy balances from verified transaction history, create opening-balance journal entries with auditable provenance, compare sums, and only then switch reads/writes. Do not drop legacy data before reconciliation and rollback window closure.

## Migration and privacy strategy

1. Snapshot and inventory production data; test migration on sanitized copy.
2. Expand schema and dual-write or backfill with idempotent checkpoints.
3. Verify counts, account balances, balanced journal and referential integrity.
4. Switch reads with feature flag; monitor discrepancies; retain rollback path.
5. Contract only after explicit approval and backup/restore test.

PII/location/KYC/media retention and deletion are policy decisions. Prefer private object storage references over public URLs; implement deletion/anonymization that respects legal holds and accounting retention rules, which require counsel.

## Open decisions

Migration tool, currency/coin model, ledger chart of accounts, exact social graph semantics, location need, message retention, KYC necessity, deletion obligations, and traffic/partition thresholds.
