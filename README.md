# LetsConnect — Voice Calling & Emotional Connection Platform

A production-ready social friendship and real-time audio calling platform built with **Flutter** (Android) and **Spring Boot** (Java 21), featuring ultra-low-latency VoIP calling, real-time WebSocket signaling, and per-minute coin billing.

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
│   ├── pom.xml
│   └── src/main/resources/application.yml
├── letsconnect-mobile/           # Flutter Android Application (Dart / Material 3)
│   ├── lib/
│   │   ├── models/               # Category, Listener, CallSession data models
│   │   ├── screens/              # Login, Home Discovery, Call HUD, Incoming Call, Chat
│   │   ├── services/             # ApiService, AgoraAudioService, SessionStore
│   │   ├── theme/                # AppColors (Cozy dark theme)
│   │   └── main.dart
│   └── pubspec.yaml
├── letsconnect-database/         # PostgreSQL Schema & Seed Data
│   ├── schema.sql                # Production schema (Wallets, Sessions, KYC, Helplines, etc.)
│   └── seed_data.sql             # Categories, Google Play coin packs, Helplines, Gifts
├── docs/                         # Architecture, API specifications, and Phase documentation
│   └── LetsConnect/              # Full production roadmap and technical documents
└── docker-compose.yml            # Local PostgreSQL 16 & Redis 7 container orchestration
```

> **Note on Flutter SDK**: The Flutter SDK is kept externally at `C:\Apoorv_Project_Data\flutter-sdk` to keep the application repository lightweight and clean.

---

## 🚀 Running the Project

### 1. Database & Cache (Docker)
```bash
docker compose up -d
```
*Starts PostgreSQL on port `5432` with preloaded schema & seed data, and Redis on port `6379`.*

### 2. Spring Boot Backend
```bash
cd letsconnect-backend
mvn clean compile
mvn spring-boot:run
```
*API runs on `http://localhost:8080`*

### 3. Flutter Mobile App
```bash
cd letsconnect-mobile
flutter pub get
flutter run
```
