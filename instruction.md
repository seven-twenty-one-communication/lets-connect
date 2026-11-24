# CONNECTO — PRODUCT ARCHITECTURE & DEVELOPMENT INSTRUCTIONS (`instruction.md`)

> **Document Type:** Production Architecture, Product Specification & GitHub Copilot Development Instructions  
> **Target Audience:** GitHub Copilot, Autonomous Agents, Full-Stack Engineers, DevOps  
> **Repository:** `seven-twenty-one-communication/lets-connect`  
> **Reference Portal Analysis:** `https://connecto.live/expert` (Benchmarked & Adapted for Connecto)

---

## 1. Product Vision & Positioning

**Connecto** is a next-generation real-time social audio friendship and creator companionship platform. It allows users to discover genuine people across India, initiate 1-on-1 direct voice conversations, and build meaningful relationships without the superficiality of swipe-based dating or the clinical framing of therapy apps.

### Core Roles (One Account, Dual Capabilities):
- **Caller:** A person looking for a companion to talk to about movies, gaming, late-night thoughts, life experiences, or casual chit-chat.
- **Listener (Expert / Companion):** A creator/listener available to receive calls, provide engaging conversation, and earn money based on talktime.

### Important Distinction from Traditional Apps:
1. **NOT a Dating App:** No swiping, no mutual match requirement. Direct call initiation to any online listener.
2. **NOT a Mental Health / Psychiatric Clinic:** Exclusively focused on social connection, everyday friendship, banter, and entertainment. No clinical or psychiatric diagnosis.
3. **100% Privacy by Design:** 3D/AI avatars ensure neither host nor caller ever reveals personal cameras or real photos unless they choose to.

---

## 2. Benchmark Analysis: `connecto.live/expert` Learnings & Strategic Adaptations

We inspected the live market leader portal `https://connecto.live/expert`. Here is what we adopt and how we adapt it:

| Feature Dimension | Benchmark (`connecto.live/expert`) | Connecto Strategy ("Similar but Superior") |
| :--- | :--- | :--- |
| **Positioning** | "Transformative financial journey for creators" | Empowering youth/creators with flexible earnings through social chit-chat and active listening. |
| **Web Lead Onboarding** | Simple 5-field form on web (`fName`, `phoneNumber`, `gender`, `language`, `time`) | Fast web lead capture **+** instant phone OTP verification **+** optional 15-second voice intro sample. |
| **Regional Languages** | Hindi, Bengali, Telugu, Tamil, Kannada, Malayalam, Punjabi, Odia, Gujarati, Other | Same 10 Indian regional languages supported across database enums, web portal, and mobile filters. |
| **Availability Slots** | 4 blocks: 12am-6am, 6am-12pm, 12pm-6pm, 6pm-12am | Same 4 standard 6-hour availability shifts + live real-time toggle (Online / Offline / On-Call). |
| **Identity & Privacy** | AI-generated 3D avatars | Pre-generated expressive 3D avatars assigned at registration for zero camera stress and anti-harassment safety. |
| **Pricing & Control** | Platform-fixed rates | **Listener controls their own calling rate (coins/minute)**, subject to platform configurable safety boundaries. |
| **Monetization & Payouts** | UPI payouts in 3-5 days; 1% TDS with PAN, 20% without PAN | Instant or scheduled UPI payouts (RazorpayX/Cashfree abstraction) + automated TDS ledger calculations. |
| **Platform Roles** | Separate expert application | **One account, dual roles**: caller can become a listener from web or mobile without registering twice. |
| **Gamification** | Weekly creator talktime leaderboards | Weekly Creator Talktime Leaderboard with bonus prize pools funded by platform commission reserves. |

---

## 3. Technology Architecture & Monolith Structure

Connecto is built as a maintainable, high-performance modular monolith.

```
connecto-calling-app/
├── letsconnect-backend/          # Java 21, Spring Boot 3.3.5, Gradle, Flyway, PostgreSQL, Redis, Agora RTC
├── letsconnect-mobile/           # React Native (v0.75+), TypeScript, Agora RTC, React Navigation
├── letsconnect-admin/            # Next.js 14 (App Router), TypeScript, Tailwind CSS (Portal + Admin)
├── letsconnect-database/         # Standalone schema and seed reference files
└── .github/workflows/ci.yml      # Automated GitHub Actions CI for all 3 apps
```

### Core Technologies:
- **Backend:** Java 21 (Temurin), Spring Boot 3.3.5, Gradle 8.10.2 (Kotlin DSL), Flyway Migration, Spring Data JPA, Spring Security (Stateless JWT HS256), PostgreSQL 16, Redis 7 (Lettuce), Agora AccessToken2 (Token007).
- **Web Portal & Admin:** Next.js 14 (React 18), TypeScript, Tailwind CSS, Lucide React, Axios.
- **Mobile:** React Native 0.75.4, TypeScript, React Navigation v6, `react-native-agora` 4.4+, AsyncStorage.

---

## 4. Web Listener / Expert Portal Specification (`/expert`)

The Next.js application (`letsconnect-admin` or dedicated web front) hosts the public-facing Expert / Listener Acquisition portal at `/expert`.

### 4.1 Page Layout & Visual Identity
- **Color Theme:** Dark modern aesthetic
  - Background: Deep Navy `#0b132b` / `#0d172e`
  - Cards & Modals: `#152238` / `#1c2a4a`
  - Primary Accent: Royal Blue `#4A7BF7` (Hover: `#3b69e0`)
  - Accent Highlight: Coral Pink / Red `#E02D51`
  - Typography: Crisp white `#FFFFFF` (Headings) and `#D1D5DB` (Body)
  - Borders: `#2a3b5c`

### 4.2 Hero Section & Content Copy
- **Badge:** `🚀 Join 10,000+ Companions & Creators across India`
- **Headline:** `Turn Your Conversations into Real Income.`
- **Subtitle:** `Earn up to ₹25,000/month by talking to people who need a friend, a mentor, or a late-night companion. 100% audio-only, 100% private with AI avatars.`
- **Value Props (3-Pillar Cards):**
  1. **100% Audio & Anonymous:** No video calls. Your real face and phone number are never shared.
  2. **Set Your Own Rate:** Choose how many coins/rupees you earn per minute of connected talktime.
  3. **Direct UPI Payouts:** Weekly payouts credited directly to your Google Pay, PhonePe, or Paytm UPI ID with automated TDS compliance.

### 4.3 Web Lead Capture Form (`ExpertLeadForm`)
The form captures qualified leads with strict client & server validations:

```typescript
export interface ExpertLeadRequest {
  fullName: string;          // 3-30 chars, letters & spaces only
  phoneNumber: string;       // 10 digits Indian mobile format: ^[6-9]\d{9}$
  gender: 'FEMALE' | 'MALE' | 'OTHER';
  primaryLanguage: string;   // 'Hindi' | 'Bengali' | 'Telugu' | 'Tamil' | 'Kannada' | 'Malayalam' | 'Punjabi' | 'Odia' | 'Gujarati' | 'English' | 'Other'
  secondaryLanguages?: string[];
  preferredSlot: 'SLOT_00_06' | 'SLOT_06_12' | 'SLOT_12_18' | 'SLOT_18_24';
  requestedRatePerMin?: number; // Optional initial rate suggestion (e.g. 10 coins/min)
  bio?: string;
}
```

#### Validation Rules:
1. `fullName`: Regex `^[a-zA-Z\s]{3,30}$`. No special characters or numbers.
2. `phoneNumber`: Exactly 10 digits, starting with 6, 7, 8, or 9 (`^[6-9]\d{9}$`).
3. `gender`: Required selection (`Female`, `Male`, `Other`).
4. `primaryLanguage`: One of the 10 supported regional languages.
5. `preferredSlot`:
   - `12:00 AM – 06:00 AM` (Late Night Owl)
   - `06:00 AM – 12:00 PM` (Early Bird)
   - `12:00 PM – 06:00 PM` (Afternoon Banter)
   - `06:00 PM – 12:00 AM` (Prime Time Evening)

### 4.4 API Integration Endpoint:
- **Route:** `POST /api/v1/user/web/expert-leads`
- **Access:** Public (Rate-limited via Redis bucket: 5 requests per IP per hour).
- **Backend Handler:** `WebLeadController.java` → creates an entry in `expert_leads` table, notifies the admin team, and triggers a WhatsApp/SMS greeting to the applicant.

---

## 5. Creator Monetization, Payouts & Tax (TDS) Compliance

### 5.1 Pricing & Economy Model
- **Virtual Coin Currency:** 
  - Standard user exchange rate: `1 Coin ≈ ₹1.00`.
  - Listeners set their rate in coins per minute (e.g., `10 Coins/min = ₹10/min`).
  - Listeners can adjust rates from their mobile dashboard between configurable bounds (e.g., `Min: 5 Coins/min`, `Max: 50 Coins/min`).

### 5.2 Double-Entry Ledger Principles
- **No mutable balance as source of truth.**
- Every coin movement produces two ledger entries (Debit and Credit) with an immutable `LedgerTransaction` reference.
- **Platform Commission:** Configurable via environment `PLATFORM_COMMISSION_PERCENTAGE` (default `40%`). Never hardcoded.
  - On a 10-coin minute: User Wallet debited 10 coins; Listener Earnings credited 6 coins (60%); Platform Revenue credited 4 coins (40%).

### 5.3 Indian Tax Compliance (TDS Section 194-O)
Connecto complies with Indian tax requirements for digital marketplace facilitators:
1. **PAN Verification:** Listener submits PAN card number during payout profile setup.
2. **PAN & UPI Name Matching:** Listener's beneficiary name on UPI must match PAN name.
3. **TDS Rates:**
   - **1% TDS** if valid PAN is linked and verified.
   - **20% TDS** (under Section 206AA) if PAN is missing, unlinked, or invalid.
4. **Form 16A Generation:** System tracks cumulative financial year earnings and outputs quarterly Form 16A cert logs.
5. **Payout Turnaround:** 3 to 5 business days via UPI or IMPS bank transfer.

---

## 6. Real-Time Audio Engine & Safety Controls

### 6.1 Agora RTC Implementation Rules
- **Backend-Only Token Generation:** Agora `AccessToken2` (Token007) is generated exclusively in Spring Boot `AgoraTokenService.java` using `AGORA_APP_ID` and `AGORA_APP_CERTIFICATE`.
- **Never expose Agora App Certificate** in mobile or web code.
- **Dynamic RTC Channel Names:** Generated securely as `call_{callerId}_{listenerId}_{timestamp}`.
- **Ringing & Timeout Handling:**
  - Ringing timeout: 30 seconds.
  - If listener rejects or misses, status transitions to `REJECTED` or `MISSED`.
  - **Zero Billing on Non-Connected Calls:** Wallet is NEVER deducted for ringing, rejected, busy, or network-failed calls.
- **Call Disconnect & Settlement:**
  - When either party hangs up or user balance reaches zero, backend immediately closes Agora channel, calculates connected billable minutes, and executes atomic ledger settlement.

### 6.2 Safety, Moderation & Code of Conduct
- **Zero Tolerance Policy:** Strictly prohibits vulgarity, sexual harassment, hate speech, or soliciting off-platform personal contact information (e.g., sharing personal phone numbers or Instagram handles for off-platform payments).
- **In-App Safety Tools:**
  - One-tap User Block (`user_blocks` table).
  - Categorized Abuse Reporting (`abuse_reports` table).
  - Admin moderation panel with instant listener suspension / ban capabilities.

---

## 7. Database Entities & Flyway Migration Specs

All database schema evolutions must use numbered Flyway migration files (`V<N>__<name>.sql`):

### Key Tables & References:
1. `users`: Core identity (Phone, nickname, 3D avatar URL, languages, role `USER` | `LISTENER` | `ADMIN`, status `ACTIVE` | `SUSPENDED` | `BANNED`).
2. `listener_profiles`: Listener metadata (Bio, voice intro URL, price/min coins, presence `is_online`, `is_on_call`, rating average, KYC & UPI banking details).
3. `expert_leads`: Web acquisition funnel leads (`fullName`, `phoneNumber`, `gender`, `primaryLanguage`, `preferredSlot`, `status`).
4. `wallets`: Dual balance tracking (`coin_balance` for calling, `earned_coins_balance` for withdrawals).
5. `call_sessions`: Authoritative call lifecycle state (`INITIATED`, `RINGING`, `CONNECTED`, `COMPLETED`, `REJECTED`, `MISSED`, `FAILED`).
6. `coin_transactions`: Auditable transaction log with idempotency keys.
7. `payout_requests`: Withdrawal records with UPI details, TDS deductions, and bank transaction UTR references.
8. `abuse_reports` & `user_blocks`: UGC safety and Google Play compliance.

---

## 8. Development Roadmap & GitHub Copilot Instructions

When implementing features, developers and AI agents must follow this sequential phase structure:

```
[Phase 1: Foundation] ────> [Phase 2: Auth & Profiles] ────> [Phase 3: Web Expert Portal]
                                                                     │
[Phase 6: Admin Dashboard] <── [Phase 5: Financial Ledger] <── [Phase 4: Calling Engine]
```

### Phase 2: Authentication & Dual-Role User Profiles
- Implement Phone OTP service with SMS Gateway abstraction (Mock in dev, Firebase/MSG91 in prod).
- Create dual-role switching endpoints:
  - `POST /api/v1/auth/request-otp` & `POST /api/v1/auth/verify-otp`
  - `GET /api/v1/users/me` (returns profile + caller balance + listener profile if enrolled)
  - `PUT /api/v1/users/mode` (toggles active mode between `CALLER` and `LISTENER`).
- Build React Native authentication screens (Phone Input, OTP Verification, Avatar Selector, Language Selector).

### Phase 3: Web Expert / Listener Portal (`letsconnect-admin` / Next.js)
- Build `/expert` responsive landing page with Tailwind CSS matching Connecto design tokens.
- Implement `ExpertLeadForm` with real-time field validation.
- Implement `POST /api/v1/user/web/expert-leads` in backend with database persistence and lead status notifications.
- Add informative policy and knowledge-base pages:
  - `/expert-faq` (How to earn, rate setting, call rules).
  - `/tds-payouts` (TDS rates, PAN linkage, Form 16A FAQ).
  - `/terms` & `/privacy`.

### Phase 4: Discovery & Agora RTC Voice Calling
- Redis-based real-time presence system (`is_online`, `is_on_call`, heartbeat every 30s).
- Listener Discovery API with filters (`category`, `language`, `rate`, `rating`).
- Call Signaling via WebSocket STOMP + FCM background push.
- Agora RTC Token Generation (Backend Token007).
- React Native Call Screen (Ringing animation, Agora audio stream, mute/speaker toggles, live duration timer, end call button).

### Phase 5: Financial Ledger & Billing Engine
- Implement immutable Double-Entry Ledger (`LedgerService.java`).
- Real-time minute billing on call completion.
- Payout request API (`POST /api/v1/payouts/request`) with UPI ID validation and 1% / 20% TDS deduction logic.
- Google Play In-App Purchase verification (`GooglePlayBillingService.java`).

### Phase 6: Admin Moderation & Operations Dashboard
- Listener verification & approval workflow (`/admin/listeners`).
- Web leads review & outreach tracker (`/admin/leads`).
- Financial reconciliation & payout approval (`/admin/payouts`).
- UGC Abuse report investigation & banning controls (`/admin/reports`).

---

## 9. Coding Standards & Commit Hygiene

1. **Strict Clean Code:** No business logic in Controllers. Domain entities must remain encapsulated.
2. **DTO & Validation:** Every request must use strong typed DTOs with `@Valid`, `@NotBlank`, `@Pattern`, or Zod schemas on TypeScript.
3. **No Hardcoded Business Constants:** Platform commission, default rates, and rounding rules must be loaded from configuration properties.
4. **Git Branching Strategy:**
   - Feature branches: `feature/<phase-number>-<feature-name>`
   - Base branch: `develop`
   - Release branch: `main`
   - Commit messages must follow Conventional Commits format (e.g. `feat(portal): add expert lead capture form with validation`).

---
*Connecto Engineering Team — Build with Quality, Privacy, and Scalability.*
