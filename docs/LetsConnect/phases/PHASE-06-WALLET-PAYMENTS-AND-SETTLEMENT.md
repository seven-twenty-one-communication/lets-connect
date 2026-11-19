# Phase 06 — Wallet, Payments and Settlement

**Status:** Not started and gated. No payment or paid calling until business, finance, legal and store rules are approved.

## 1. Phase objective

If monetization is approved, implement verified payment and auditable double-entry settlement with idempotent refunds/reconciliation.

## 2. Business requirements

BR-011 includes owner-approved billing rules: first unit charged on confirmed audio connection (even if a normal hang-up is under 60 seconds); no user charge or Host earning without confirmed connection; pause further billing/earnings during audio interruption and resume afterward; charge each additional minute as it starts; Host initially earns ₹2 INR per user-billed minute unit. UPI/card recharge and campaign-based promotional credits are intended, but campaign eligibility/amount is not set. Before a paid call, disclose and confirm the admin-approved Host price, snapshotted at call start. Still resolve user price/catalog, campaign rules, India store/payment/tax/accounting policy, authoritative RTC evidence/grace/finalization, provider-failure refunds and payout ledger integrations. Existing seed values and commission are not requirements.

## 3. Scope and exclusions

In: ledger, provider order lifecycle, server verification, call charge postings, refund and reconciliation. Out: hardcoded coin packs, mutable balance as source of truth, client-side timer billing, manual SQL adjustment, host payouts unless Phase 07 approved.

## 4. Dependencies on previous phases

Phase 00 approved economics/market; Phase 04 authoritative call events; finance/legal chart of accounts; provider/store account and sandbox. Phase 07 depends on this ledger.

## 5. Architecture and design decisions

Ledger/payment module; immutable balanced entries; materialized balance projection; external provider adapter; idempotency keys; transactional outbox and reconciliation jobs. Use provider verification API and signed webhooks where available; do not assume client receipt signature alone is sufficient.

## 6. Database entities and relationships

`wallet_accounts`, `ledger_transactions`, `ledger_entries`, `payment_orders`, `provider_events`, `refunds`, `call_settlements`, `balance_projection`. Unique external transaction/event and idempotency keys; postings linked to call/order. Asset/currency rules and accounting chart approved by finance.

## 7. Database migration requirements

Introduce ledger additively. Reconcile existing wallet and `coin_transactions`; create opening entries only from verified balances with provenance. Shadow compare totals, dual-write/controlled cutover, backup and rollback. Never drop legacy ledger/balance until sign-off.

## 8. Backend implementation tasks

Product catalog from server configuration; create pending order; verify with official provider API; dedupe and atomically post; webhook signature/event processing; server-authoritative call settlement; reversals/refunds; projection rebuild; reconciliation and discrepancy queue; privileged audit.

## 9. Frontend/mobile implementation tasks

Show server catalog and any explicitly approved event promotion; use a payment flow allowed by current India/Google Play policy; report purchase result to backend; display pending/verified/failed and refreshed balance; restore purchases per policy. Before a paid call, show the snapshotted, approved Host price and obtain explicit confirmation. Call UI shows server status and never controls charges.

## 10. API endpoint specifications

`GET /api/v1/me/wallet`; `GET /api/v1/me/wallet/transactions`; `GET /api/v1/payment-products`; `POST /api/v1/payment-orders`; `POST /api/v1/payment-orders/{id}/verify`; internal signed webhook endpoint; settlement internal service. All money-changing routes idempotent.

## 11. Request and response examples

```json
POST /api/v1/payment-orders
{"productId":"server-catalog-id","provider":"approved-store"}
// 201 {"orderId":"uuid","state":"PENDING_PROVIDER","idempotencyKey":"uuid"}
POST /api/v1/payment-orders/uuid/verify
{"providerPurchaseToken":"<redacted>"}
// 200 {"orderId":"uuid","state":"POSTED","wallet":{"asset":"approved-asset","available":"server-derived"}}
```
Never accept client amount/coins as authoritative; examples have no suggested monetary value.

## 12. Validation requirements

Catalog product active/market; provider package, signature/state/ownership; unique purchase/order; asset decimal/integer precision; balanced entries; sufficient available balance; valid effective policy; no duplicate period/refund.

## 13. Authentication and authorization requirements

Purchaser can access own order/account only; provider webhook uses signature/mTLS and replay checks; settlement service account scoped; finance/admin actions role-gated and audited; no host approval of own payout.

## 14. Security considerations

Protect receipts/tokens, provider secrets and wallet IDs; rate-limit verification; replay prevention; fraud/chargeback policy; do not log payload; ledger immutable; secret rotation; enforce store policy and privacy requirements.

## 15. Error handling

Provider unavailable => pending/retry/reconcile, not failed-success; invalid/replayed purchase => reject without posting; insufficient funds => business error; journal invariant failure => rollback and alert; duplicate request returns prior result.

## 16. Event processing

`payment.verified`, `ledger.posted`, `call.settlement.requested`, `refund.requested`, `provider.event.received`; transactional outbox, unique IDs, retry/dead-letter/manual review. Never settle on an unpersisted scheduler event.

## 17. External service integrations

Google Play if Android monetization and store policy require it; other payment/payout providers only after market/legal approval. Sandbox credentials, signed notifications, reconciliation exports, provider limits and operational contacts required.

## 18. Unit testing

Balanced postings, charge unit calculation, rounding configured policy, idempotency, refunds/reversal, product mapping, account classification and projection.

## 19. Integration testing

PostgreSQL concurrent debits, rollback, duplicate webhook/purchase, provider sandbox, migration/reconciliation of legacy balances, payout interaction in Phase 07.

## 20. Performance testing

Parallel purchase verification and call billing at expected concurrency; lock contention, settlement backlog and ledger query latency; define targets from forecast.

## 21. Failure scenarios

Purchase paid but callback lost, callback duplicated/out of order, store timeout, app killed mid-purchase, concurrent call ticks, DB crash after provider verify, refund/chargeback, imbalance or projection drift. Pending event reconciles without duplicate posting.

## 22. Acceptance criteria

Every posted journal balances; repeated inputs post once; client cannot set amount/duration; provider-verified purchase only; call settlement based on approved server evidence; reconciliation finds injected mismatch; legacy cutover auditable.

## 23. Completion checklist

- [ ] Approved financial rules and counsel review.
- [ ] Double-entry ledger and projection implemented.
- [ ] Provider verification/webhooks/idempotency pass.
- [ ] Legacy wallet data reconciliation approved.
- [ ] Concurrency, refund and reconciliation tests pass.

## 24. Deployment considerations

Deploy additive migration and shadow comparison; canary ledger writes; monitor discrepancies; keep kill switch for purchase/paid calls; reconcile before/after cutover; define financial support runbook.

## 25. Documentation update requirements

Update payment, database, API, security, audio, testing, traceability, decision log and DEVELOPMENT-STATUS. Maintain policy revision history.
