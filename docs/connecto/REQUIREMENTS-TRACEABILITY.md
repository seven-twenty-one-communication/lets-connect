# Requirements Traceability

**Rule:** evidence classification is distinct from approval and implementation status. A confirmed public claim does not make its detailed workflow confirmed.

| ID | Requirement summary | Evidence class | Source / confidence | Phase | Verification / decision |
|---|---|---|---|---|---|
| REQ-001 | Friendship/meaningful connection positioning | Confirmed from reference app | Play listing, marketing claim | 00, 02 | Investigate connection mechanics |
| REQ-002 | Private one-to-one audio | Confirmed from reference app | Play listing | 04 | Verify lifecycle and provider |
| REQ-003 | Voice chat rooms | Play listing text claims rooms; live app availability not publicly verified | Play listing description; none of six reviewed listing screenshots shows a room UI; owner doubts the option exists | Deferred pending verification | Verify in live app before including; do not implement based on listing text alone |
| REQ-004 | Generated avatars | Confirmed from reference app | Play listing | 01 | Verify generation/editing/privacy |
| REQ-005 | Regional languages | Confirmed from reference app | Play listing | 01–03 | Verify actual languages and filters |
| REQ-006 | Verified community and safety | Confirmed from reference app | Play listing | 01, 08 | Verification meaning and controls unknown |
| REQ-007 | UI/navigation and registration details | Not publicly verifiable — investigation needed | No reviewed screens | 00, 01 | Obtain versioned screenshots/recording |
| REQ-008 | Location-based nearby discovery | Deferred/excluded from initial scope | Owner decision, 2026-11-18; reference behavior unverified | Deferred | Reconsider only with direct reference evidence and separate privacy approval; no GPS collection for initial discovery |
| REQ-009 | Direct-call option after login; select an approved language (catalog deferred), and one of Relationship, Support, Understanding, Marriage, Confidence, Friends or All (no topic filter); browse matching admin-approved online/available Hosts and choose whom to call; no friendship request required first | Owner decision / reference details unverified | User direction, 2026-11-18 | 02, 04 | Verify reference language options and obtain launch-language approval; define profile fields, ranking, presence freshness and server-side call availability |
| REQ-010 | Event-configured wallet promotion (if approved); Host-proposed user price within admin limits approved before display/billing and snapshotted at call start; caller confirms price; first-minute charge/Host earning on confirmed connection; no charge/earning without confirmed connection; pause further billing/earnings during interruption; charge each additional minute as it starts | Owner decision; campaign rules, numeric price limits and interruption/failure evidence unresolved | User direction, 2026-11-18; illustrative ₹80→106 credits/coins, not guaranteed | 06, 07 | Define campaign rules if used, set rate limits/effective-time policy; finalize interruption evidence/grace and other provider-failure refunds; review Google Play/payment rules and India legal obligations |
| REQ-011 | Hosts apply for admin approval; initial review covers application/profile and safety/terms consent, with identity documents only if legally/provider-required; Hosts propose rates within admin limits for approval, initially earn ₹2 INR cash per user-billed minute, and request UPI payouts | Owner decision; exact review checklist, numeric limits, threshold/timing and provider/legal details unresolved | User direction, 2026-11-18 | 07, 06, 08 | Define application/review/appeal workflow and provider; review threshold, timing and document requirements with payout provider/India counsel |
| REQ-012 | Full admin web suite | Proposed improvement — approval required | User brief; repo only has limited APIs | 08 | Approve capabilities and roles |
| REQ-013 | Existing Android Flutter client | Repository fact | `mobile/` and `pubspec.yaml` | 01–09 | Keep framework |
| REQ-014 | Existing Spring Boot/Java/PostgreSQL/Agora | Repository fact | `backend/pom.xml`, config/services | 01–09 | Preserve; provider evaluation only |
| REQ-015 | Double-entry/equivalent auditable accounting | Proposed target requirement | Explicit project brief | 06 | Finance/legal review and invariant tests |
| REQ-016 | No audio recording by default | Proposed safety boundary | Project brief | 04, 05 | Separate review required to add |
| REQ-017 | Admin KYC status and report review APIs | Repository fact, not reference evidence | Backend controllers | 08 | Verify authorization/tests and retention |
| REQ-018 | Demo/fake client fallbacks and incomplete auth path | Repository gap | Current Flutter source | 01, 04, 06 | Remove from release and E2E verify |
| REQ-019 | Initial launch is restricted to India | Owner decision | User confirmed India only, 2026-11-18 | 00, 09 | Validate India-specific legal/store/provider obligations |
| REQ-020 | Minimum age for Connecto is 18+; age-assurance method deferred pending India legal/Google Play review, minimize collected age data | Owner decision | User confirmed 18+ and deferred age-assurance choice, 2026-11-18 | 00, 01, 09 | Confirm applicable India legal/Google Play requirements before selecting age-assurance method; no age/identity documents unless required |
| REQ-021 | One-to-one text chat is deferred until live reference behavior is verified | Owner decision / reference details unverified | User direction, 2026-11-18; listing screenshots do not show messaging flow | Deferred pending verification | Verify in live app before bringing chat into scope |

Update rows when evidence changes; link tests, implementation PR and decision record. Never mark unknown as confirmed based on an implementation assumption.
