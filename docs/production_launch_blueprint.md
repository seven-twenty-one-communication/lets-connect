# Connecto-Parity Production Launch Blueprint

This guide documents the exact production configurations, credentials, and deployment steps needed to launch the app on the Google Play Store.

---

## 1. Agora RTC Production Setup
1. Log in to [Agora Developer Console](https://console.agora.io/).
2. Create a new project:
   - **Authentication mechanism:** Select **App ID + App Certificate** (Token007).
3. In `backend/src/main/resources/application.yml` (or via environment variables on your production server):
   ```yaml
   agora:
     app-id: YOUR_ACTUAL_AGORA_APP_ID
     app-certificate: YOUR_ACTUAL_AGORA_CERTIFICATE
     token-expiration-in-seconds: 3600
   ```
4. Voice Optimization Profile:
   - In `AgoraAudioService.dart`, `AudioProfileType.audioProfileSpeechStandard` is configured for crystal-clear human speech with automatic background noise reduction and acoustic echo cancellation (AEC).

---

## 2. Google Play In-App Billing (IAB) Production Setup
1. In the **Google Play Console**, go to **Monetize > Products > In-app products**.
2. Create the exact matching product IDs:
   - `com.connecto.coins_50` - ₹49
   - `com.connecto.coins_120` - ₹99
   - `com.connecto.coins_350` - ₹249
   - `com.connecto.coins_800` - ₹499
3. In Play Console, go to **Monetization setup > Licensing** and copy the **Base64-encoded RSA public key**.
4. Set this key in `application.yml` under `google.play.public-license-key`.
5. The backend will cryptographically verify every purchase signature before crediting coins to prevent APK hacks and replay attacks.

---

## 3. Real-Time Call Signaling Flow (Connecto Parity)

```
Caller (Flutter)               Backend (Spring Boot)             Listener (Flutter)
     │                                   │                                 │
     │ 1. POST /api/calls/initiate       │                                 │
     ├──────────────────────────────────>│                                 │
     │                                   │ 2. STOMP: /topic/listener/{id}  │
     │                                   ├────────────────────────────────>│ (Phone lights up on lockscreen)
     │                                   │                                 │ (IncomingCallScreen rings)
     │                                   │ 3. POST /api/calls/{id}/accept  │
     │                                   │<────────────────────────────────┤ (Taps "Accept")
     │ 4. STOMP: /topic/call/{id}        │                                 │
     │<──────────────────────────────────┤                                 │
     │                                   │                                 │
     ├───────────── Agora RTC Voice Channel (Encrypted Audio) ─────────────┤
     │                                   │                                 │
     │                                   │ 5. Every 60s Billing Tick:      │
     │                                   │    - Deduct coins from Caller   │
     │                                   │    - Credit Listener share      │
     │                                   │                                 │
     │ 6. If Balance < 1 min:            │                                 │
     │<──────────────────────────────────┤                                 │ (Top Warning Banner)
     │                                   │                                 │
```

---

## 4. Google Play Mandatory Compliance Checklist

- [x] **Crisis Hotline Banner:** Integrated into `HomeScreen` with official government tele-mental health numbers (Tele-MANAS `14416`, AASRA).
- [x] **No Medical Claims Disclaimer:** Stated in App Terms and onboarding.
- [x] **UGC Block & Report:** One-tap shield button on `CallScreen` allowing instant user reporting and blocking.
- [x] **Google Play Billing Only:** Coin top-ups routed through Google Play Billing (preventing store rejection).
- [ ] **14-Day Closed Testing (20 Testers):** Google requirement for personal developer accounts created after Nov 2023. Must be started by late December to launch in January 2027.
