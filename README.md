# LetsConnect — Voice Calling & Emotional Connection Platform

A React Native, Spring Boot, PostgreSQL and Agora social friendship and real-time audio calling application.

---

## 📁 Repository Structure

```
lets-connect/
├── letsconnect-backend/          # Spring Boot 3.3.5 (Java 21) REST API & WebSocket Server
│   ├── src/main/java/com/letsconnect/app/
│   │   ├── config/               # Security (JWT), WebSocket (STOMP), Exception Handler
│   │   ├── controller/           # Auth, Calls, Rooms, Listeners, Wallet, Chat, Gifts, Safety, Admin
│   │   ├── model/                # JPA Entities (User, ListenerProfile, CallSession, Wallet, etc.)
│   │   ├── repository/           # Spring Data JPA repositories with pessimistic locking
│   │   └── service/              # Agora Token007, Billing Scheduler, Signaling, In-App Purchase
│   ├── build.gradle.kts
│   └── src/main/resources/application.yml
├── letsconnect-mobile/           # React Native Android Application (TypeScript)
│   ├── src/api/                  # Typed API client and interceptors
│   ├── src/theme/                # Application colors
│   ├── src/types/                # Shared TypeScript types
│   └── App.tsx
├── letsconnect-admin/            # Next.js admin foundation
│   └── app/
├── letsconnect-database/         # PostgreSQL Schema & Seed Data
│   ├── schema.sql                # Production schema (Wallets, Sessions, KYC, Helplines, etc.)
│   └── seed_data.sql             # Categories, Google Play coin packs, Helplines, Gifts
├── docs/                         # Architecture, API specifications, and Phase documentation
│   └── LetsConnect/              # Full production roadmap and technical documents
└── docker-compose.yml            # Local PostgreSQL 16 & Redis 7 container orchestration
```

## 🚀 Running the Project

### 1. Database & Cache (Docker)
```bash
docker compose up -d
```
*Starts PostgreSQL on port `5432` with preloaded schema & seed data, and Redis on port `6379`.*

### 2. Spring Boot Backend
```bash
cd letsconnect-backend
.\gradlew.bat bootRun
```
*API runs on `http://localhost:8080`*

### 3. React Native Mobile App
```bash
cd letsconnect-mobile
npm install
npm run android
```
