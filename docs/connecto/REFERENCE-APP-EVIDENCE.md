# Reference App Evidence Register

## Source

- Google Play listing: [Connecto - Online Friendship](https://play.google.com/store/apps/details?id=com.connecto&hl=en_IN&gl=IN), retrieved/reviewed 2026-11-18.
- Six screenshot assets displayed on the Google Play listing were retrieved and visually reviewed on 2026-11-18. They are promotional listing images, not a screen recording or a complete walkthrough.
- No user-provided screenshots or recordings.
- Repository evidence: source, current README and documentation; these describe this project, not the commercial reference app.

## Evidence labels

1. **Confirmed from the reference app** — listing text makes a public product claim. This does not confirm implementation details or availability by region/version.
2. **Observed from screenshots/recordings** — only direct visual evidence personally inspected; record file/URL, date, screen and observation.
3. **Not publicly verifiable — investigation needed** — listing does not substantiate exact behavior; investigate the live app with authorized test account or user evidence.
4. **Proposed improvement — approval required** — requested product-brief item or architecture recommendation; not represented as reference behavior.

## Confirmed public claims (listing text)

| ID | Claim | Evidence caveat |
|---|---|---|
| E-001 | App positions itself around making friends, meaningful conversations and emotional connection. | Marketing claim; not evidence of moderation efficacy or clinical service. |
| E-002 | Private one-to-one audio/live voice calls. | Exact invitation, lifecycle, pricing and eligibility unknown. |
| E-003 | Voice chat rooms. | Membership, speaker permissions, room capacity and moderation unknown. |
| E-004 | AI-generated avatars / anonymous profiles. | Generation, editing, privacy and moderation flow unknown. |
| E-005 | Regional-language conversations. | Supported languages, filters and translation unknown. |
| E-006 | Verified community and safety controls including block/report/moderation. | Verification method, identity requirements, response time and enforcement unknown. |
| E-007 | Public listing is rated 12+ at retrieval. | Store rating is not Connecto's age policy or legal advice. Verify current rating/target market. |

## Observed from listing screenshots

| Asset order | Visible content | What this supports / does not establish |
|---|---|---|
| 1 | Promotional “Connect Instantly” / “Best Quality Audio Calls” image with a one-to-one call screen. | Supports that the listing promotes direct audio calls; does not show the full call flow. |
| 2 | “Make Friends Online”, “Experts Available 24*7”, featured profile cards and visible “Call Now” actions. | Supports profile browsing and a direct-call affordance in the promotional UI. Availability, role terminology and actual navigation are unverified. |
| 3 | “Safe and Private”, “Verified Experts”, “100% Secure”, and zero-tolerance language. | Marketing claims only; no verification or moderation workflow is shown. |
| 4 | “Anonymous & Real People”, “No personal info needed”, and a one-to-one call screen. | Supports anonymous positioning and one-to-one call presentation; does not establish signup fields or actual privacy guarantees. |
| 5 | “Create your own Avatar”, “AI Generated Avatars”. | Supports avatar creation as a public feature claim; the actual generation/edit flow is not shown. |
| 6 | “Choose to Talk”; visible topic choices “Personality”, “Marriage”, “Confidence”, “Friends” and profile cards with ratings. | Supports topic choice and profile browsing in promotional UI; not proof of exact onboarding/filter logic or ranking. |

The listing's written description mentions voice chat rooms, but none of the six reviewed screenshots shows a room interface or room entry point. This conflict means room availability in the live product is **Not publicly verifiable — investigation needed**; do not infer either presence or absence from marketing copy or screenshot omission alone. The owner believes the room option may not exist and wants the plan to follow the actual app.

## Not publicly verifiable

Registration/OTP and onboarding; required profile data; profile/avatar actions; discovery ranking, search and filters; online/nearby presence and location precision; friend requests/acceptance; chat consent; call/room details and notifications; paid coins/subscriptions/pricing/refunds; host/listener application, earnings or payout; KYC and document collection; admin web product; account deletion and retention; crisis escalation and exact policy. Do not convert these into “confirmed” merely because current repository contains similarly named APIs.

## Owner-approved product decisions (not reference evidence)

These are Connecto product-scope decisions and must not be presented as observed behavior of the reference app:

- Minimum age is 18+; age-assurance method is deferred pending India legal/Google Play review, with age-data minimization and no document collection unless required.
- Exclude location/GPS-based nearby matching from initial launch unless live-reference evidence and separate privacy approval support including it.
- Initial topic choices are Relationship, Support, Understanding, Marriage, Confidence and Friends; “All” is a filter option that shows every topic. This is owner-approved product scope, not a claim that the reference app uses this exact taxonomy.
- Defer the language catalog until reference verification and explicit launch-language approval; do not assume Hindi/English or an India-wide list.
- Decide promotional credits per event/campaign; there is no blanket first-recharge or once-per-account bonus rule. Any campaign must explicitly approve eligibility, amount and accounting before it runs.
- Hosts must apply and receive admin approval before their profiles are discoverable or can accept calls.
- Initial Host approval reviews the application/profile and required safety/terms consent; no identity documents by default unless required by India law or the payout provider. Detailed criteria and review/appeal workflow remain open.
- Do not collect Host identity documents by default; do so only if qualified India legal review or the chosen payout provider requires them.
- Initial Host earnings are ₹2 INR cash per billable call minute, using exactly the user's billed minute units including the connected first minute. User call prices and any percentage-based compensation model may be changed later; subsequent billing boundaries and payout policies remain open.
- Hosts request payouts of accrued earnings, subject to eligibility and review, with UPI as the intended initial payout rail. Minimum threshold, processing cadence/delivery timing and provider selection are deferred pending provider/compliance review; fees are undecided.
- A call with no confirmed audio connection produces neither a user charge nor Host earnings. Connection evidence and post-connect interruption/refund handling remain to be defined.
- Once audio is confirmed, the first user charge and matching ₹2 Host earning apply even if a normal early hang-up ends the call in under 60 seconds; network/provider failure exceptions remain open.
- Pause further billing and matching Host earnings while audio is interrupted, then resume on reconnection. The already-triggered first-minute charge and earning stand after confirmed connection; interruption detection, grace/finalization and other provider-failure refunds remain open.
- After the first connected minute, charge each additional minute as it starts; interruption time does not advance billable call time.
- Before placing a paid call, display the Host's approved per-minute price and require explicit caller confirmation.
- Hosts propose user-facing rates within admin-configured limits; an admin must approve a rate before display or billing.
- Snapshot the approved rate when a call starts; later approved changes affect new calls only.
- Show only admin-approved Hosts who are currently online and available to accept calls; presence freshness and call-race handling remain technical decisions.

## Screenshots and recordings

Listing screenshot assets were reviewed, but **no in-app screenshots or recordings were supplied**. For direct flow verification, record app version, country, screen sequence, visible controls, user action and system response. Avoid reproducing protected assets; screenshots are evidence, not permission to copy branding/design.

## Investigation procedure

Use official listing and an authorized installation/account. Record app version, country, account role/eligibility, permissions and exact reproducible steps. Separate observed facts from interpretation; note unavailable/paywalled/region-locked surfaces. Do not bypass access controls or collect real user data. Update traceability rows with source and date.
