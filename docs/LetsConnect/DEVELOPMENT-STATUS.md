# Connecto — Development Status

**Blueprint status:** documentation approved for generation; implementation status must be updated only from verified evidence. **Last reviewed:** 2026-11-18.

## Phase tracker

| Phase | Status | Exit evidence required |
|---|---|---|
| 00 Reference and decisions | In progress | Evidence ledger and owner decisions closed or explicitly deferred. |
| 01 Foundation, identity, profiles | In progress (limited authentication slice) | Full session/security/profile contract, migrations, tests and mobile flow verified. |
| 02 Discovery, direct calling, privacy | Not started | Approved discovery semantics and privacy/consent tests. |
| 03 Messaging, notifications | Deferred pending reference verification | Owner deferred one-to-one text chat until the live app flow is verified. |
| 04 One-to-one audio | Not started | Real provider E2E lifecycle and failure recovery. |
| 05 Voice rooms | Deferred pending reference verification | Listing description mentions rooms, but none of six reviewed listing screenshots shows a room flow; owner is unsure rooms exist in the app. |
| 06 Wallet, payment, settlement | Not started | Approved economics, balanced ledger, provider verification and reconciliation. |
| 07 Host experience and payouts | Not started | Approved host policy, legal checks and secured payout flow, if in scope. |
| 08 Admin, moderation, operations | Not started | Existing versus proposed admin scope clear; least privilege and audits tested. |
| 09 Production readiness and launch | Not started | SLOs, security/compliance review, migration/restore/rollback and go-live gates. |

## Repository baseline (not production-ready declaration)

Evidence inventory is in [PROJECT-OVERVIEW.md](PROJECT-OVERVIEW.md) and [REFERENCE-APP-EVIDENCE.md](REFERENCE-APP-EVIDENCE.md). Existing modules are represented in code, but mock/demo behavior, unconfigured real integrations, mutable-balance accounting, the H2-only dev schema policy, absent production auth/migration pipeline and limited end-to-end tests are blockers to production use.

## Current implementation evidence

The Phase 01 implementation has started with a limited foundation slice: React Native has a typed API client, Keychain-backed access-token helpers, environment-based API configuration, and OTP/session restore UI. The backend exposes compatibility `/api/v1/auth/send-otp`, `/api/v1/auth/verify-otp`, and authenticated `GET/PATCH /api/v1/me`. This is not a complete authentication or session flow. Firebase exchange, refresh/revoke, account lifecycle and mobile E2E remain incomplete. Focused backend tests and mobile/admin TypeScript/lint checks pass.

This is not a complete or production-ready authentication flow. Firebase Phone Authentication and FCM are selected directions but are not configured or integrated; the current OTP delivery path is not production-ready. Backend Firebase ID-token verification/exchange, app session refresh/revocation, push-token lifecycle, complete server-side authorization, profile, account lifecycle, database migrations, and end-to-end tests remain incomplete. Existing call, wallet, and discovery demo behavior remains out of scope for this slice.

## Decisions recorded

- **Initial launch market:** India only. Other markets are out of initial launch scope.
- **Minimum age:** 18+ only (owner decision); India-specific legal and Google Play review remains required.
- **Age assurance:** Method deferred until India legal/Google Play review; minimize age data and do not collect age/identity documents unless required.
- **Connection flow:** direct-call option after login, based on topic choices and a language catalog deferred pending verification; no separate friendship request required first. Rooms/chat remain deferred pending live-app evidence.
- **Initial matching choices:** Relationship, Support, Understanding, Marriage, Confidence, Friends; “All” shows every topic. Defer language catalog until reference verification and owner approval; matching and ranking behavior remain to be decided.
- **Direct-call discovery:** show matching admin-approved Hosts who are online and available; user chooses whom to call. Presence freshness, race handling and ranking remain unresolved.
- **Location discovery:** exclude location/GPS-based nearby matching from initial launch unless live-reference evidence and separate privacy approval justify adding it.
- **Host activation:** Host must apply and receive admin approval before becoming discoverable or accepting calls. Initial profile/safety review is decided; exact fields, reviewer checklist, service level, rejection and appeal remain unresolved.
- **Host review:** Initial review covers the application/profile and required safety/terms consent; no ID documents unless required by India law or payout provider. Detailed checklist and appeal process remain open.
- **Host identity documents:** No document KYC by default; require collection only if applicable India law or the selected payout provider requires it. This is not a legal determination; verify before enabling payouts.
- **Initial Host earnings:** ₹2 INR cash per user-billed minute unit, including the connected first minute. Additional started-minute billing and interruption pauses are decided; authoritative evidence, failure treatment and payout details remain unresolved. User prices and any later percentage model may change.
- **Host payout initiation:** Hosts request accrued-earnings payouts subject to eligibility/review. Minimum threshold and processing cadence/delivery timing are deferred pending provider/compliance review; provider and fees remain unresolved.
- **Host payout method:** UPI is the intended initial rail; choose the provider after India compliance review.
- **Call billing boundary:** No user charge or Host earning without confirmed audio connection. Once connected, the first-minute user charge and matching ₹2 Host earning apply even if normal hang-up occurs before 60 seconds and stand through a subsequent interruption. Pause further billing/earnings during interruption; charge each additional minute as it starts, excluding paused time. Interruption evidence, grace/finalization, and other provider-failure refunds remain unresolved.
- **Paid-call consent:** Show the Host's approved per-minute price and require explicit caller confirmation before placing a paid call.
- **Host rates:** Host proposes a per-minute price within admin-configured limits; admin approval is required before display or billing. Numeric limits and change/effective-time policy remain open.
- **Rate changes:** Snapshot the approved rate when the call starts; a later change applies only to new calls.
- **Voice rooms:** deferred from initial planned scope pending live-app verification; Play listing text and screenshot evidence do not resolve whether users can access rooms.
- **One-to-one text chat:** deferred from initial planned scope pending live-app verification.
- **Monetization:** UPI/card are intended wallet payment options. Promotional credits are event/campaign-dependent, not a guaranteed first-recharge offer; ₹80→106 credits is illustrative only. Calls follow the approved billing policy above. Hosts receive initial ₹2 INR per user-billed minute and UPI-requested payouts; user prices, campaign rules, later percentage model, payout threshold/timing/provider, failure refunds and India payment/store compliance remain unresolved.

## Blockers / unresolved decisions

Remaining blockers: authorized live-app evidence for exact screens and whether rooms/chat exist; language catalog, profile fields, matching/ranking and presence freshness; wallet catalog and campaign policy; per-minute user price; interruption evidence/grace and other failure/refund policy; Host payout threshold/timing/provider and review details; store/payment compliance; retention/deletion; admin product scope/framework; scale/SLO; Firebase project setup and India production SMS configuration/billing/quotas; RTC, payment and hosting accounts. India-specific review is required to determine age-assurance and Host payout identity-document requirements; legal/platform obligations including 18+ require qualified review. Do not infer completion from an API or entity existing.

## Status rules

`Not started` means no implementation verified. `In progress` requires verified partial work with remaining exit evidence recorded. `Blocked` records explicit blocker/owner. `Complete` requires acceptance criteria, tests, deployment evidence and docs update. AI agents must not mark tasks complete on generating code alone.

## Next recommended work

Continue Phase 01 only on approved, policy-independent foundation work; keep Phase 00 in progress until authorized live-app evidence and required India legal/store/provider reviews close or formally disposition the remaining blockers. Do not copy protected branding/assets or invent unverified reference behavior. Do not start financial, location, KYC or recording flows while required decisions are open.
