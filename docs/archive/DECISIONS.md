# FaultCall — Architectural & Technical Decisions

## 1. Multi-Module Project Structure
- **Root Directory**: `FaultCall/`
- **Modules**:
  - `:shared`: Pure JVM / Android library containing data models, immutable game state, tennis scoring rules, service rotation calculator, undo/redo snapshot stack, command definitions, and JSON serialization. Allows 100% test coverage using fast JUnit tests without needing an Android emulator or device for pure business logic.
  - `:mobile`: Phone application (`com.faultcall.mobile`). Target SDK 34 (Android 14, S24 Ultra), Min SDK 26. Manages the official match state, announcement audio playback (via Local Audio clips, ElevenLabs backend proxy client, or TextToSpeech fallback), audio routing detection, Bluetooth speaker diagnostics, and Wearable Data Layer listener service.
  - `:wear`: Smartwatch application (`com.faultcall.wear`). Target SDK 34 (Wear OS 5 / Galaxy Watch8), Min SDK 30. Wear OS Jetpack Compose UI designed specifically for circular high-contrast outdoor usability, large touch targets, haptic feedback, debounced command transmission, and local optimistic display synchronized with the phone.

## 2. Voice Architecture & "The Authority" Signature Umpire
- **Voice Identity**: Mature adult female, Southern English/RP-influenced British accent, medium-low register, crisp and authoritative.
- **Provider-Independent Voice Architecture**:
  - `VoiceProvider` interface:
    - `ElevenLabsVoiceProvider`: Connects to backend proxy or direct configurable endpoint, with persistent disk cache for synthesized announcements (key: `voiceId_hash(text)_settings`).
    - `LocalAudioProvider`: Pre-bundled fixed calls (FAULT, LET, OUT, DEUCE, ADVANTAGE, GAME, SET, MATCH POINT, DOUBLE FAULT, etc.).
    - `AndroidTtsVoiceProvider`: Built-in offline fallback supporting pitch/rate customization, engine selection, and British English locale (`en_GB`).
- **Announcement Priority Queue**:
  - Priority 1: `FAULT`, `DOUBLE_FAULT` (instant, preempts low priority)
  - Priority 2: `LET`
  - Priority 3: `GAME`, `SET`, `MATCH`
  - Priority 4: `DEUCE`, `ADVANTAGE`, `SET_POINT`, `MATCH_POINT`
  - Priority 5: Normal Score announcements (`15-0`, `30-15`, etc.)
  - Priority 6: Informational ("Audio test ready", "Change of ends")
- **Cancellation of Obsolete Score Announcements**:
  - When an UNDO command arrives or a new point is awarded before an older score announcement finishes or starts playing, obsolete score announcements in the queue are safely purged.

## 3. Communication Protocol (Wearable Data Layer)
- **Transport**: Google Play Services Wearable `MessageClient` & `DataClient`.
- **Command Envelope**:
  - `commandId` (UUID string)
  - `type` (e.g. `ANNOUNCE_FAULT`, `AWARD_POINT_A`, `AWARD_POINT_B`, `UNDO`, `ANNOUNCE_SCORE`, `ANNOUNCE_LET`, etc.)
  - `timestamp` (epoch millis)
  - `matchId` (string)
  - `payload` (optional JSON string)
- **Deduplication**:
  - Mobile maintains a recent sliding window of processed command UUIDs (last 50 commands). Retried messages with duplicate IDs are acknowledged without re-applying score mutations.
- **State Synchronization**:
  - Mobile updates `MatchState` in the Wearable `DataClient` at path `/faultcall/match_state`, providing automatic data replication across devices, caching on the watch even if temporarily disconnected.

## 4. Background Execution & Tennis Session Reliability
- During an active tennis match, the phone app starts a `MatchSessionForegroundService` with notification channel and persistent notification ("FaultCall Match Active — Listening for Watch8 Calls").
- This ensures the Android system does not kill the phone application while the user's phone is sitting courtside in a bag or bench with the screen turned off.

## 5. UI/UX Design System
- **Wear OS**: Dark theme (charcoal `#121212`), high contrast, circular Wear Compose layout.
  - Giant Red `#E53935` FAULT button at the bottom for zero-miss thumb tapping while running or serving.
  - Green `#43A047` point buttons for Player A and Player B.
  - Cyan `#00ACC1` SCORE button.
  - Vibrant yellow/amber server tennis ball indicator (`🎾`).
  - Haptic feedback pattern on tap (`VibrationEffect.createOneShot`).
- **Mobile**: Material 3 sports dashboard with dark theme, large current score banner, Bluetooth audio route indicator, Test Speaker button, and voice settings.
