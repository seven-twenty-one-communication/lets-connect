# Instructions for AI Coding Agents

## Mandatory reading

Before any work, read this file, `DEVELOPMENT-STATUS.md`, `REFERENCE-APP-EVIDENCE.md`, `REQUIREMENTS-TRACEABILITY.md`, and the relevant phase plus global contracts (API, database, security, calling, wallet as applicable). Inspect actual code before changing it. If docs and code disagree, report the discrepancy and update docs only with evidence/approval; do not assume either is correct.

## Architecture restrictions

- Preserve existing Flutter Android, Java 21/Spring Boot modular monolith, PostgreSQL and Agora baseline. Do not replace frameworks or split services without an approved decision.
- Do not add Kafka or a distributed broker absent a demonstrated requirement and approved ADR.
- Keep Host as a role in the same Flutter app; admin web is separate. No admin stack selected yet.
- Keep APIs versioned under `/api/v1` for new contracts; document adapters/deprecations before changing unversioned routes.
- Use reviewed forward migrations; disable Hibernate schema mutation in production. Never edit a deployed migration or destructively change data without an approved recovery plan.

## Evidence and scope

Label every feature as reference-confirmed, screenshot-observed, not publicly verifiable/investigate, or proposed/approval required. The Play listing's marketing claims do not prove exact UX. Never invent navigation, prices, commission, age rules, KYC, subscription, payout or location behavior. Record assumptions and unresolved questions.

## Security and financial rules

- Never hardcode credentials, tokens, PII or provider secrets; never log OTPs, access/RTC tokens, payment data, KYC documents, exact location or message/audio content.
- Derive actor from authenticated principal; enforce object/field/role auth for REST and every WebSocket action.
- Never trust client wallet, rate, elapsed duration, participant identity or purchase success.
- Financial writes must be transactional and idempotent; use immutable double-entry or approved auditable equivalent. Correct with reversals, not edits. No mutable balance is the sole record.
- No recording, KYC document upload, location collection or paid operation without its documented product/legal approval.

## Implementation workflow

1. Identify exact phase/requirement IDs and files; inspect surrounding patterns and tests.
2. State scope and compatibility impact; avoid unrelated edits.
3. Implement smallest complete vertical slice with validation, authorization, failure handling and observability.
4. Add unit, integration/contract and relevant E2E tests; test PostgreSQL semantics for locking/transactions.
5. Run targeted checks; report commands/results and unresolved risks accurately.
6. Update API/schema/security/phase docs, traceability and DEVELOPMENT-STATUS when verified.

Never simulate a success path in production builds. Errors must remain visible and actionable; do not silently return empty data, demo profiles, mock tokens, fake balances or success-shaped fallbacks.

## Completion and review

Do not claim production-ready or phase complete until acceptance criteria pass and deployment, security, privacy, migration, rollback and operational evidence exists. Preserve backward compatibility; document breaking changes and staged migration. Do not commit unless explicitly requested.
