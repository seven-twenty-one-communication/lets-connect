# Connecto — Project Overview

**Status:** Blueprint; not an implementation declaration. **Last reviewed:** 2026-11-18.

## Vision and evidence boundary

Connecto is described publicly as a place to build friendships and have private audio conversations, including one-to-one calls and voice rooms. The project brief targets user discovery, connection, messaging, audio, optional paid features and hosts, and an admin application. Exact reference-app journeys and business rules remain unverified; see [reference evidence](REFERENCE-APP-EVIDENCE.md).

Build Connecto's own brand, visual design, and implementation. Do not copy protected assets, proprietary code, or competitor-specific UI. The Google Play listing is evidence of public claims, not proof that each advertised behavior works or is available in every market.

## Target users and roles

- **User:** creates an account/profile, discovers people, and uses approved communication features.
- **Host:** a role in the same Flutter app for users who opt into host capabilities, if the role and its requirements are approved.
- **Moderator/admin:** uses a separate responsive web client with least-privilege permissions. Existing backend admin scope is limited to listener KYC status and reports.
- **Operator/support:** operational role to be defined; no unrestricted production database access is implied.

## Product modules (target architecture)

Identity and session; profiles and privacy; discovery and connections; messaging; one-to-one audio; voice rooms; notifications; optional wallet/payment/host settlement; safety and moderation; admin; audit and operations. Keep these as modules in one Spring Boot deployment initially. Feature flags or explicit scope decisions must prevent unapproved modules from becoming active products.

## Existing repository baseline

- Flutter Android app in `mobile/`, with Agora RTC dependency and early login/discovery/call/chat screens.
- Java 21 / Spring Boot 3.3.5 backend, JPA, Spring Security, PostgreSQL, STOMP/WebSocket, Redis dependency, and Agora services.
- SQL schema/seeds and Docker Compose for PostgreSQL and Redis.
- Existing backend APIs cover OTP, profiles, listeners, calls, wallet/purchases, chat, rooms, gifts, safety, and limited admin operations.
- Demo IDs/data, mock fallbacks, and simulated flows exist in mobile; production integration and end-to-end behavior are unverified. This repository is a starting point, not a production-ready product.

## Business model

Unresolved. The Play listing does not establish paid calling, subscriptions, host commission, coin purchases, or withdrawals. Existing repository defaults and sample values are not product policy. Phase 00 must record owner-approved currencies, fees, prices, eligibility, refunds, and payout rules before implementing financial flows.

## Scope and roadmap

The proposed development order is: reference and decisions; foundation/identity; discovery/connections; messaging/notifications; one-to-one audio; voice rooms; wallet/payments; host experience; admin/moderation; production readiness. See [DEVELOPMENT-STATUS.md](DEVELOPMENT-STATUS.md) and [phase specifications](phases/).

## Explicit exclusions until approved

No recording, professional mental-health service, unverified paid model, assumed KYC document collection, location collection, specific payout policy, or iOS client. Each requires product/legal/privacy review and a documented decision.

## Definition of a blueprint-complete feature

Requirements have evidence classification and traceability; owner decisions are recorded; API, authorization, schema/migration, failure, test, and operational behavior are specified; implementation and verification are evidenced before status becomes complete.
