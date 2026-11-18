# 1-on-1 Emotional Support & Voice Calling App

A modern, anonymous voice companionship platform built with **Flutter** (Android) and **Spring Boot** (Java 21), powered by **Agora RTC** for low-latency VoIP calling and real-time per-minute coin billing.

---

## 📁 Project Structure

```
connecto-calling-app/
├── database/
│   ├── schema.sql         # Production PostgreSQL schema (Wallets, Sessions, KYC, Helplines)
│   └── seed_data.sql       # Categories, Google Play coin packs, Crisis helplines
├── backend/               # Spring Boot 3.3.5 (Java 21)
│   ├── src/main/java/com/connecto/app/
│   │   ├── config/        # Security & CORS configuration
│   │   ├── controller/    # CallController, ListenerController, CategoryController, WalletController
│   │   ├── model/         # User, ListenerProfile, Category, Wallet, CallSession
│   │   ├── repository/    # Spring Data JPA repositories with pessimistic locking
│   │   └── service/       # AgoraTokenService, CallBillingService (60s atomic loop), WalletService
│   ├── pom.xml
│   └── src/main/resources/application.yml
├── mobile/                # Flutter Mobile Application
│   ├── lib/
│   │   ├── models/        # CategoryModel, ListenerModel, CallTokenModel
│   │   ├── screens/       # HomeScreen (Discovery feed), CallScreen (Active voice call)
│   │   ├── services/      # ApiService, AgoraAudioService (WebRTC audio engine)
│   │   ├── theme/         # AppColors (Cozy dark night theme)
│   │   └── main.dart
│   └── pubspec.yaml
└── docs/
    └── audio_sdk_cost_and_architecture.md
```

---

## 🚀 Running the Project

### 1. Database Setup
```bash
# In PostgreSQL:
createdb connecto_db
psql -d connecto_db -f database/schema.sql
psql -d connecto_db -f database/seed_data.sql
```

### 2. Spring Boot Backend
```bash
cd backend
mvn clean compile
mvn spring-boot:run
```
*API runs on `http://localhost:8080`*

### 3. Flutter Mobile App
```bash
cd mobile
flutter pub get
flutter run
```

---

## 🏷️ Rebranding Note
The codebase is structured to be completely brand-neutral. Once you finalize your app name (e.g., *Sukoon*, *Suno*, *Mitra*, etc.), you only need to update the display strings in `pubspec.yaml` and `main.dart`.

## Technical Blueprint

The evidence-labelled product requirements, architecture, API/database/security contracts, and implementation phase specifications are in [docs/connecto](docs/connecto/README.md). These documents distinguish verified public claims from unverified behavior and proposed features; they do not imply that planned functionality is already implemented.
