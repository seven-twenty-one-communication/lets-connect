# Phase 00 — Reference Evidence and Product Decisions

**Status:** In progress. No product implementation is authorized by this phase document.

### Decision log

| Decision | Owner answer | Status / implications |
|---|---|---|
| Initial launch market | India only | Decided. Do not include other markets in launch requirements; country-specific age, privacy, payments, consumer protection and platform obligations still require validation. |
| Minimum age | 18+ only | Owner decision. Verify legal and Google Play requirements for India before launch; implement a suitable age gate/assurance only after that review. |
| Age assurance | Defer the specific age-assurance method until India legal and Google Play review; minimize age data and avoid document collection unless required. | Owner decision, 2026-11-18. This is not a compliance determination; confirm applicable requirements before launch. |
| Primary connection flow | Direct call option after login, based on selected language/topic choices; no separate friendship request is required before direct calling. | Owner direction interpreted from answer. Language catalog is deferred; whether rooms/chat are available at launch remains unresolved. This is a Connecto-inspired target, not a verified reference-app screen flow. |
| Initial matching preferences | Language and conversation topics. Initial topics are Relationship, Support, Understanding, Marriage, Confidence and Friends; “All” means no topic filter and shows profiles across every topic. | Owner decision, 2026-11-18. Defer the language catalog until reference verification and explicit launch-language approval. Topic editability and matching/ranking rules remain unresolved. The listing screenshots show Personality, Marriage, Confidence and Friends; the owner-approved initial taxonomy replaces Personality with Relationship, Support and Understanding. |
| Direct-call discovery interaction | Show matching profiles; the user chooses whom to call. | Owner decision. Availability behavior, profile fields, and exact match/filter ranking remain unresolved. |
| Location-based discovery | Exclude location/GPS-based nearby matching from initial launch unless verified reference evidence and separate privacy approval justify adding it. | Owner decision, 2026-11-18. No precise or approximate location collection for discovery in initial scope. |
| Call discovery availability | Show only admin-approved Hosts who are currently online and available to accept calls. | Owner decision, 2026-11-18. Presence freshness, race handling and server-side eligibility checks remain to be designed. |
| Voice rooms | Defer from the planned initial scope until the live app is verified. | The public listing description mentions rooms; none of six reviewed listing screenshots shows a room UI. Owner believes the option may not exist. This is unresolved, not a definitive claim that rooms are absent. |
| One-to-one text chat | Defer from the planned initial scope until the live app is verified. | Owner decision. Listing claims broadly mention chat, but the reviewed screenshots do not establish an in-app text-messaging journey. |
| Recharge monetization | Include wallet recharge; UPI/card are intended payment options. Promotional credits may be offered based on a specific event/campaign decision; do not assume every recharge or first recharge earns a bonus. | Owner decision, 2026-11-18. ₹80 paid →106 non-cash credits/coins is an illustrative example only, not a final catalog price or guaranteed offer. Decide event eligibility/credit amount per campaign; India app-store/payment policy/legal review remains required. |
| One-to-one call billing model | Charge credits/coins per minute during calls; first unit is charged at confirmed audio connection, then each additional minute as it starts, excluding audio interruption time. | Owner decision, 2026-11-18. User-facing price catalog, authoritative connection/interruption evidence, grace/finalization, other provider-failure refunds and any later percentage-based Host/platform model remain unresolved. |
| First call charge timing | Debit the first minute when audio connects. | Owner decision, 2026-11-18. This may result in a one-minute charge for a call ending sooner; failure/refund exceptions and authoritative connection evidence remain unresolved. |
| Unconnected call handling | If a call never reaches a confirmed audio connection, neither the user nor Host is charged/credited. | Owner decision, 2026-11-18. Define authoritative provider/server connection evidence and connected-call network interruption/refund behavior before implementation. |
| Short connected call | Once audio connection is confirmed, the first-minute user charge and matching ₹2 Host earning apply even if the call ends before 60 seconds. | Owner decision, 2026-11-18. Applies to a normal early hang-up; network-failure, provider-error and later refund exceptions remain unresolved. |
| Network interruption metering | Pause further billing and matching Host earnings while audio is interrupted, resuming when audio resumes. The already-triggered first-minute user charge and ₹2 Host earning stand once connection is confirmed. | Owner decision, 2026-11-18. Exact authoritative interruption/resumption evidence, grace/finalization window and refund rules for other provider failures remain unresolved. |
| Subsequent call-minute rounding | Charge each additional minute when it starts after the first connected minute. Paused interruption time does not advance billable call time. | Owner decision, 2026-11-18. Server/provider evidence must define billable connected time; exact event handling and reconciliation remain to be designed. |
| Paid-call consent | Before placing a paid call, show the Host's approved per-minute price and require the caller's explicit confirmation. | Owner decision, 2026-11-18. UI must disclose the charge unit and applicable approved terms; final price catalog remains unresolved. |
| Host user-facing rate setting | Host proposes a rate within admin-configured limits; an admin must approve it before it is shown or used for billing. | Owner decision, 2026-11-18. Numeric bounds, approval workflow details, change/effective-time rules remain unresolved. |
| Rate changes during calls | Snapshot the approved rate when the call starts; a later rate change applies only to new calls. | Owner decision, 2026-11-18. Define effective time and caller/Host display of changes before implementation. |
| Host earnings | Initial Host earning is ₹2 INR cash per user-billed minute unit, including the connected first minute; later user prices and any percentage-based share may be changed. | Owner decision, 2026-11-18. Payout schedule/threshold/provider, eligibility, tax/accounting and applicable verification requirements remain unresolved and require India-specific review. |
| Host payout initiation | Host requests payout of accrued earnings, subject to eligibility and review. | Owner decision, 2026-11-18. Minimum threshold and processing cadence/delivery timing are deferred until payout provider/compliance review. Provider, review rules, fees and required verification remain unresolved. |
| Host payout rail | UPI is the intended initial Host payout method; choose the specific provider after India compliance review. | Owner decision, 2026-11-18. Provider coverage, fees, settlement behavior and fallback remain unresolved. |
| Promotional credits | Decide whether to award credits for each specific event/campaign; there is no blanket once-per-account or always-on first-recharge bonus rule yet. | Owner decision, 2026-11-18. Configure and approve eligibility, amount, limits, expiry and accounting per campaign before launch. |
| Host activation | Require a Host application and admin approval before their profile is discoverable or can accept calls. | Owner decision, 2026-11-18. Application fields, review service levels, rejection/appeal and evidence requirements remain unresolved. |
| Host approval criteria | Initial review covers the Host application/profile and required safety/terms consent; identity documents are not required unless India law or the payout provider requires them. | Owner decision, 2026-11-18. Exact application fields, policy text, reviewer checklist, service level, rejection and appeal remain unresolved. |
| Host identity documents | Do not require identity-document KYC by default; collect documents only if India law or the selected payout provider requires them. | Owner decision, 2026-11-18. Obtain qualified India legal and payout-provider review before defining evidence collection or activating payouts. |
| Authentication and OTP provider | Prefer Firebase Phone Authentication for India OTP sign-in. | Owner preference, 2026-11-18. Validate India delivery eligibility, current SMS charges, quotas, app verification requirements, and project billing before production. Backend must verify Firebase ID tokens; Firebase client success alone must not grant an application session. |
| Push notifications | Use Firebase Cloud Messaging (FCM) for app push notifications. | Owner decision, 2026-11-18. FCM is for notifications, not a substitute for OTP authentication. Device-token registration, consent, payload minimization, delivery retries, and invalid-token cleanup remain implementation requirements. |

## 1. Phase objective

Turn public reference claims and the owner's product brief into a signed, traceable scope baseline. Prevent implementation from guessing journeys, money rules, age/privacy obligations or provider behavior.

## 2. Business requirements

Capture user, host, room, audio, payment, moderation and admin requirements in `../BUSINESS-REQUIREMENTS.md`; classify each exactly as confirmed, screenshot-observed, not publicly verifiable/investigate, or proposed/approval required. Public listing presently confirms only broad friendship, one-to-one voice, room, avatar, language and safety claims.

## 3. Scope and exclusions

In scope: listing/source capture, authorized walkthrough if available, market/legal questions, stack confirmation, decision log and approval. Out: coding, scraping private data, reverse engineering, copying protected assets, assuming paid calls/KYC/location, or recording.

## 4. Dependencies on previous phases

None. Blocks implementation phases 01–09 except approved discovery/architecture exploration.

## 5. Architecture and design decisions

Retain Flutter Android, Spring Boot modular monolith, PostgreSQL and current Agora integration baseline. One Flutter app for User/Host; separate admin web app. Kafka and service decomposition are not default. Record each approved change as ADR with options, evidence, consequences, approver and date.

## 6. Database entities and relationships

No product schema change. Proposed decision records: `product_decisions` (key, chosen value, rationale, approval, effective date) and evidence register. Store decisions in docs/issue system, not user DB, unless product explicitly needs runtime configuration.

## 7. Database migration requirements

None. Any runtime-config table later needs migration, authorization and audit; do not make DDL changes in this phase.

## 8. Backend implementation tasks

Inspect controllers, entities, services, config, dependency manifests, tests and profiles; record what exists versus what is exercised. Resolve discrepancies between SQL, JPA config and READMEs. No runtime code edits.

## 9. Frontend/mobile implementation tasks

Inspect all Flutter screens/services/models, Android manifest/build settings and dependencies; document placeholders, fake values, unauthenticated calls, UI simulation and platform coverage. No UI implementation.

## 10. API endpoint specifications

No new endpoints. Inventory existing unversioned endpoints and WebSocket destinations into API docs, label them implementation evidence only, and identify authorization/contract gaps. Proposed future APIs remain draft.

## 11. Request and response examples

No live API contract approved. Use redacted/synthetic examples only. Do not copy production personal data or secrets into evidence.

## 12. Validation requirements

Verify listing URL/title/date/claims; note country/version visibility; source every repository fact to path/symbol. Record unavailable evidence. User must approve scope and unresolved decisions before phase 01 begins.

## 13. Authentication and authorization requirements

Use authorized app access only. No account impersonation, access-control bypass, private endpoint probing or collection of other users' data.

## 14. Security considerations

Sanitize screenshots and notes for phone numbers, profile identifiers, payment data and tokens. Do not publish sensitive user evidence. Respect platform terms and copyright; use listing only to understand behavior.

## 15. Error handling

Unavailable listing/screen/flow is recorded as unknown, never inferred. Conflicting sources become an explicit issue with source/version and owner decision.

## 16. Event processing

No product event processing. Maintain a change log for evidence and decisions; do not introduce queue/broker.

## 17. External service integrations

Inventory Firebase Phone Authentication, FCM, Agora, object storage, payment/payout, AWS and observability needs. Mark current provider configuration and production availability separately; do not create credentials in docs.

## 18. Unit testing

No code tests required. Validate evidence taxonomy, decision completeness and traceability references by review.

## 19. Integration testing

No integrations invoked. Confirm only account access/service ownership prerequisites with the user.

## 20. Performance testing

Not applicable before workload and SLO decisions. Record expected initial cohort and concurrency once owner provides them.

## 21. Failure scenarios

No screenshots, app access, market policy, or decision owner response: phase remains partially blocked; downstream work can proceed only on explicitly approved scope and mark unknown behavior as deferred.

## 22. Acceptance criteria

Evidence register has source/date/confidence; each requirement classified; user journeys separated into verified versus unverified; architecture and open decisions have owner disposition; no invented prices/fees/KYC/age/location policy.

## 23. Completion checklist

- [ ] Reference source version and country recorded.
- [ ] Supplied screenshots/recordings examined or absence recorded.
- [ ] Every unknown has investigation owner/action or explicit deferral.
- [ ] Stack and current gaps verified against code.
- [ ] Product scope and decisions approved.
- [ ] Traceability and development status updated.

## 24. Deployment considerations

None; phase is documentation and decision work only.

## 25. Documentation update requirements

Update evidence register, business requirements, project overview, architecture ADRs, traceability and development status. Preserve existing docs; identify contradictions, do not silently rewrite them.
