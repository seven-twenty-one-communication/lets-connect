# Complete Testing & Production Support Guide

This guide details everything required to **test** the application locally/in closed testing, and everything needed to **support it in production 24/7**.

---

## Part 1: What You Need for Testing

```
  ┌────────────────────────────────────────────────────────────────────────┐
  │                           Testing Checklist                            │
  ├────────────────────────────────────────────────────────────────────────┤
  │ 1. Docker Compose (Local PostgreSQL 16 + Redis 7 with auto seed)       │
  │ 2. Two Devices (Phone A = Caller, Phone B = Listener)                  │
  │ 3. Agora Free Developer Account (10,000 free minutes / month)          │
  │ 4. Google Play License Testers (Buy coins for ₹0 in sandbox)           │
  │ 5. Google Play Closed Test Group (20 Testers for 14 Days)              │
  └────────────────────────────────────────────────────────────────────────┘
```

### 1. Local Database & Cache (Zero Setup)
In the project root, run:
```powershell
docker compose up -d
```
*This starts PostgreSQL 16 and Redis 7 automatically, pre-seeded with categories, gifts, and helplines.*

### 2. Device Hardware for End-to-End Call Testing
* **Minimum Requirement:** **Two Android devices** (or 1 physical Android phone + 1 Android Studio Emulator).
  - **Device A (Caller):** Logs in, browses listeners, top-up coins, taps "Call Now", sends gifts.
  - **Device B (Listener):** Toggles "Online", receives the full-screen incoming call over lockscreen, accepts, and talks.
* **Network Testing:** Test with both Wi-Fi and 3G/4G to verify Agora's automatic packet loss recovery and audio clarity.

### 3. Google Play Billing Sandbox Testing (Free Coin Top-ups)
* In Google Play Console:
  1. Go to **Settings > License Testing**.
  2. Add your developer email and testers' Gmail addresses.
  3. Set **License Response** to `RESPOND_NORMALLY`.
* **Result:** Whenever you or your testers buy the ₹49, ₹99, or ₹249 coin packs in the app, Google Play approves the purchase instantly using a test card for **₹0**, and our backend credits the coins!

### 4. Mandatory Google Play 20-Tester Closed Testing (For January 2027 Launch)
* Since November 2023, Google requires all personal developer accounts to run a **Closed Test with at least 20 opted-in testers for 14 continuous days** before the "Apply for Production" button is unlocked.
* **Action Item:** Create a closed testing track in Play Console by **late December** and invite 20 friends/testers so you meet the 14-day requirement in time for the January 2027 public release.

---

## Part 2: What You Need for 24/7 Production Support

To keep the app running reliably without downtime or customer complaints once real users start paying, you need 5 production support pillars:

```
                          ┌──────────────────────────┐
                          │    Production Support    │
                          └────────────┬─────────────┘
                                       │
      ┌────────────────┬───────────────┼───────────────┬────────────────┐
      ▼                ▼               ▼               ▼                ▼
┌───────────┐    ┌───────────┐   ┌───────────┐   ┌───────────┐    ┌───────────┐
│  Server   │    │ 24/7 Down │   │ Crash &   │   │   Audio   │    │ Customer  │
│  Hosting  │    │  Monitor  │   │ Exception │   │ Analytics │    │  Disputes │
└───────────┘    └───────────┘   └───────────┘   └───────────┘    └───────────┘
```

### 1. Cloud Server & Database Hosting
* **VPS Size:** 2 vCPU / 4 GB RAM (e.g., AWS EC2 `t4g.medium`, Hetzner CX22, or DigitalOcean Droplet: ~$10–$15 / month).
* **Database Backups:** Automated daily PostgreSQL snapshots (stored offsite on AWS S3 or Cloudflare R2).
* **Process Management:** Docker restart policy `restart: always` or a systemd service ensuring the Spring Boot backend restarts automatically if the server reboots.

### 2. 24/7 Uptime & Down Monitoring (Free)
* We integrated **Spring Boot Actuator** (`/actuator/health`).
* Use **UptimeRobot** or **BetterStack** (Free plan):
  - Pings `https://api.yourdomain.com/actuator/health` every 60 seconds.
  - Sends an instant alert to your Telegram, WhatsApp, or Email if the server ever goes down.

### 3. Crash & Error Reporting (Sentry)
* **Sentry.io** (Free tier covers 5,000 errors/month):
  - Integrates into both Flutter mobile and Spring Boot.
  - If a user's phone crashes or a payment fails, Sentry emails you the exact line number, device model, and error stack trace immediately.

### 4. Agora Call Quality & Drop Analytics
* Log in to [console.agora.io](https://console.agora.io) > **Agora Analytics** (Free built-in tool):
  - View real-time audio latency, packet loss, and call drop rates across all active calls in India.
  - Helps resolve listener complaints like *"I was on call for 5 minutes, but the call dropped."*

### 5. Dispute Resolution & Customer Support
* **Auto-Refund Policy:** If a call disconnects within the first 10 seconds (due to network timeout or accidental tap), the backend automatically refunds the caller's coins.
* **Support Email:** Create `support@yourdomain.com` for billing issues.
* **Abuse Moderation:** Check the `abuse_reports` table weekly to ban users who harass listeners or violate community rules.

### 6. Legal & Google Play Mandatory URLs
Google Play will reject your app submission unless these two public links exist:
1. **Privacy Policy URL:** Must explicitly state that call audio is encrypted and user phone numbers are never shared.
2. **Account Deletion URL:** Google requires an in-app button and a web link where users can request account and data deletion.
