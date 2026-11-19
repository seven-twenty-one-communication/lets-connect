# Connecto — Business Requirements

**Status:** Draft requirements with evidence labels. A requested feature is not automatically reference-confirmed or approved for release.

## Classification

Use exactly one primary label for each requirement:

- **Confirmed from the reference app** — public Google Play listing claim only unless direct flow evidence is cited.
- **Observed from screenshots/recordings** — only when the screen evidence has actually been examined.
- **Not publicly verifiable — investigation needed** — no reliable public evidence for exact behavior.
- **Proposed improvement — approval required** — a product-brief request or architectural recommendation not established by reference evidence.

See [REFERENCE-APP-EVIDENCE.md](REFERENCE-APP-EVIDENCE.md) and map IDs in [REQUIREMENTS-TRACEABILITY.md](REQUIREMENTS-TRACEABILITY.md). The user's written brief is a target request, not proof of reference behavior.

## Functional requirements

| ID | Requirement | Evidence |
|---|---|---|
| BR-001 | Support building friendships and meaningful connections. | Confirmed from the reference app — listing claim; exact relationship flow unverified. |
| BR-002 | Support private one-to-one audio calls. | Confirmed from the reference app — listing claim; call invitation, accept, decline, and charging behavior unverified. |
| BR-003 | Support voice chat rooms. | Listing description claims this, but none of six reviewed listing screenshots shows a room UI and the owner doubts the option exists. Live app availability is Not publicly verifiable — investigation needed; defer from initial scope pending verification. |
| BR-004 | Offer generated avatars and privacy-conscious profiles. | Confirmed from the reference app — listing claim; avatar creation/editing flow unverified. |
| BR-005 | Support conversations in regional languages. | Confirmed from the reference app — listing claim; exact language list and discovery filters unverified. |
| BR-006 | Provide a verified community and safety/moderation tools. | Confirmed from the reference app — listing claim; verification level and review process unverified. |
| BR-007 | Registration, authentication and account/session management. | Not publicly verifiable — investigation needed. User brief requests this. |
| BR-008 | Profile photos, gender/age/interests, search/filter and online presence. | Not publicly verifiable — investigation needed. Exclude nearby/location discovery from initial launch unless reference evidence and separate privacy approval justify it. |
| BR-009 | After login, allow selection from an approved language catalog (deferred pending reference verification/owner approval) and topics, show matching admin-approved Hosts who are online/available, and let the user choose whom to call without requiring a friendship request first. Initial topics: Relationship, Support, Understanding, Marriage, Confidence and Friends; “All” displays profiles across all topics. | Owner decision for target flow; exact matching/reference journey remains unverified. Initial discovery excludes location/GPS. Presence freshness and server-side call eligibility need design. One-to-one text chat is deferred until live app verification. |
| BR-010 | Call history, notifications and settings/account deletion. | Not publicly verifiable — investigation needed. |
| BR-011 | Offer wallet recharge via intended UPI/card options; promotional credits are conditional on an explicitly approved event/campaign, not guaranteed for every or first recharge. Before a paid call, display the Host's approved per-minute price and require explicit caller confirmation. Charge the first minute at confirmed audio connection even if a normal hang-up occurs before 60 seconds. Calls without confirmed connection generate neither user charges nor Host earnings. Pause further billing/earnings during audio interruption and charge each additional minute as it starts, excluding interruption time. | Owner decision. Example ₹80→106 credits/coins is illustrative only, not a final price or guaranteed offer. Decide campaign eligibility/amount/accounting, product catalog, per-minute rate bounds, interruption evidence/grace and other provider-failure refunds, store policy and legal review. Subscriptions remain unverified/proposed. |
| BR-012 | Hosts apply for admin approval before discovery/call availability; initial review covers application/profile and required safety/terms consent. Identity documents are not required unless India law or the payout provider requires them. Hosts propose user-facing rates within admin-configured limits, and an admin must approve a rate before display or billing. Snapshot the approved rate at call start so later changes apply only to new calls. Initial Host earnings are ₹2 INR cash per user-billed minute unit, including the connected first minute. Hosts request payouts of accrued earnings subject to eligibility/review; UPI is the intended initial payout rail. Minimum threshold and processing cadence/delivery timing/provider are deferred pending provider/compliance review. User call prices and any percentage-based compensation model may change later. | Owner decision; exact application fields, review/appeal workflow, numeric rate limits/effective-time rules, payout provider and any future percentage model remain unresolved. Document requirements need qualified India legal/provider review. |
| BR-013 | Admin web workflows beyond existing backend KYC-status and report endpoints. | Proposed improvement — approval required. |

## Non-functional requirements

These are engineering requirements for the target system, not claims about the existing app:

- **NFR-001 Security/privacy:** least privilege; minimize sensitive collection; encrypt transport and sensitive stored data; never expose phone numbers, exact location, tokens, KYC data, or payment secrets to other users or logs.
- **NFR-002 Reliability:** define availability, recovery-time, recovery-point, and call-quality objectives with the owner before launch; measure them rather than inventing SLAs.
- **NFR-003 Financial integrity:** no client-authoritative balance/duration; immutable balanced journal or equivalent audit ledger; transactional, idempotent settlement and reconciliation.
- **NFR-004 Compatibility:** version public APIs; use expand/migrate/contract database changes and document deprecations.
- **NFR-005 Operability:** structured redacted logs, metrics, traces/correlation IDs, alerts, backups, restore drills, and runbooks.
- **NFR-006 Accessibility/localization:** accessible controls and content; define launch languages and translations with product approval.
- **NFR-007 Performance:** set measurable discovery/API, signaling, call setup, and load targets based on launch cohort and provider capabilities before load testing.

## User journeys (target state; exact details unresolved)

1. **User onboarding:** open app → consent/notices → authenticate → create minimal profile/avatar → choose privacy/preferences → reach discovery. Exact required fields, age gate, and sequence require investigation and approval.
2. **Discover/connect:** browse/filter eligible people → view permitted profile details → send/accept/decline connection if that workflow is approved → block/report at every relevant surface.
3. **Message:** open authorized conversation → send/receive messages with delivery/retry state → report/block; enforce consent and abuse controls.
4. **One-to-one audio:** initiate only when eligible → recipient receives app/push invitation → accept/decline/timeout → obtain short-lived RTC credentials → connect → recover or end → server finalizes duration and any approved settlement → show history/receipt and safety actions.
5. **Voice room:** discover/join a room → receive role-appropriate audio privileges → mute/leave/report → server revokes membership/token and updates presence. Moderator/speaker rules require decisions.
6. **Host (if approved):** apply → complete only legally/product-approved verification → wait for authorized review → configure profile/availability → receive/accept/decline an eligible call → view server-calculated earnings → request payout under approved rules.
7. **Admin:** authenticate with MFA/SSO decision → view only authorized cases → review evidence/metadata → take reasoned action → create immutable audit entry.

## Business rules requiring explicit decision

- Initial market is India only and minimum age is 18+ (owner decisions, 2026-11-18). Defer the age-assurance method until India legal/Google Play review; minimize age data and collect age/identity documents only if required.
- Whether friend requests are required before chat/call; whether users can be hosts.
- Nearby discovery radius, precision, retention and opt-out; do not persist exact coordinates by default.
- Price units, currency, top-up products, subscriptions, minimum billable duration, grace intervals, rounding, call timeout, refunds and charge dispute policy.
- Host share/commission, earnings vesting, payout threshold/cadence/method, taxes, chargebacks and negative balances.
- KYC data, legal necessity, provider, retention/deletion, reviewer permissions and appeal.
- Call eligibility, concurrency, retries, reconnection window and network-interruption settlement.
- Moderation response targets, escalation, crisis copy, sanctions, appeals, evidence access/retention.
- Account deletion, data export/retention, privacy notices, consent and regional legal obligations.

No audio recording or professional mental-health claim is in scope by default. Crisis resources and emergency escalation content require market-specific verification.

## Host journeys

Host is a proposed role in the shared Flutter app. Public listing does not verify host registration, paid host services, earnings, or KYC. Do not activate those features until Phase 00 closes policy, legal and product decisions. See Phase 07.

## Admin journeys

Current code evidence: admin authorization exists; backend exposes listener KYC status update and report review operations. It does not establish a web UI, full user/host management, payouts, financial adjustment, analytics or audit log. Requested admin capabilities are proposed until approved. See Phase 08.
