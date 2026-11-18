# Connecto — UI/UX Guidelines

**Status:** product-neutral guidance, not a reconstruction of uninspected reference screens. No screenshots/recordings were provided or visually reviewed.

## Brand and design principles

Create original Connecto branding and visual assets; do not reproduce the reference app's protected design/artwork. Warm, calm, trustworthy, non-clinical language. Clearly distinguish real availability from stale state and paid features from free ones if monetization is approved. Never imply therapists or professional care unless verified/authorized.

## Navigation and screen inventory (proposed; validate against reference)

### User surface (Flutter)

Authentication/consent; profile setup/avatar; discovery/search/filters; profile detail; connections; conversation list/chat; incoming/outgoing call; active call; voice room browse/room; history; notifications; wallet/payment only if approved; report/block; privacy/account settings/deletion.

### Host role (same Flutter app)

Host application/status (only if approved); profile/availability; incoming call; active call; earnings/payout history only if financial model approved; notifications and safety. Role switch and host eligibility must be explicit, not hidden.

### Admin web (new project; framework TBD)

MFA sign-in; case/report queue; existing listener review workflow only if product decides to retain it; user/host state, moderation, financial/payout capabilities only as separately approved; audit history. Responsive, accessible, least-privilege views with sensitive fields masked.

## Core UX requirements

- Show privacy explanation before phone/media permissions. Location-based discovery is excluded from initial scope; do not request GPS for discovery unless evidence and separate privacy approval add it later.
- Discovery shows only admin-approved Hosts currently online and available to accept calls. Profile cards show approved fields and current availability; never expose phone/exact coordinates or present stale presence as live.
- Provide clear request/accept/decline, loading, timeout, offline, busy, retry and empty/error states.
- Incoming call UI identifies who is calling using safe profile projection; accept/decline controls accessible and not accidentally triggered.
- Before a paid call, show the Host's approved per-minute price and require explicit caller confirmation. Active call separates local connection status from billable time; show server-confirmed balance/receipt. Never fake connected state or deduct locally.
- Host rate edits must show pending/rejected/approved state; do not publish or bill a proposed rate before admin approval and its effective time. Show the rate selected for a call and keep it fixed for that call.
- In chat/rooms/calls surface block/report with a confirmation and actionable result; do not silently simulate messages or moderation.
- Error copy is non-blaming and avoids leaking account existence/security state.

## Accessibility and localization

Semantics labels, scalable fonts, sufficient contrast, focus order, screen-reader support, large call controls and reduced-motion option. Localize user-facing strings; never concatenate localized money/rate values without formatters. Language support is a public listing claim, but exact languages and translation quality need confirmation.

## Loading, offline and lifecycle

Use skeleton/loading and explicit retry; distinguish empty data from network failure. Preserve draft text securely as policy permits. Handle app background/foreground, call permission and audio focus; do not claim push is reliable until tested. No fabricated fallback profiles or sessions in release builds.

## Design system and acceptance

Define tokens for color/type/spacing/radius/elevation, components and dark/light contrast policy before screen build. Test at supported screen sizes, font scaling, accessibility services, localization and Android lifecycle. Capture approved reference screens and note evidence label in REFERENCE-APP-EVIDENCE before claiming parity.
