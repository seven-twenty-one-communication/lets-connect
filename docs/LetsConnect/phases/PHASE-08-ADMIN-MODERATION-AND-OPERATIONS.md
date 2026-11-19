# Phase 08 — Admin, Moderation and Operations

**Status:** Not started. Existing backend has admin APIs for listener KYC status and reports; no admin web client was found. Other requested capabilities are proposals.

## 1. Phase objective

Provide audited, least-privilege moderation and operations workflows, with a responsive admin web client if approved.

## 2. Business requirements

BR-006 confirmed only as public safety claim. User's requested user/host/KYC/payment/payout/analytics/admin modules remain proposed. Define moderation policy, service levels, appeals, evidence access, role scopes and business continuity.

## 3. Scope and exclusions

In: current backend-supported report and listener status workflows after verification; case queue, audit, account action only if approved. Out: unrestricted user data, audio monitoring/recording, arbitrary ledger edits, automated bans, exposing KYC to general admins.

## 4. Dependencies on previous phases

Phase 00 policy and admin roles; Phase 01 admin authentication; Phases 02–07 entities/actions being administered; Phase 06 ledger controls for financial functions.

## 5. Architecture and design decisions

Separate responsive web client; frontend framework TBD; backend admin module with MFA/SSO decision, scoped permissions, reason codes, audit events and break-glass process. Admin UI does not directly access DB. Current endpoints are not a complete admin product.

## 6. Database entities and relationships

`admin_roles/permissions`, `moderation_cases`, `moderation_actions`, `audit_events`, `verification_cases`, `payout_reviews`, `config_revisions`; references to users/calls/reports, not copied sensitive payloads. Audit immutable and access-controlled.

## 7. Database migration requirements

Add admin role scopes, audit case/action schema and indexes; preserve existing report/KYC records with explicit status mapping. No migration that grants all existing admins new broad permissions. Backfill actor provenance only when supported.

## 8. Backend implementation tasks

Admin login/session, permission-scoped case queues/detail, block/suspend/restore if approved, report actions, listener review if retained, PII masking, audited reason/evidence refs, config change with approval and audit. Paginate DB queries; current report implementation loads all reports and must not be copied as scaling design.

## 9. Frontend/mobile implementation tasks

New responsive admin app: secure auth, accessible queue/filter/detail, confirm dangerous action, show only fields allowed by role, audit timeline, stale data/version warnings. Mobile app exposes report/block and clear outcome; no KYC reviewer screens in user app.

## 10. API endpoint specifications

Current code: `/api/admin/listeners/{userId}/kyc`, `/api/admin/reports`, `/api/admin/reports/{reportId}` (unversioned, verify authorization/tests). Proposed v1: `/api/v1/admin/cases`, `/users/{id}/status`, `/reports/{id}/actions`, `/audit-events`; permission scopes explicitly enumerated.

## 11. Request and response examples

```json
PATCH /api/v1/admin/reports/uuid
{"action":"REVIEWED","reasonCode":"POLICY_REVIEWED","caseNote":"<restricted>"}
// 200 {"reportId":"uuid","state":"REVIEWED","auditEventId":"uuid"}
```
Example is a draft; action enums, note access and legal retention require policy approval.

## 12. Validation requirements

Valid case state transition, reason required, evidence access purpose, role scope, dual approval for configured risky actions, pagination bounds and config effective date. Prevent self-review where applicable.

## 13. Authentication and authorization requirements

MFA/SSO for admin; least privilege by action/data field; re-auth for high-risk actions; separate support/moderator/finance/security roles. Every endpoint server-side authorization; no frontend-only checks.

## 14. Security considerations

Admin account takeover, insider access, data exfiltration, evidence leakage and financial fraud. Mask PII; audit view and export; rate-limit; session expiry; alert unusual access; no direct object storage public links.

## 15. Error handling

404 concealed; 403 scope denial; 409 stale case/version conflict; 422 invalid action; 429; audit-write failure blocks privileged mutation. UI does not display success unless backend commits and audit exists.

## 16. Event processing

Moderation action + audit event committed atomically; notification and downstream suspension via outbox; dedupe actions; audit append-only. Config change events include before/after hash but not secrets.

## 17. External service integrations

SSO/MFA, document verification, payment/payout provider console APIs and alerting TBD. No vendor selected; minimize third-party data disclosure. Admin web hosted privately with TLS and CSP.

## 18. Unit testing

Permission matrix, report state transitions, masking, audit generation, approval threshold and action idempotency.

## 19. Integration testing

Cross-role API denial, audit atomicity, web UI workflows, stale updates, KYC private-object access, report/block behavior, finance role segregation.

## 20. Performance testing

Queue queries and case detail under expected volume; bounded pagination; avoid full-table loads; define case response SLO with operations owner.

## 21. Failure scenarios

Admin session theft, reviewer outage, audit store unavailable, duplicate action, parallel case decisions, provider evidence inaccessible, mistaken suspension. Fail closed on privileged write and provide documented appeal/reversal.

## 22. Acceptance criteria

Every privileged read/action authorized and auditable; no sensitive data to wrong role; users can report/block; admin outcome reflected; no unsupported feature presented as existing reference behavior.

## 23. Completion checklist

- [ ] Admin features and role matrix approved.
- [ ] Current endpoints reviewed for auth, pagination and audit gaps.
- [ ] Admin frontend stack selected.
- [ ] MFA, audit and data masking tested.
- [ ] Moderation and appeal runbooks ready.

## 24. Deployment considerations

Private admin access path, MFA, restricted origins, audit retention, alerting, separate release and rollback. Seed no shared/default admin credentials.

## 25. Documentation update requirements

Update business/API/security/database/UI/testing/evidence/traceability/status and moderation operations runbook.
