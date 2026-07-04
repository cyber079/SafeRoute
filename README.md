# SafeRoute Campus Companion
### ITS62204 Mobile Applications Development — Assessment 2
**Taylor's University · Semester April 2026**
### ⭐ 100% FREE VERSION — No credit card, no billing account required anywhere

---

## What changed from the original version
| Component | Old (required billing) | New (100% free) |
|---|---|---|
| Maps | Google Maps SDK | **OSMDroid (OpenStreetMap)** — no API key needed |
| Shared/cloud database | Firebase Firestore | **Firebase Realtime Database** — free Spark plan |
| Authentication | Firebase Auth | Unchanged — already free |
| Local database | SQLite | Unchanged |
| SMS alerts | SmsManager | Unchanged |

No feature was removed. The app does exactly the same things — login, SOS, map, incidents, check-in — just on a fully free backend.

---

## Tech stack
| Layer | Technology | Cost |
|---|---|---|
| Language | Java | Free |
| IDE | Android Studio | Free |
| Local database | SQLite via `SQLiteOpenHelper` | Free |
| Cloud database | Firebase Realtime Database | Free (Spark plan) |
| Authentication | Firebase Authentication | Free (Spark plan) |
| Maps | OSMDroid (OpenStreetMap) | Free, no API key |
| Location | FusedLocationProviderClient | Free |
| SMS alerts | Android SmsManager | Free |

---

## Setup instructions

### Step 1 — Firebase setup (Member 3) — NO credit card needed
1. Go to https://console.firebase.google.com
2. Create a new project called "SafeRoute"
3. **Do NOT click "Upgrade to Blaze"** — stay on the free Spark plan
4. Add an Android app with package name `com.saferoute.app`
5. Download `google-services.json`
6. Replace the placeholder `app/google-services.json` with this real file
7. Enable **Email/Password** under Authentication → Sign-in methods
8. Go to **Build → Realtime Database → Create Database**
9. Choose a region → **Start in test mode** (no billing prompt will appear)

### Step 2 — Maps setup (Member 2) — Nothing to configure!
OSMDroid works immediately with no account, no API key, and no setup. Map tiles load directly from OpenStreetMap's free servers.

### Step 3 — Update campus coordinates (All members)
In `SafeRouteDatabase.java`, update the seed path coordinates to match the actual Taylor's University campus layout.

In `MapActivity.java` and `MainActivity.java`, update `TAYLORS_CAMPUS`, `SECURITY_POST_A`, `HEALTH_CENTER`, and `MAIN_GATE` GeoPoint coordinates.

### Step 4 — Update campus contacts (Member 1)
In `HelpActivity.java`, update phone numbers in `buildResourceList()` to real Taylor's University numbers.

### Step 5 — Set Realtime Database security rules (Member 3, before submission)
In Firebase Console → Realtime Database → Rules tab, replace the default test rules with:
```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
```
This requires users to be logged in to read/write data, while remaining completely free.

### Step 6 — Run the app
1. Open the project in Android Studio
2. Wait for Gradle sync to complete (downloads OSMDroid + Firebase libraries)
3. Connect a device or start an emulator (API 24+)
4. Run the app

---

## Screen list
| Screen | Activity | Owner |
|---|---|---|
| Login | `LoginActivity` | Member 1 |
| Register | `RegisterActivity` | Member 1 |
| Home / Dashboard | `MainActivity` | Member 1 |
| Campus Map | `MapActivity` | Member 2 |
| Safety Alerts | `AlertsActivity` | Member 3 |
| Campus Resources / Help | `HelpActivity` | Member 1 |
| Safety Check-in | `CheckInActivity` | Member 2 + 3 |
| SOS / Quick-Alert | `SosActivity` | Member 4 |

---

## Realtime Database structure
```
saferoute-default-rtdb/
├── incidents/
│   └── {auto-id}/
│       ├── userId, description, location, type, verified, latitude, longitude, timestamp
├── safe_paths/
│   └── {auto-id}/
│       ├── label, lat_start, lng_start, lat_end, lng_end
└── checkins/
    └── {auto-id}/
        ├── userId, displayName, location, timestamp
```

## SQLite tables
| Table | Purpose |
|---|---|
| `emergency_contacts` | Personal SOS contacts (offline) |
| `safe_paths` | Pre-seeded well-lit campus paths |

---

## Why this is genuinely free — for your report (Section 7, Technical Approaches)

> "SafeRoute's cloud architecture uses Firebase Realtime Database and Firebase Authentication on the Spark (free) plan, which does not require a billing account and provides limits more than sufficient for a student prototype (1GB storage, 10GB/month transfer). Mapping functionality is implemented using OSMDroid, an open-source library that renders OpenStreetMap tiles directly with no API key or account registration required. This combination eliminates any financial barrier to development while preserving full functionality of the originally proposed architecture."

---

## GitHub commit guidelines
- Every member commits their own code directly
- Use meaningful commit messages: `feat: add SOS GPS capture`, `fix: realtime db null pointer`
- Branch: work on `develop`, merge to `main` when feature is complete

---

*Generated as a development reference · SafeRoute · ITS62204 Semester April 2026*
