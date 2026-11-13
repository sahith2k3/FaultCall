# FaultCall — Smartwatch-Controlled Tennis Umpire & Announcement System

**FaultCall** is a professional tennis companion application that pairs your **Samsung Galaxy Watch8** with your **Samsung Galaxy S24 Ultra** phone and courtside **Bluetooth speaker**.

With FaultCall, your watch acts as a high-contrast physical umpire remote control while your phone operates the tennis scoring engine and broadcasts announcements through your connected speaker.

![FaultCall Logo](FaultCall/mobile/src/main/res/drawable/ic_faultcall_logo.xml)

---

## The Courtside Experience

1. **Arrive at the court** and connect your portable Bluetooth speaker to your Galaxy phone.
2. **Start a match** on FaultCall and place the phone in your bag or courtside bench.
3. **Play tennis** wearing your Galaxy Watch8.
4. Tap **FAULT!** → The speaker booms **"FAULT!"** with zero delay.
5. Tap **+ PT A** → The speaker announces the score (e.g., *"Fifteen-Love"*).
6. Reach 40-40 → The speaker announces *"Deuce"*.
7. Win the game → The speaker announces *"Game, Player A"*.
8. Made a mistake? Tap **UNDO** on your watch to reverse points, games, and service rotation.
9. Need to check the score? Tap **SCORE** anytime.

---

## Key Features

- **Circular Galaxy Watch8 UI**: Purpose-built high-contrast Wear OS Jetpack Compose interface with giant touch targets, sharp tactile haptic feedback, and hardware debounce protection.
- **"The Authority" Signature Umpire Voice**: Mature adult female voice inspired by Centre Court chair umpires with a refined Southern English / RP British accent, medium-low resonant register, and measured, commanding delivery.
- **Provider-Independent Voice Architecture**:
  - **Local Audio Library**: Pre-rendered fixed calls (FAULT, LET, OUT, DEUCE, ADVANTAGE, GAME, SET, MATCH POINT, DOUBLE FAULT).
  - **ElevenLabs AI Voice Integration**: Connects via secure backend proxy with intelligent on-device disk caching (`SHA-256` hashed keys).
  - **Android Text-to-Speech Fallback**: High-quality offline British English engine with customizable pitch and speech rate.
- **Announcement Priority Queue**: Urgent calls like FAULT immediately preempt standard score announcements. Obsolete scores are automatically purged on undo or point score updates.
- **Complete Tennis Scoring Engine**:
  - Standard tennis progression (Love, 15, 30, 40, Deuce, Advantage).
  - Standard 7-point tiebreak (at 6-6) and 10-point super tiebreak.
  - Complete tiebreak service rotation logic (point 1 first server, points 2 & 3 opponent, alternating every 2 points).
  - Standard advantage scoring and No-Ad (sudden death at 40-40).
  - Best-of-3 and Best-of-5 sets.
  - Unlimited snapshot-based Undo stack.
- **Bluetooth Speaker Diagnostics**: Detects Bluetooth A2DP, BLE, and wired output routes with one-tap speaker testing (*"FaultCall audio test. The announcement system is ready."*).
- **Courtside Background Reliability**: Background match foreground service keeps connection active while the phone screen is locked.
- **Persistent Match History**: Records completed matches with timestamps, final scores, and winners.

---

## Pre-Built APK Downloads

Pre-compiled, installable debug APKs are ready in the `release_apks/` directory:

| Device | Module | File Path | Size |
|--------|--------|-----------|------|
| **Phone (S24 Ultra)** | `:mobile` | [`release_apks/FaultCall-Mobile-v1.0.0.apk`](release_apks/FaultCall-Mobile-v1.0.0.apk) | 17.08 MB |
| **Watch (Galaxy Watch8)** | `:wear` | [`release_apks/FaultCall-GalaxyWatch8-v1.0.0.apk`](release_apks/FaultCall-GalaxyWatch8-v1.0.0.apk) | 20.66 MB |

---

## Quick Installation via ADB

Ensure ADB is in your PATH or use the local installation at `C:\Users\sahith\android-sdk\platform-tools\adb.exe`:

### 1. Install Phone APK
```powershell
& "C:\Users\sahith\android-sdk\platform-tools\adb.exe" -d install -r "release_apks\FaultCall-Mobile-v1.0.0.apk"
```

### 2. Install Galaxy Watch8 APK (via Wireless Debugging)
```powershell
# Pair watch (from Watch Settings -> Developer Options -> Wireless Debugging)
& "C:\Users\sahith\android-sdk\platform-tools\adb.exe" pair <WATCH_IP>:<PAIR_PORT> <PAIR_CODE>

# Connect
& "C:\Users\sahith\android-sdk\platform-tools\adb.exe" connect <WATCH_IP>:<WIRELESS_PORT>

# Install APK
& "C:\Users\sahith\android-sdk\platform-tools\adb.exe" -s <WATCH_IP>:<WIRELESS_PORT> install -r "release_apks\FaultCall-GalaxyWatch8-v1.0.0.apk"
```

*For complete details, see [`INSTALLATION_GUIDE.md`](INSTALLATION_GUIDE.md).*

---

## Building from Source

Requirements:
- Windows 10/11
- JDK 17 (installed in `C:\Users\sahith\android-tools\jdk-17`)
- Android SDK 34 (installed in `C:\Users\sahith\android-sdk`)

```powershell
cd FaultCall
$env:JAVA_HOME = "C:\Users\sahith\android-tools\jdk-17"

# Run all 33 unit and protocol tests
.\gradlew.bat test

# Build debug APKs for both Phone and Watch
.\gradlew.bat assembleDebug
```

---

## Automated Test Results

33 of 33 automated tests pass with 100% success rate:
- **`TennisScoringEngineTest`**: 28 tests (Love-all, 15-0, 30-0, 40-0, Deuce, Advantage, 6-0, 6-4, 7-5, 6-6 Tiebreak, 7-5 Tiebreak, 8-6 Tiebreak, Super Tiebreak, Best-of-3, Best-of-5, No-Ad, Service rotation, Tiebreak rotation, Undo point/game/set, Repeated undo, State persistence, Duplicate command rejection).
- **`CommandProtocolTest`**: 3 tests (Serialization, Acknowledgment, Deduplication window).
- **`AnnouncementQueueTest`**: 2 tests (Priority preemption, Obsolete score purging).

*See [`TEST_RESULTS.md`](TEST_RESULTS.md) for full execution logs.*

---

## Architectural Documentation

- [`DECISIONS.md`](DECISIONS.md) — Technical choices, Wearable Data Layer contract, voice architecture.
- [`PROJECT_STATUS.md`](PROJECT_STATUS.md) — 9-phase development tracker and Definition of Done.
- [`INSTALLATION_GUIDE.md`](INSTALLATION_GUIDE.md) — Step-by-step installation, wireless ADB setup, and courtside testing guide.
- [`server/voice_proxy.py`](FaultCall/server/voice_proxy.py) — Reference Python Flask backend proxy for ElevenLabs API integration.
