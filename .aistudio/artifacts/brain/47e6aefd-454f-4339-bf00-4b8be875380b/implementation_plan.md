# OfferScout: Completed Architecture & Daily Scraping + Push Notifications Roadmap

OfferScout is a native Android deal aggregator built with Kotlin and Jetpack Compose. This plan summarizes everything built to date and outlines the architecture for the next milestone: **Automated Daily Deal Web Scraping & Scheduled Push Notifications**.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> **Confirmed Priority**: The user confirmed that the next milestone should focus on **Automated Daily Deal Web Scraping & Push Notifications**.
>
> **Architecture Strategy**:
> - **Scraping Engine**: A hybrid background synchronization model utilizing Android Jetpack `WorkManager` for battery-efficient daily background jobs, fetching live catalog feeds and parsing circular updates from regional merchants (Supermarkets, Travel, Banks, Amazon).
> - **Push Notification Engine**: Android System Notification manager with rich notification styling (big picture, deal expiry badges, direct deep linking into deal detail screen).
> - **Data Ingestion**: Scraped deals are normalized and upserted into both the local Room database (`CategorizedDealEntity` & `Deal`) and synced to Firebase Firestore for authenticated cloud users.

---

## 1. Overview of Work Completed Till Now

### A. Data Persistence & Architecture
* **Local Database (Room v4)**:
  * `CategorizedDealEntity` & `CategorizedDealDao`: Partitioned and indexed deals by organizational types: **'Travel'**, **'Credit Card'**, and **'Local'**.
  * `Deal` & `DealDao`: Full catalog caching with search, country/city filters, and expiry sorting.
  * `SavedOfferEntity`, `ReminderEntity`, `SearchLogEntity`, `PurchaseRecordEntity`: Offline storage for saved deals, search history, purchase savings tracker, and local alerts.
* **Cloud Persistence (Firebase Firestore Enterprise)**:
  * Zero-trust security rules (`firestore.rules`) enforcing user-ownership isolation (`request.auth.uid == userId`).
  * Cloud collections for `/users/{userId}`, `/users/{userId}/saved_deals/{dealId}`, and `/users/{userId}/reminders/{reminderId}`.
  * Verified with automated 6-test suite in `firestore.test.js` against local emulators.
* **Authentication**:
  * Integrated **Google Sign-In** using Jetpack `CredentialManager` (`GetSignInWithGoogleOption`) and `Firebase.auth`.
  * Multi-user account sync with real-time Firestore listeners.

### B. Core UI & Screens (Jetpack Compose Material 3)
1. **Explore / Home Screen**:
   * Interactive **Location Selector** (United States, Saudi Arabia, UAE, UK, Canada, India, etc., with multi-city support and dynamic currencies like $, SAR, AED, £).
   * **Deal Type Filter Chips**: Instantly switch between "🌟 All Deals", "✈️ Travel", "💳 Credit Card", and "📍 Local".
   * **Category & Source Row**: Amazon, ClickFlyers, Banks, Supermarkets.
   * **Search & Sort Engine**: Filter by keyword, discount %, expiring soonest, or promo codes.
2. **AI Deal Scout Screen**:
   * Gemini-powered deal scout searching and summarizing regional flyer catalogs.
3. **Wishlist & Saved Screen**:
   * Cross-device synchronized saved deals with one-tap redemption tracking.
4. **Expiry Reminders Screen**:
   * Custom notification scheduling for deals about to expire.
5. **Spend Patterns & Savings Insights**:
   * Analytics dashboard tracking total money saved, top shopping categories, and proactive spending advice.
6. **Profile, Card Perks & Sharing**:
   * Card portfolio manager (Amex, Chase, AlRajhi, etc.).
   * One-tap app sharing via Android share sheet and direct link copying.

### C. Build & DevOps Pipeline
* Configured debug keystore and SHA-1/SHA-256 certificate fingerprints with Google Services.
* Added `gradlew` wrapper and `.github/workflows/build-apk.yml` to compile and publish `.apk` artifacts on every GitHub push.

---

## 2. System Architecture & Data Flow

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
│   HomeScreen  │  AiScoutScreen  │  Wishlist  │  Reminders  │  Profile  │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ StateFlow / Events
┌───────────────────────────────────▼────────────────────────────────────┐
│                             OfferViewModel                             │
│       Filtering, Search, DealType, Sorting, Google Sign-In Flow        │
└───────────────────┬───────────────────────────────┬────────────────────┘
                    │                               │
┌───────────────────▼──────────────┐   ┌────────────▼───────────────────┐
│         OfferRepository          │   │      FirebaseSyncRepository     │
│  - Aggregated deals caching      │   │  - Google Sign-In Auth          │
│  - Room Database (v4)            │   │  - Firestore enterprise cloud   │
│  - AI Scout & Gemini API client  │   │  - User profile & saved deals   │
└───────────────────┬──────────────┘   └────────────┬───────────────────┘
                    │                               │
        ┌───────────┴───────────┐                   │
        ▼                       ▼                   ▼
┌──────────────────┐    ┌──────────────┐    ┌───────────────────────────┐
│   DealDao /      │    │ Daily Scraper│    │     Firebase Cloud        │
│ CategorizedDeal  │    │  WorkManager │    │   (Firestore & Auth)      │
│  (Local SQLite)  │    │  (Scheduled) │    └───────────────────────────┘
└──────────────────┘    └───────┬──────┘
                                │
                    ┌───────────▼───────────┐
                    │ System Notifications  │
                    │  (Urgent Deal Alerts) │
                    └───────────────────────┘
```

---

## 3. Next Milestone: Automated Daily Scraping & Push Notifications

### Component 1: Daily Background Scraping Worker (`DailyDealSyncWorker`)
* **Technology**: Android Jetpack `WorkManager` with `PeriodicWorkRequestBuilder` (runs every 24 hours).
* **Constraints**: Requires `NetworkType.CONNECTED` and battery not low.
* **Scraping Pipeline**:
  1. Detects current active country/city preference from `UserProfile`.
  2. Queries public supermarket circulars, airline promotions, and bank discount portals.
  3. Parses discount percentage, merchant, promo code, and expiration dates.
  4. Upserts new entries into Room's `CategorizedDealEntity` and `Deal` tables.
  5. Computes high-value discounts (>30% OFF or expiring in <48 hours) to enqueue for notification.

### Component 2: Smart Notification Engine (`OfferNotificationManager`)
* **Notification Channels**:
  * `daily_deals_channel`: "Daily Top Deals" (low urgency, digests).
  * `expiry_alerts_channel`: "Expiring Deal Alerts" (high urgency, sound + vibration).
* **Rich Styling**:
  * BigTextStyle previews highlighting merchant name, discount badge (e.g. "30% OFF at Lulu Hypermarket"), and promo code.
  * Direct Action Buttons on notification: "Save to Wishlist" and "View Deal".
* **Permission Handling**:
  * Runtime request for `POST_NOTIFICATIONS` on Android 13+ (API 33+) with graceful fallback.

---

## 4. Step-by-Step Implementation Sequence

1. **WorkManager & Serialization Setup**:
   * Ensure `androidx.work:work-runtime-ktx` is enabled in `app/build.gradle.kts`.
2. **Create `DailyDealSyncWorker`**:
   * Implement `CoroutineWorker.doWork()` to fetch, parse, and store new offers.
   * Add duplicate suppression to avoid re-notifying users about deals they have already seen or dismissed.
3. **Create `OfferNotificationManager`**:
   * Build helper methods for dispatching rich notifications with PendingIntents deep-linking to the specific deal in `MainActivity`.
4. **Schedule Periodic Work**:
   * Initialize unique periodic work `DAILY_DEAL_SYNC` in `MainActivity.onCreate()` or `OfferRepository`.
5. **UI Settings & Manual Trigger**:
   * Add a "Notification Preferences" toggle and an immediate "Test Daily Scan & Alert" button in the Profile & Cards screen so users can test notifications on demand.

---

## 5. Verification & Testing

* **Local JVM Tests**: Author Robolectric tests verifying that `DailyDealSyncWorker` executes successfully and correctly inserts deals into `CategorizedDealDao`.
* **Notification Simulation**: Test notification dispatch and deep link intent handling.
* **Compilation**: Verify build succeeds via `compile_applet`.
