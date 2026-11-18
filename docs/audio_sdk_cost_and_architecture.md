# Audio SDK Pricing Comparison & Recommendation

## 1. Cost Comparison Table (Audio Calls)

| Provider | Free Tier (Monthly) | Rate per 1,000 Audio Mins | Cost per Minute (USD) | Approx Cost in INR (per min) | 100,000 Mins Total Cost |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Agora RTC** (Recommended) | **10,000 Mins Free / mo** | **$0.99** | **$0.00099** | **~₹0.085 (8.5 Paise)** | **$89.10 (~₹7,500)** |
| **ZEGOCLOUD** | 10,000 Mins Free / mo | $1.20 - $1.50 | $0.0012 - $0.0015 | ~₹0.10 - ₹0.13 | $108 - $135 (~₹9,200) |
| **LiveKit Cloud** | 10,000 Mins Free / mo | Bandwidth based (~$0.004/GB) | ~$0.0008 | ~₹0.07 | ~$72.00 |
| **LiveKit (Self-Hosted)** | Unlimited (Open Source) | Free (Pay your own VM & Bandwidth) | ~$0.0002 | ~₹0.017 | Server cost (~$40/mo) |

---

## 2. Why Agora is the Best Pick for Your Connecto App

1. **Cheapest Managed Voice Tier:** At **$0.99 per 1,000 minutes**, Agora is significantly cheaper than ZegoCloud and Twilio.
2. **Generous Free Tier:** You get **10,000 free audio minutes every single month**. For your MVP, testing, and first 100–300 users in January 2027, your voice infrastructure bill will be **$0 / month**.
3. **Proven in Weak Network Conditions:** Agora has superior 2G/3G/4G packet-loss resilience (up to 80% packet-loss recovery), which is vital for Indian regional callers.
4. **Official Flutter SDK:** `agora_rtc_engine` gives fine-grained control over audio streams, call state listeners, and background audio execution.

---

## 3. Real-Time Per-Minute Billing Architecture

```
 Caller Phone                  Spring Boot + Redis                   Agora RTC
      │                                 │                                │
      │ 1. Request Call (listener_id)   │                                │
      ├────────────────────────────────>│ (Checks caller balance >= min) │
      │                                 │ (Generates RTC Channel & Token)│
      │                                 │ (Sends FCM ring to Listener)   │
      │                                 │                                │
      │<────────────────────────────────┤ (Returns Agora Token & Channel)│
      │                                 │                                │
      │ 2. Join RTC Channel             │                                ├─────────┐
      ├─────────────────────────────────────────────────────────────────>│ Channel │
      │                                                                  │ Active  │
      │ 3. Listener Accepts & Joins     │                                ├─────────┘
      │                                 │ 4. Agora Webhook (Call Started)│
      │                                 │<───────────────────────────────┤
      │                                 │ (Starts 60s Redis Timer Loop)  │
      │                                 │                                │
      │                                 │ 5. Every 60 Seconds:           │
      │                                 │    - Deducts coins from caller │
      │                                 │    - Credits listener share    │
      │                                 │                                │
      │                                 │ 6. If caller balance < 1 min:  │
      │<────────────────────────────────┤    - Send "LOW_BALANCE" socket │
      │ (Displays warning banner/beep)  │                                │
      │                                 │ 7. If balance = 0:             │
      │                                 │    - Kick from Agora Channel   │
      │<────────────────────────────────┤    - Send Call Ended event     │
```
