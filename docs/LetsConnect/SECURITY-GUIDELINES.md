# Connecto — Security and Privacy Guidelines

**Applies to:** mobile, backend, admin, CI/CD and operations. This is a target baseline, not a claim that current code complies.

## Authentication and JWT/session lifecycle

- Use Firebase Phone Authentication for the selected OTP direction. Backend must verify Firebase ID tokens with the Firebase Admin SDK or equivalent trusted verifier, including signature, issuer, audience, expiry, and verified phone; never trust a client-supplied UID or a successful client callback alone. Apply abuse controls and generic account responses; never log OTPs or ID tokens.
- Treat FCM as a separate push-notification service. Register only tokens from authenticated devices, protect token updates to the owning account, remove invalid tokens, and keep sensitive content out of push payloads.
- Use a cryptographically strong, rotated signing key stored in secrets manager. Define short access-token lifetime, refresh-token rotation/reuse detection, session revocation and key rollover before implementation.
- Store refresh-token hashes, not raw refresh tokens. Mobile secure storage only; never `shared_preferences` for credentials.
- Reject malformed/expired tokens; verify issuer/audience/algorithm; derive user/roles server-side. Do not trust client-sent `userId` or role.
- Admin authentication should require MFA/SSO appropriate to deployment; separate admin audiences/scopes and privileged session controls.

## Authorization

Default deny. Authorize every REST action, object and field; conceal private existence where useful. Apply authorization again to WebSocket connect, subscribe, and each SEND handler. Restrict resource subscriptions to call/room/conversation participants. Use method-level policy and integration tests against IDOR and role escalation. Host privileges are not automatically admin privileges.

## Secrets and cryptographic data

Use AWS Secrets Manager or selected equivalent; separate environments and least-privilege IAM; rotate and audit access. No secrets in code, APK, YAML, logs, Docker image or docs. TLS for all traffic; managed encryption at rest. Encrypt particularly sensitive identifiers/documents with controlled key access. Do not implement custom cryptography.

## Privacy and data minimization

Collect only approved fields. Phone numbers are private; public profile DTOs expose pseudonymous display information only. Location-based discovery is excluded from initial launch; do not collect GPS for discovery unless direct reference evidence and separate privacy approval justify it. Do not expose exact coordinates or infer presence from stale location. Define consent, retention, deletion/export and legal hold per data class. Avoid recording audio; any recording is prohibited unless a separate legal/privacy/consent review and product approval authorizes it.

## Abuse prevention and user trust

Apply OTP/call/message/report rate limits, spam/fraud detection, blocks across discovery/chat/calls, report workflows and clear moderation escalation/appeals. Connecto's minimum age is owner-decided as 18+ for the India launch; defer the age-assurance method until India legal/Google Play review, minimize age data, and collect age/identity documents only if required. Do not assume 12+ listing content is an age-policy decision for Connecto. Provide accurate non-clinical positioning; no professional mental-health claim. Crisis guidance must be locally reviewed, visible, and not represented as emergency service.

## Secure file uploads and KYC

Do not upload documents until product/legal establishes necessity. If approved: use short-lived server-generated upload intents, private object bucket, MIME/signature/size validation, malware scanning, encryption, least-privilege reviewer access, audited access, short retention and deletion. Never accept arbitrary URLs as proof, serve private documents through public CDN, or place signed URLs in logs.

## Payments and financial controls

Verify purchases server-side with the store/provider; unique provider transaction/event IDs; idempotency; signed webhook validation; ledger transaction; reconciliation. Never trust client balance, price, host rate, elapsed time or receipt fields alone. Separate payment, call and payout permissions. Dual approval for high-risk manual adjustments/configuration and payout where policy requires.

## Web, API and infrastructure

Production origins are explicit allowlists, not `*`. Use CSRF policy appropriate to bearer/session transport; secure headers, TLS, request size/timeouts, dependency scanning, database network isolation, private subnets, least-privilege roles and secret rotation. Patch dependencies, build reproducibly, scan container/SBOM, sign release artifacts.

## Logging, audit and incident response

Redact OTP, phone, tokens, RTC credentials, payment payload, location, message bodies and KYC content. Use correlation ID, actor ID only where policy permits, action, result and resource reference. Immutable audit for admin, account/role changes, financial adjustments and document access. Alert on authentication abuse, settlement mismatch, unusual moderator actions, provider failures and data access anomalies. Maintain incident response, key rotation, notification/legal escalation and restore runbooks.

## Required tests and release gates

Threat model and review before release; automated authorization/IDOR, injection, validation, replay, rate-limit and secret scans; dependency and mobile binary checks; WebSocket cross-user isolation; purchase/call settlement duplication and concurrency; permission/location/privacy leakage; deletion/retention checks. No critical/high unresolved findings without documented risk acceptance from authorized owner.
