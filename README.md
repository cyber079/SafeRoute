# SafeRoute Campus Companion

### ITS62204: Mobile Applications Development · Semester April 2026 · Taylor's University
### Assessment 2 — Android Java Prototype · Version 3

> **100% Free Stack — No credit card required anywhere**

---

## Table of Contents
1. [What is SafeRoute?](#1-what-is-saferoute)
2. [Technology Stack](#2-technology-stack)
3. [App Screens](#3-app-screens)
4. [Project Folder Structure](#4-project-folder-structure)
5. [Setup Instructions](#5-setup-instructions)
6. [Database Structure](#6-database-structure)
7. [Permissions](#7-permissions)
8. [How to Test Each Feature](#8-how-to-test-each-feature)
9. [Known Issues and Fixes](#9-known-issues-and-fixes)
10. [What Still Needs to Be Done](#10-what-still-needs-to-be-done)
11. [Team Roles](#11-team-roles)
12. [Version History](#12-version-history)

---

## 1. What is SafeRoute?

SafeRoute Campus Companion is an Android safety application for Taylor's University students. It allows students to:

- Send emergency **SOS alerts** with live GPS coordinates via SMS
- View **well-lit safe paths** on an interactive campus map
- **Report and browse** community safety incidents in real-time
- **Mark themselves safe** with a check-in system
- Access **campus emergency contacts** quickly

Built entirely in **Java** using Android Studio. Uses a 100% free technology stack.

---

## 2. Technology Stack

| Layer | Technology | Cost | Notes |
|---|---|---|---|
| Language | Java | Free | Required — all app logic |
| IDE | Android Studio (latest) | Free | Build, run, debug |
| Local Database | SQLite (SQLiteOpenHelper) | Free | Contacts + seed paths — works offline |
| Cloud Database | Firebase Realtime Database | Free (Spark) | Shared incidents, check-ins, paths |
| Authentication | Firebase Authentication | Free (Spark) | Email + password login |
| Maps | OSMDroid 6.1.17 (OpenStreetMap) | Free — no key | No API key, no billing needed |
| Location (GPS) | FusedLocationProviderClient | Free | Live GPS for SOS screen |
| SMS Alerts | Android SmsManager | Free | Works without internet (cellular) |

> **Why OSMDroid instead of Google Maps?**
> Google Maps SDK requires enabling a billing account even for the free tier. OSMDroid uses OpenStreetMap tiles which are completely free — no account, no API key, no setup needed.

---

## 3. App Screens

| Screen | Activity File | Purpose |
|---|---|---|
| Login | `LoginActivity.java` | Firebase Auth sign-in. Validates session on launch. |
| Register | `RegisterActivity.java` | Firebase Auth new account creation. |
| Home Dashboard | `MainActivity.java` | Quick actions, mini map, stats, recent alerts. |
| Campus Map | `MapActivity.java` | Full OSMDroid map — safe paths, markers, incidents. |
| Safety Alerts | `AlertsActivity.java` | Real-time incident feed. Submit incident dialog. |
| Campus Help | `HelpActivity.java` | Emergency contacts list with Call Now buttons. |
| Safety Check-in | `CheckInActivity.java` | Mark yourself safe. View community check-ins. |
| SOS Alert | `SosActivity.java` | GPS capture + SMS to emergency contacts. |

---

## 4. Project Folder Structure

```
SafeRoute_Free/
├── app/
│   ├── google-services.json              ← REPLACE with real file from Firebase Console
│   ├── build.gradle                      ← All dependencies + namespace declaration
│   └── src/main/
│       ├── AndroidManifest.xml           ← Permissions + activity declarations
│       ├── java/com/saferoute/app/
│       │   ├── activities/               ← All 8 Activity Java files
│       │   ├── adapters/                 ← AlertAdapter, CheckInAdapter, ContactAdapter, ResourceAdapter
│       │   ├── models/                   ← Alert, CheckIn, Contact, SafePath, CampusResource
│       │   └── database/                 ← SafeRouteDatabase.java (SQLite), FirebaseHelper.java
│       └── res/
│           ├── layout/                   ← All XML layout files for screens, items, dialogs
│           ├── drawable/                 ← All 17 vector icons and background shape drawables
│           └── values/                   ← colors.xml, strings.xml, themes.xml, dimens.xml, arrays.xml
├── gradle.properties                     ← android.useAndroidX=true (REQUIRED)
├── build.gradle                          ← Project-level gradle
└── settings.gradle
```

---

## 5. Setup Instructions

> ⚠️ **Use `SafeRoute_Free_Codebase_v3.zip` only** — not v1 or v2.
> v3 includes the OSMDroid map fix, AndroidX gradle.properties fix, and login session fix.

### Step 1 — Download Android Studio
Go to [developer.android.com/studio](https://developer.android.com/studio) and install the latest stable version.

### Step 2 — Open the project
Extract `SafeRoute_Free_Codebase_v3.zip` → open the `SafeRoute_Free` folder in Android Studio.

### Step 3 — Create Firebase project *(Member 3 does this)*
1. Go to [console.firebase.google.com](https://console.firebase.google.com)
2. Click **Add project** → name it `SafeRoute`
3. **Stay on the FREE Spark plan — do NOT click Upgrade**

### Step 4 — Add Android app to Firebase
1. Inside your project → **Add app** → choose Android
2. Package name: `com.saferoute.app`
3. Download **`google-services.json`**
4. Replace the placeholder `app/google-services.json` with the real downloaded file

> ⚠️ The app will crash on launch if the placeholder file is not replaced. This is the most common setup mistake.

### Step 5 — Enable Authentication
Firebase Console → **Build → Authentication → Sign-in method → Enable Email/Password**

### Step 6 — Create Realtime Database
Firebase Console → **Build → Realtime Database → Create Database → Start in test mode**

> ✅ No billing prompt will appear — Realtime Database is free on the Spark plan.

### Step 7 — Fix Database Rules
Go to **Realtime Database → Rules tab** and replace with:

```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

Click **Publish**.

> Before final submission, tighten to auth-required:
> ```json
> {
>   "rules": {
>     ".read": "auth != null",
>     ".write": "auth != null"
>   }
> }
> ```

### Step 8 — Maps *(nothing to configure)*
OSMDroid uses OpenStreetMap — completely free, no API key, no Google Cloud account needed. It works the moment you run the app.

### Step 9 — Copy drawable icon files
Extract `drawable_icons.zip` → copy all **17 XML files** into:
```
app/src/main/res/drawable/
```
Overwrite all existing files when prompted. The 17 files are:
```
ic_shield.xml           ic_shield_green.xml     ic_sos.xml
ic_report.xml           ic_navigate.xml         ic_alert_others.xml
ic_bell.xml             ic_home.xml             ic_map_pin.xml
ic_phone.xml            ic_checkin.xml          ic_email.xml
ic_lock.xml             bg_spinner.xml          bg_logo_red.xml
bg_circle_white.xml     bg_circle_green_light.xml
```

### Step 10 — Sync and Run
**File → Sync Project with Gradle Files** → press **▶ Run**

---

## 6. Database Structure

### Firebase Realtime Database

| Node | Fields | Written by |
|---|---|---|
| `incidents/{id}` | userId, description, location, type, verified, latitude, longitude, timestamp | `AlertsActivity` |
| `safe_paths/{id}` | label, lat_start, lng_start, lat_end, lng_end | Future feature |
| `checkins/{id}` | userId, displayName, location, timestamp | `CheckInActivity` |

### SQLite (local, on-device)

| Table | Fields | Purpose |
|---|---|---|
| `emergency_contacts` | id, name, phone | Personal SOS contacts — never leaves device, works offline |
| `safe_paths` | id, label, lat_start, lng_start, lat_end, lng_end | Pre-seeded well-lit campus paths for map |

> ⚠️ The seed path coordinates in `SafeRouteDatabase.java` are placeholder values.
> The team must physically survey Taylor's campus, record real GPS coordinates, and update `seedSafePaths()` before the demo.

---

## 7. Permissions

| Permission | Type | Why it is needed |
|---|---|---|
| `INTERNET` | Normal | Firebase + OSMDroid map tile downloads |
| `ACCESS_NETWORK_STATE` | Normal | OSMDroid checks connectivity before downloading tiles |
| `ACCESS_WIFI_STATE` | Normal | OSMDroid network type detection |
| `ACCESS_FINE_LOCATION` | Dangerous | GPS coordinates for SOS alert and map location |
| `ACCESS_COARSE_LOCATION` | Dangerous | Fallback location for map |
| `SEND_SMS` | Dangerous | SOS alert SMS dispatch via SmsManager |
| `CALL_PHONE` | Dangerous | Call Now button in Campus Resources screen |
| `WRITE_EXTERNAL_STORAGE` | Dangerous | OSMDroid tile cache (API 18 and below only) |

> Dangerous permissions are requested at runtime when the user first needs them.

---

## 8. How to Test Each Feature

### 8.1 Login and Register
1. Open the app — should show **Login screen**
   > If it goes directly to Home, `google-services.json` was not replaced correctly
2. Tap **Register with your university email**
3. Enter any `.edu` or `.edu.my` email and a password of at least 6 characters
4. Account created — app navigates to Home screen
5. Close and reopen — should go to Home directly (session remembered)

### 8.2 Map
1. From Home, tap **Full Map >** or the Map tab
2. Allow location permission when prompted
3. Map loads OpenStreetMap tiles *(needs internet on first load)*
4. Green lines appear showing pre-seeded safe paths
5. Blue dot shows your current location

> 📌 If map shows blank — check internet connection. Tiles are cached after first load so the map works offline afterwards.

### 8.3 Report an Incident
1. Tap the **Alerts** tab or **Report** quick action on Home
2. Tap the red **+** button (FAB) at the bottom right
3. Fill in location, description, and select type (ALERT / INFO / SAFE)
4. Tap **Submit** — the report appears instantly for all users

> ⚠️ If you get "Permission denied" — Firebase Console → Realtime Database → Rules → set `.read` and `.write` to `true` → Publish.

### 8.4 SOS Alert *(real device only)*
1. From Home, tap **SOS**
2. Tap **+ Add Contact** → save a groupmate's phone number
3. Allow Location and SMS permissions when prompted
4. Tap **SEND SOS ALERT**
5. The contact receives an SMS:

```
🚨 SOS ALERT from SafeRoute!
I need help. My location:
https://maps.google.com/?q=LAT,LNG
Please contact me or call emergency services.
```

> ⚠️ SMS only works on a **real physical Android device** with an active SIM card.
> It will **not** work on the emulator.

### 8.5 Safety Check-in
1. Tap the **Check-in** tab
2. Select your campus location from the dropdown
3. Tap **Check In — I'm Safe**
4. Your check-in appears in the Community Check-ins list
5. Open on another device — your check-in should appear there too (real-time sync)

---

## 9. Known Issues and Fixes

| Error / Symptom | Cause | Fix |
|---|---|---|
| AndroidX dependencies error on build | `gradle.properties` missing | Add `android.useAndroidX=true` and `android.enableJetifier=true` to `gradle.properties` |
| App launches directly to Home, skips Login | Stale Firebase session cache | Fixed in v2+ — `LoginActivity` calls `currentUser.reload()` to verify session |
| Map shows blank / grey tiles | Missing user agent or cache dir | Fixed in v3+ — `MapActivity` sets `setUserAgentValue()` and `setOsmdroidBasePath()` |
| Map still blank after v3 fix | No internet on device/emulator | Make sure device has active Wi-Fi or mobile data |
| Permission denied on reporting alert | Firebase Realtime DB rules not set | Firebase Console → Realtime Database → Rules → set `.read` and `.write` to `true` → Publish |
| App icon shows red circle | Manifest used `@drawable` instead of `@mipmap` | Change `android:icon` to `@mipmap/ic_launcher` in `AndroidManifest.xml` |
| `resource drawable/bg_spinner not found` | Background drawable files missing | Copy all 17 files from `drawable_icons.zip` into `res/drawable/` |
| SMS not sending | Testing on emulator | SMS only works on a real physical device with a SIM card |
| SMS not sending on real device | SEND_SMS permission denied | Phone Settings → Apps → SafeRoute → Permissions → enable SMS |
| Gradle sync failed | Various | File → Invalidate Caches → Restart, then sync again |
| `google-services.json` error | Placeholder file not replaced | Download real file from Firebase Console → paste into `app/` folder |

---

## 10. What Still Needs to Be Done

### 🔴 High Priority — must complete before demo

- [ ] Add **logout button** to Home screen — `MainActivity.java` + `activity_main.xml`
- [ ] Wire up **delete contact button** in SOS screen — `ContactAdapter.java`
- [ ] Update **seed path GPS coordinates** to real Taylor's campus values — `SafeRouteDatabase.java`
- [ ] Update **campus contact phone numbers** to real Taylor's numbers — `HelpActivity.java`
- [ ] Update **campus marker GeoPoints** to real coordinates — `MapActivity.java`

### 🟡 Medium Priority — should do for good marks

- [ ] Add permission denial fallback dialog — `SosActivity.java` + `MapActivity.java`
- [ ] Add active tab highlight in bottom navigation bar — all Activities
- [ ] Add loading spinner during Firebase data fetch — `AlertsActivity.java` + `CheckInActivity.java`
- [ ] Add empty state views (no alerts / no contacts / no check-ins) — RecyclerView layouts
- [ ] Set Realtime DB rules from test mode to auth-required before final submission
- [ ] Add proper app icon — `res/mipmap/` folders

### 🟢 Low Priority — nice to have

- [ ] Restrict registration to `@taylors.edu.my` email domain — `RegisterActivity.java`
- [ ] Add profile screen with logout and change password — new `ProfileActivity.java`
- [ ] Add swipe-to-dismiss on alert cards — `AlertsActivity.java`
- [ ] Add filter tabs on alerts screen (All / Alert / Info / Safe) — `AlertsActivity.java`
- [ ] Add pagination on alerts list — `AlertsActivity.java`

---

## 11. Team Roles

| Member | Role | Screens owned | Report sections |
|---|---|---|---|
| Member 1 (Leader) | Coordination + Documentation | Login, Register, Home, Help | Sec 1, 2, 3, 4, 9, 11, 12 |
| Member 2 | UI/UX + Maps | MapActivity, Check-in UI | Sec 5 (System Design) |
| Member 3 | Backend + Firebase | Alerts, Check-in DB, Firebase setup | Sec 6, 7 (Development + GenAI) |
| Member 4 | SOS + Testing | SosActivity | Sec 8 (Testing) |

---

## 12. Version History

| Version | Changes |
|---|---|
| v1 | Initial codebase — Java + SQLite + Firebase Firestore + Google Maps SDK |
| v2 | Replaced Firestore → Realtime Database (free). Replaced Google Maps → OSMDroid. Added `gradle.properties`. Fixed login session bypass. Added `FirebaseHelper.java`. |
| v3 (current) | Fixed blank map — added `setUserAgentValue()`, `setOsmdroidBasePath()`, `setOsmdroidTileCache()`. Added `ACCESS_NETWORK_STATE` + `ACCESS_WIFI_STATE` to Manifest. Removed `package` from Manifest (moved to `build.gradle` namespace). Fixed all 17 drawable resource files. |

---

*SafeRoute Campus Companion · ITS62204 Semester April 2026 · Taylor's University*
