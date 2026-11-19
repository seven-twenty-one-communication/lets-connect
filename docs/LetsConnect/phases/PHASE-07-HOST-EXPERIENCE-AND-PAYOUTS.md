# Phase 07 — Host Experience and Payouts

**Status:** Not started; Host application/admin approval with initial profile and safety/terms-consent review, no default identity-document KYC (unless required by India law or the payout provider), initial Host earnings of ₹2 INR cash per user-billed minute unit (including the connected first minute), Host-requested payouts subject to eligibility/review, and UPI as the intended initial payout rail are owner-decided. Minimum threshold, processing cadence/delivery timing and provider selection are deferred pending provider/compliance review. User call prices and any percentage-based compensation model may change later. Reference listing does not establish the paid host/payout workflow. Verify document requirements with qualified India legal counsel and the chosen provider.

## 1. Phase objective

Implement approved Host capabilities as a role in the same Flutter app and securely expose approved availability, earnings and payout workflows.

## 2. Business requirements

BR-012 and Host portions of BR-011 are owner-approved target requirements, not reference-confirmed behavior. Review the application/profile and safety/terms consent; collect identity documents only if required by qualified India legal advice or the chosen payout provider. Initial Host earning is ₹2 INR for each user-billed minute unit, including the first connected unit; additional units follow the user's started-minute billing, excluding audio interruptions. UPI is the intended payout rail; provider, eligibility, threshold, timing, fee and tax treatment require resolution.

## 3. Scope and exclusions

In: host application/role, approved profile/safety review, availability, incoming-call UX, Host-proposed rates within admin limits with admin approval before publication/billing, server-derived earnings (initially ₹2 INR per user-billed minute unit) and UPI-intended payout request/status. Out: separate Host app, mandatory document upload without legal need, guessed user prices or percentage splits, unapproved incentive/rating mechanisms.

## 4. Dependencies on previous phases

Phase 00 approved host definition and legal obligations; Phase 01 roles/profiles; Phase 04 calls; Phase 06 ledger/earnings postings; Phase 08 admin review and audit.

## 5. Architecture and design decisions

Host domain module in monolith; role-based routes/views in same Flutter app; profile status state machine; payout adapter separate from ledger. Host cannot self-approve verification or payout. Sensitive documents handled only if approved.

## 6. Database entities and relationships

`host_profiles` 1:1 user; `host_applications`, `verification_cases`, `host_availability`, `host_earnings_projection`, `payout_requests`, linked ledger accounts/transactions. Bank/payment destination tokenized/encrypted. Do not duplicate earnings as mutable sole record.

## 7. Database migration requirements

Create host-specific tables/constraints and role migration; map current `listener_profiles` only after a domain mapping review, preserving audit/history. Existing KYC fields do not prove required policy. Migrate payout data without exposing sensitive JSON.

## 8. Backend implementation tasks

Apply/review workflow, profile moderation, availability leases, incoming call eligibility, earnings query from ledger, payout reserve and state machine, approved provider transfer, reconciliation and admin audit. No arbitrary client earnings/rate updates.

## 9. Frontend/mobile implementation tasks

Role-aware navigation; application state and clear review status; online/offline toggle with server confirmation; incoming call accept/reject; earnings/payout history; secure error/retry; mask payout details and make fees/timing transparent per approved policy.

## 10. API endpoint specifications

`POST /api/v1/host/applications`; `GET/PATCH /api/v1/host/profile`; `PATCH /api/v1/host/availability`; `GET /api/v1/host/earnings`; `POST /api/v1/host/payout-requests`; `GET /api/v1/host/payout-requests`. Admin review endpoints are separate and scoped.

## 11. Request and response examples

```json
POST /api/v1/host/applications
{"displayBio":"...","languages":["..."],"categoryIds":["uuid"]}
// 202 {"applicationId":"uuid","state":"PENDING"}
POST /api/v1/host/payout-requests
{"amount":"approved-unit-amount","destinationId":"vault-reference"}
// 202 {"payoutRequestId":"uuid","state":"REQUESTED"}
```
No currency, minimum, fee, or verification fields implied; server policy validates.

## 12. Validation requirements

Host eligibility/status; approved profile content; active-call absence before offline; earnings available and unreserved; payout destination verified under policy; idempotency, sanctions, amount boundaries and duplicate pending requests.

## 13. Authentication and authorization requirements

Host self-access only; role/status checked on each command; admin reviewer separate permission; finance/payout processor service account scoped; prevent self-review/self-payout approval.

## 14. Security considerations

KYC minimization, encrypted/private document access if required, masking, audited access, anti-fraud, payout destination protection, host/user abuse and safety. Do not expose host legal identity to users.

## 15. Error handling

403 role/application state; 409 availability/payout race; 422 policy/eligibility; provider timeout remains pending; rejected payout reverses reservation; document scan failure returns explicit pending/retry.

## 16. Event processing

`host.application.submitted/reviewed`, `host.availability.changed`, `host.earning.posted`, `payout.requested/processed/failed`; outbox, dedupe, audit, user notification without sensitive data.

## 17. External service integrations

KYC/identity and payout vendors are TBD and conditional; select only after legal, security, coverage, data residency and cost assessment. FCM for host incoming call if approved. Store provider credentials in secrets manager.

## 18. Unit testing

Application transitions, role policy, availability/call race, earnings projection, payout reservation/reversal and approval segregation.

## 19. Integration testing

PostgreSQL locking and ledger linkage; verification sandbox if approved; payout sandbox idempotency; admin separation; same-app role access tests.

## 20. Performance testing

Presence/availability changes, host discovery and earnings reads at forecast; payout batches/provider rate limits only as approved.

## 21. Failure scenarios

Host goes offline during ringing/call, duplicate application, reviewer outage, document provider timeout, payout callback lost/replayed, balance reserved twice, destination revoked. Recover state without double earning/payout.

## 22. Acceptance criteria

Host cannot activate before approved eligibility; users see only approved public fields; online status accurate; earnings reconcile to ledger; payout cannot be duplicated or self-approved; verification data access audited.

## 23. Completion checklist

- [ ] Host role and market/legal rules approved.
- [ ] Mapping from existing listener schema reviewed.
- [ ] Same Flutter app role UX complete.
- [ ] Earnings tied to ledger; payout lifecycle sandbox-tested.
- [ ] KYC/privacy controls justified and tested.

## 24. Deployment considerations

Feature flag host role; migrate existing listener records with reconciliation; separate reviewer roles; monitor payout pending/failed; rollback disables new application/payout without corrupting ledger.

## 25. Documentation update requirements

Update business, database, API, security, payment, audio, UI, testing, evidence, traceability, status and all host policy decisions.
