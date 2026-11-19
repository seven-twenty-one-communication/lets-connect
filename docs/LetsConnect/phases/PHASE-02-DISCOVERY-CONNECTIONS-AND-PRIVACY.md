# Phase 02 — Discovery, Direct Calling and Privacy

**Status:** Not started; initial discovery scope is owner-approved. Exact matching/ranking and language catalog require decisions/reference investigation.

## 1. Phase objective

Deliver privacy-controlled discovery of admin-approved Hosts who are online and available, allowing a user to select a profile and initiate a direct call without a friendship-request prerequisite.

## 2. Business requirements

BR-009; reference screenshots observe profile cards and topic choices but do not verify the full discovery flow. Initial topic catalog is Relationship, Support, Understanding, Marriage, Confidence and Friends; “All” means no topic filter. Defer the language catalog until reference verification and owner approval; do not guess supported languages. Location-based discovery is excluded from initial scope.

## 3. Scope and exclusions

In: approved profile projection, paginated discovery filtered by language/topic, online/available Hosts only, block-aware visibility and privacy settings. Selecting a Host proceeds to the direct-call flow; no friendship request is required before calling. Out: location/GPS discovery, friend-request flow as a prerequisite to calling, ranking by sensitive traits, friend recommendations/AI, monetized ranking, unapproved gender/age filters.

## 4. Dependencies on previous phases

Phase 00 approval of discovery scope, initial topics, consent and age/market; Phase 01 authentication/profile/privacy schema; Phase 04 call initiation contract.

## 5. Architecture and design decisions

Discovery module queries indexed public Host projections; call module owns call initiation and eligibility. Use cursor pagination and deterministic sort; no client-supplied actor. Only approved, online/available Hosts are returned; recheck eligibility atomically when initiating the call. “All” omits topic filtering. Location stays disabled unless separately approved. Block policy is shared authorization service.

## 6. Database entities and relationships

`host_profiles` references a user and approval state; profile topic associations and language preferences support filters; `blocks` are a directed user pair; optional `discovery_preferences` stores selected language/topic. No connection table is required for this direct-call flow. No raw location by default.

## 7. Database migration requirements

Add indexes for approved Host status, availability, languages and topics, plus block lookups. Do not migrate inferred friendship/connection state. If location is separately approved later, require its own opt-in consent/expiry record and geospatial migration plus deletion plan.

## 8. Backend implementation tasks

`GET /api/v1/discover?cursor=&language=&topic=` returns only approved, online/available Host profiles matching the selected filters. `topic=all` omits topic filtering. `GET /api/v1/profiles/{id}` returns an authorized public projection; `POST /api/v1/users/{id}/blocks` creates a block. Direct call initiation is specified in Phase 04. Do not add friendship request endpoints for this initial direct-call scope.

## 9. Frontend/mobile implementation tasks

Discovery list/profile detail, language/topic controls, availability and empty/loading/error states, privacy settings and block action. Never seed fake listeners or expose stale online data as current.

## 10. API endpoint specifications

`GET /api/v1/discover?cursor=&language=&topic=`; `GET /api/v1/profiles/{id}`; `POST /api/v1/users/{id}/blocks`.

## 11. Request and response examples

```http
GET /api/v1/discover?language=hi&topic=relationship&cursor=<opaque>
Authorization: Bearer <token>
```
```json
{"items":[{"userId":"uuid","displayName":"Mira","avatar":{"assetId":"uuid"},"languages":["hi"],"topics":["relationship"],"availability":"AVAILABLE"}],"nextCursor":null}
```
IDs and fields shown are illustrative. Never return stale/unavailable Hosts as call-eligible; backend must recheck status on call initiation.

## 12. Validation requirements

Allowlisted filters and sort; bounded page size/query; valid language/topic IDs; “All” means no topic filter; visibility, block, Host approval, availability and age eligibility enforced on server; cursor integrity.

## 13. Authentication and authorization requirements

Authenticated discovery unless owner approves public browsing. Authenticated users may discover public Host projections and initiate calls only through the call module. Block immediately denies discovery visibility and calls according to approved policy. Conceal private profile existence where needed.

## 14. Security considerations

Prevent scraping/enumeration with rate limits, minimal profile fields, abuse detection and pagination; do not expose phone, exact location, private age/date of birth or moderation flags. Location permission is not consent for public display.

## 15. Error handling

400 malformed filter; 401 session; 403 policy; 404 hidden/absent profile; 409 availability changed before call initiation; 429 scraping limit. Distinguish no results from unavailable service.

## 16. Event processing

Host approval, profile/language/topic updates, availability changes and block events update discovery projections; outbox and dedupe. Call lifecycle events are owned by Phase 04. Preserve ordering by aggregate ID.

## 17. External service integrations

No external discovery service required. If location or search engine approved, document provider/data residency, deletion and fallback first; no location SDK until decision.

## 18. Unit testing

Approval/availability filtering, block/visibility predicates, field projection, topic/language filter validation, “All” semantics, cursor tampering and age/privacy rule.

## 19. Integration testing

PostgreSQL indexes and pagination, cross-user access, block propagation, current availability recheck before call initiation and API contract.

## 20. Performance testing

Load indexed discovery with realistic cardinality and filter selectivity; set latency/p95 and traffic target with owner. Ensure no unbounded full-table fetch.

## 21. Failure scenarios

Availability changes between discovery and call initiation, stale cursor, index/search projection lag, block event delivery failure, deleted profile during paging. Call initiation must recheck current Host approval/availability and block state even if the discovery projection is delayed.

## 22. Acceptance criteria

Only approved, online/available Host profiles and approved public fields are returned; topic/language filters and “All” behave as approved; user can choose a profile and initiate a call without a friendship request; blocked users cannot discover/contact each other; pagination stable; no location leakage; real data/error states only.

## 23. Completion checklist

- [ ] Discovery filters, topic catalog and direct-call selection behavior implemented.
- [ ] Data minimization and location decision recorded.
- [ ] Migrations/indexes and REST contracts tested.
- [ ] Block enforcement shared across domains.
- [ ] Load, scraping and privacy tests pass.

## 24. Deployment considerations

Migrate indexes online where supported; feature-flag discovery filters; monitor query latency and abuse; maintain non-location fallback.

## 25. Documentation update requirements

Update business/API/database/security/UI, evidence register, traceability and status with observed versus proposed flow.
