# SafeRoute Campus Companion

### ITS62204: Mobile Applications Development · Semester April 2026 · Taylor's University
### Assessment 2 — Android Java Prototype · Version 4

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
- **Manage their account** via a Profile screen with logout and password reset

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
| Location (GPS) | FusedLocationProviderClient | Free | Fresh high-accuracy GPS for SOS + map |
| SMS Alerts | Android SmsManager | Free | Works without internet (cellular) |

> **Why OSMDroid instead of Google Maps?**
> Google Maps SDK requires enabling a billing account even for the free tier. OSMDroid uses OpenStreetMap tiles which are completely free — no account, no API key, no setup needed.

---

## 3. App Screens

| Screen | Activity File | Purpose |
|---|---|---|
| Login | `LoginActivity.java` | Firebase Auth sign-in. Validates session on launch. |
| Register | `RegisterActivity.java` | Firebase Auth new account creation. |
| Home Dashboard | `MainActivity.java` | Quick actions, mini map, stats, recent alerts. Profile button in top bar. |
| Campus Map | `MapActivity.java` | Full OSMDroid map — safe paths, markers, live location dot. |
| Safety Alerts | `AlertsActivity.java` | Real-time incident feed. Submit incident dialog. |
| Campus Help | `HelpActivity.java` | Emergency contacts list with Call Now buttons. |
| Safety Check-in | `CheckInActivity.java` | Mark yourself safe. View community check-ins. |
| SOS Alert | `SosActivity.java` | GPS capture + SMS to emergency contacts. Add/delete contacts. |
| Profile | `ProfileActivity.java` | View account info, change password, logout. |

---

## 4. Project Folder Structure

```
SafeRoute_Free/
├── app/
│   ├── google-services.json              ← REPLACE with real file from Firebase Console
│   ├── build.gradle                      ← All dependencies + namespace declaration
│   └── src/main/
│       ├── AndroidManifest.xml           ← Permissions + all 9 activity declarations
│       ├── java/com/saferoute/app/
│       │   ├── activities/               ← All 9 Activity Java files (includes ProfileActivity)
│       │   ├── adapters/                 ← AlertAdapter, CheckInAdapter, ContactAdapter, ResourceAdapter
│       │   ├── models/                   ← Alert, CheckIn, Contact, SafePath, CampusResource
│       │   └── database/                 ← SafeRouteDatabase.java (SQLite), FirebaseHelper.java
│       └── res/
│           ├── layout/                   ← All XML layout files including activity_profile.xml
│           ├── drawable/                 ← All 19 vector icons and background shape drawables
│           └── values/                   ← colors.xml, strings.xml, themes.xml, dimens.xml, arrays.xml
├── gradle.properties                     ← android.useAndroidX=true (REQUIRED)
├── build.gradle                          ← Project-level gradle
└── settings.gradle
```

---

## 5. Setup Instructions

> ⚠️ **Use the latest codebase only** — make sure you have all v4 fixed files applied.

### Step 1 — Download Android Studio
Go to [developer.android.com/studio](https://developer.android.com/studio) and install the latest stable version.

### Step 2 — Open the project
Extract the ZIP → open the `SafeRoute_Free` folder in Android Studio.

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
OSMDroid uses OpenStreetMap — completely free, no API key, no Google Cloud account needed. Works immediately on first run.

### Step 9 — Copy drawable icon files
Copy all **19 XML files** from the drawable icons ZIP into:
```
app/src/main/res/drawable/
```
Overwrite all existing files when prompted. Files include:
```
ic_shield.xml           ic_shield_green.xml     ic_sos.xml
ic_report.xml           ic_navigate.xml         ic_alert_others.xml
ic_bell.xml             ic_home.xml             ic_map_pin.xml
ic_phone.xml            ic_checkin.xml          ic_email.xml
ic_lock.xml             ic_person.xml           bg_spinner.xml
bg_logo_red.xml         bg_circle_white.xml     bg_circle_green_light.xml
bg_avatar_red.xml
```

### Step 10 — Update campus coordinates
Open `SafeRouteDatabase.java` and update `seedSafePaths()` with real GPS coordinates from Taylor's University campus. Walk each path with Google Maps open and record start/end coordinates. Also update `MapActivity.java`:

```java
// Replace placeholder values with real Taylor's coordinates
private static final GeoPoint TAYLORS_CAMPUS  = new GeoPoint(3.06731, 101.60033);
private static final GeoPoint SECURITY_POST_A = new GeoPoint(/* real coords */);
private static final GeoPoint HEALTH_CENTER   = new GeoPoint(/* real coords */);
private static final GeoPoint MAIN_GATE       = new GeoPoint(/* real coords */);
```

### Step 11 — Sync and Run
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

> ⚠️ The seed path coordinates in `SafeRouteDatabase.java` are approximate placeholder values.
> Update `seedSafePaths()` with real GPS coordinates from physical campus survey before demo.

---

## 7. Permissions

| Permission | Type | Why it is needed |
|---|---|---|
| `INTERNET` | Normal | Firebase + OSMDroid map tile downloads |
| `ACCESS_NETWORK_STATE` | Normal | OSMDroid checks connectivity before downloading tiles |
| `ACCESS_WIFI_STATE` | Normal | OSMDroid network type detection |
| `ACCESS_FINE_LOCATION` | Dangerous | Fresh GPS coordinates for SOS alert and live map dot |
| `ACCESS_COARSE_LOCATION` | Dangerous | Fallback location for map |
| `SEND_SMS` | Dangerous | SOS alert SMS dispatch via SmsManager |
| `CALL_PHONE` | Dangerous | Call Now button in Campus Resources screen |
| `WRITE_EXTERNAL_STORAGE` | Dangerous | OSMDroid tile cache (API 18 and below only) |

> Dangerous permissions are requested at runtime when the user first needs them.
> If permanently denied, the app shows a dialog directing the user to phone Settings → Apps → SafeRoute → Permissions.

---

## 8. How to Test Each Feature

### 8.1 Login and Register
1. Open the app — should show **Login screen**
   > If it goes directly to Home, `google-services.json` was not replaced correctly
2. Tap **Register with your university email**
3. Enter any `.edu` or `.edu.my` email and a password of at least 6 characters
4. Account created — app navigates to Home screen
5. Close and reopen — should go to Home directly (session remembered)

### 8.2 Profile and Logout
1. From Home, tap the **person icon** in the top right
2. Profile screen shows your email initial as avatar, full email, and user ID
3. Tap **Change Password** — a reset link is sent to your email
4. Tap **Log Out** — confirmation dialog appears
5. Confirm logout — app returns to Login screen and clears session
6. Back button no longer returns to the app without logging in again

### 8.3 Map and Live Location
1. From Home, tap **Full Map >** or the Map tab
2. Allow location permission when prompted
3. Map loads OpenStreetMap tiles *(needs internet on first load)*
4. **Blue dot** appears at your real current location within a few seconds
5. Green lines show pre-seeded safe paths around campus
6. Tap **Navigate** button — map centres and zooms to your current position
7. Tap any campus marker to see its name and details

> 📌 If map shows blank — check internet connection. Tiles cache after first load for offline use.
> 📌 If blue dot does not appear — make sure GPS is enabled in phone Settings.

### 8.4 Report an Incident
1. Tap the **Alerts** tab or **Report** quick action on Home
2. Tap the red **+** button (FAB) at bottom right
3. Fill in location, description, and select type (ALERT / INFO / SAFE)
4. Tap **Submit** — report appears instantly for all users in real-time

> ⚠️ If you get "Permission denied" — Firebase Console → Realtime Database → Rules → set `.read` and `.write` to `true` → Publish.

### 8.5 SOS Alert *(real device only)*
1. From Home, tap **SOS**
2. Tap **+ Add Contact** → enter name and phone number (e.g. `0123456789` or `+60123456789`)
3. Allow Location and SMS permissions when prompted
4. Tap **SEND SOS ALERT**
5. App requests a **fresh GPS fix** — wait a few seconds
6. SMS is sent to all saved contacts:

```
SOS ALERT from SafeRoute!
I need help. My location:
https://maps.google.com/?q=LAT,LNG
Please contact me or call emergency services.
```

7. To delete a contact — tap the **delete icon** on the right of any contact → confirm deletion

> ⚠️ SMS only works on a **real physical Android device** with an active SIM card.
> It will **not** work on the emulator.
> ⚠️ If SMS fails on real device — phone Settings → Apps → SafeRoute → Permissions → enable SMS.

### 8.6 Safety Check-in
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
| Map still blank after fix | No internet on device/emulator | Make sure device has active Wi-Fi or mobile data on first launch |
| Blue location dot not appearing | Location permission not granted or GPS off | Allow location permission when prompted + enable GPS in phone Settings |
| Blue dot appears but in wrong location | Placeholder campus coordinates | Update `TAYLORS_CAMPUS` GeoPoint in `MapActivity.java` with real coordinates |
| Navigate button did nothing | Not implemented | Fixed in v4 — button now centres map on your current GPS position |
| SMS not sending | Testing on emulator | SMS only works on a real physical device with a SIM card |
| SMS not sending on real device (Android 12+) | `SmsManager.getDefault()` deprecated | Fixed in v4 — uses `getSystemService(SmsManager.class)` for Android 12+ |
| SMS not sending on real device (any version) | SEND_SMS permission denied | Phone Settings → Apps → SafeRoute → Permissions → enable SMS |
| Delete contact button does nothing | Click listener not wired up | Fixed in v4 — `ContactAdapter` now has `OnDeleteListener` callback |
| Permission denied on reporting alert | Firebase Realtime DB rules not set | Firebase Console → Realtime Database → Rules → set `.read` and `.write` to `true` → Publish |
| App icon shows red circle | Manifest used `@drawable` instead of correct resource | Use `android:icon="@drawable/ic_shield"` — mipmap folder does not exist in this project |
| `resource drawable/bg_spinner not found` | Background drawable files missing | Copy all 19 files from drawable icons ZIP into `res/drawable/` |
| App crashes randomly | `btnProfile` not found in layout | Make sure `activity_main.xml` is the updated version with `btnProfile` added |
| `ProfileActivity` not found crash | Activity not declared in Manifest | Add `<activity android:name=".activities.ProfileActivity" android:exported="false"/>` to `AndroidManifest.xml` |
| Gradle sync failed | Various | File → Invalidate Caches → Restart, then sync again |
| `google-services.json` error | Placeholder file not replaced | Download real file from Firebase Console → paste into `app/` folder |

---

## 10. Team Roles

| Member | Role | Screens owned | Report sections |
|---|---|---|---|
| Member 1 (Leader) | Coordination + Documentation | Login, Register, Home, Help, Profile | Sec 1, 2, 3, 4, 9, 11, 12 |
| Member 2 | UI/UX + Maps | MapActivity, Check-in UI | Sec 5 (System Design) |
| Member 3 | Backend + Firebase | Alerts, Check-in DB, Firebase setup | Sec 6, 7 (Development + GenAI) |
| Member 4 | SOS + Testing | SosActivity | Sec 8 (Testing) |

---

## 11. Version History

| Version | Changes |
|---|---|
| v1 | Initial codebase — Java + SQLite + Firebase Firestore + Google Maps SDK |
| v2 | Replaced Firestore → Realtime Database (free). Replaced Google Maps → OSMDroid. Added `gradle.properties`. Fixed login session bypass. Added `FirebaseHelper.java`. |
| v3 | Fixed blank map — added `setUserAgentValue()`, `setOsmdroidBasePath()`, `setOsmdroidTileCache()`. Added `ACCESS_NETWORK_STATE` + `ACCESS_WIFI_STATE` to Manifest. Fixed all 17 drawable resource files. |
| v4 (current) | Added `ProfileActivity` — account info, logout with confirmation dialog, change password via Firebase. Fixed SMS not sending on Android 12+ (`getSystemService(SmsManager.class)`). Fixed live location blue dot not appearing — `myLocationOverlay.runOnFirstFix()` now centres map on user's real position. Fixed Navigate button — now centres map on current GPS position. Fixed delete contact button — `ContactAdapter` now has `OnDeleteListener` callback wired to `SosActivity`. Fixed SOS GPS — now always requests a fresh high-accuracy location instead of stale cached fix. Added 15-second GPS timeout with fallback message. Added permission denial dialog directing user to phone Settings. Added 2 new drawables: `ic_person.xml`, `bg_avatar_red.xml`. |

---

*SafeRoute Campus Companion · ITS62204 Semester April 2026 · Taylor's University*
