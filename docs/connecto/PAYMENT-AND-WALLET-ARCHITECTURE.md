# Connecto — Payment and Wallet Architecture

**Status:** target architecture conditional on owner-approved monetization. Public reference listing does not confirm paid calls, subscriptions, host compensation or withdrawals. Existing coin defaults are not policy.

## Decisions required before build

Owner wants wallet recharge via intended UPI/card options; promotional credits are event/campaign-dependent and are not guaranteed on every or first recharge. ₹80 paid →106 non-cash credits/coins is illustrative only, not a guaranteed offer or final catalog price. Decide campaign eligibility, amount, limits, expiry and ledger treatment for each approved campaign before launch. Per-minute one-to-one call billing applies. A Host proposes the user-facing per-minute rate within admin-configured limits; an admin approves it before display or billing. Snapshot the approved rate when a call starts so later changes apply only to new calls. Before placing a paid call, show the approved rate and require explicit caller confirmation. The first minute is charged on confirmed audio connection even if a normal hang-up occurs before 60 seconds; a call without confirmed audio connection produces no user charge and no Host earning. Pause further billing and matching Host earnings during audio interruption, resuming on reconnection; the already-triggered first-minute charge and earning stand once connected. Charge each additional minute as it starts; interrupted time does not advance billable time. Initial Host earnings are ₹2 INR cash per user-billed minute unit including the connected first minute, and Hosts request accrued-earnings payouts subject to eligibility/review, with UPI intended as the initial payout rail. Minimum payout threshold, processing cadence/delivery timing and provider selection are deferred pending provider/compliance review. User call prices and any percentage-based compensation model may change later. ₹2/min is the current owner-approved initial Host earning, not a reference-app claim. Numeric rate limits/change timing, authoritative interruption evidence/grace/finalization and other provider-failure refund rules; payout fees and required verification; subscription scope; price display/tax; user eligibility and India store/payment/legal rules remain unresolved. Store approved rules as versioned effective-dated configuration, with approval and audit. Never hardcode values or derive policy from seed data.

## Double-entry ledger

Use immutable journal transactions and entries. Each transaction has unique idempotency key, type, reference, status and timestamps. Each entry has account, asset/currency, signed integer amount and transaction ID. Sum of entries per transaction and asset must be zero. No update/delete of posted entries; corrections use linked reversal and replacement. Materialized balances are rebuildable projections, not the financial record.

Example conceptual postings (accounts and exact accounting treatment subject to finance/legal review):

| Event | Debit | Credit |
|---|---|---|
| Verified top-up | Store/provider clearing receivable | User coin liability |
| Call charge and Host earning | Debit the user's coin liability for the approved user price; separately accrue ₹2 INR to Host payable for each owner-defined billable minute, with the remaining economics accounted for under finance-approved treatment | Exact accounts and accounting treatment require finance/legal approval; credits and rupees are distinct assets and must not be assumed 1:1 |
| Refund | Appropriate revenue/host payable/clearing reversal accounts | User coin liability |
| Payout | Host payable | Payout clearing/cash account |

Coins may not map one-to-one to fiat. Accounting chart, tax, expiry, promotional coin restrictions and breakage require finance/legal approval.

## Payment lifecycle

`CREATED → PENDING_PROVIDER → VERIFIED → POSTED`, or `FAILED/REJECTED/REFUNDED/CHARGEBACK`. Persist provider transaction/event ID with uniqueness, product and account mapping from server catalog, sanitized verification evidence and timestamps. Verify receipt with official provider API; validate package/product/purchase state/ownership/acknowledgement; acknowledge/consume per provider rules. Webhooks are signature-verified and deduplicated. Client cannot choose credited amount.

Timeouts remain pending and reconcile; do not grant duplicate value. Refund/chargeback posts compensating journal transaction and invokes configured negative-balance/fraud policy. Retry with same idempotency key yields the original result.

## Call settlement

Approved user price and applicable billing policy are snapshotted at call start. Server/provider events define confirmed connection and interruption intervals; a client timer is presentation only. No confirmed connection means no charge or Host earning. Charge the first unit on confirmed connection; charge each additional minute as it starts, excluding paused interruption time. Host earning mirrors the user's billed units at the owner-approved ₹2 INR per unit. Settle idempotently and atomically in the ledger with unique call-period/settlement keys. Never debit without matching credits. Concurrent workers lock/claim a settlement period and use DB uniqueness to prevent replay. Call termination or retries cannot duplicate a period.

Uncertain RTC evidence enters `PENDING_RECONCILIATION`; don't silently charge maximum or discard a legitimate charge. A documented policy determines treatment for provider/client event disagreement, brief disconnect, grace window and insufficient funds.

## Wallet queries

`GET /api/v1/me/wallet` returns derived/verified available balance per asset and as-of time. Transactions are cursor-paginated with references and status; never expose another user's account. Distinguish purchased, promotional, earned, reserved and withdrawable value if policy/accounting requires separate accounts.

## Host earnings and payout

Initially accrue ₹2 INR to Host payable for each user-billed minute unit, including the connected first minute; additional units start after each additional minute of confirmed audio time, excluding interruptions. User call price is separately proposed by the Host within admin limits and approved; do not assume coins equal rupees or use an unapproved percentage split. Any later change to Host prices or percentage-based compensation must be versioned/effective-dated and approved. Earnings dashboard is a read projection of ledger. Payout request reserves eligible balance transactionally and moves through `REQUESTED → REVIEW/PROCESSING → PAID|FAILED|REJECTED`; external transfer reference unique, webhook/reconciliation required. Release reservation on failure/rejection with compensating entries. No host may approve own payout. Store payout sensitive details encrypted/provider-tokenized; never show full bank/UPI data to general admin.

## Reconciliation, fraud and operations

Provider statement reconciliation compares purchases/refunds/payouts to internal events and journal. Alert on imbalance, missing events, duplicate provider IDs, stale pending items, negative projection, unmatched call settlement. Manual adjustment requires reason, case reference, permission, dual control where configured, and audit; no SQL balance edits. Build replayable, idempotent reconciliation jobs with dry-run and per-record review.

## Existing migration warning

Current `wallets` mutable balance plus `coin_transactions` is not double-entry. Before cutover: freeze writes or dual-write under a controlled window, reconcile user and earned balances to source transactions, create traceable opening entries, validate journal/projection totals, run shadow comparison, then switch. Keep backup and rollback. Never infer missing financial history.

## Test gates

Atomic balanced postings; duplicate purchase/webhook/call-period/payout requests; parallel last-balance debits; rollback on partial error; rounding boundaries; concurrent payout/call settlement; provider timeout/replay/reversal; reconciliation mismatch; access control. Amounts/rates in tests are fixtures only, not product defaults.
