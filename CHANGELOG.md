# Changelog

All notable changes to the **FaultCall** project are documented here.

## [1.0.0] - 2026-11-12

### Initial Release Highlights

#### Mobile (:mobile)
- **Tennis Match Engine**: Complete rules engine supporting standard game, advantage, No-Ad sudden death, 7-point tiebreak, and 10-point super tiebreak.
- **Audio Ducking System**: 5% transient ducking for background music apps (Spotify, YouTube Music) using Android `USAGE_ASSISTANCE_NAVIGATION_GUIDANCE` audio attributes.
- **Unducking Lifecycle**: Centralized focus state machine in `AnnouncementManager` with leak-proof release upon queue completion.
- **Voice Synthesis Engine**:
  - Offline pre-recorded audio library for signature umpire calls.
  - ElevenLabs AI voice integration with on-device SHA-256 disk caching.
  - British English Text-to-Speech fallback with customizable pitch and speech rate.
  - Automatic speech sanitation eliminating hyphens and normalizing punctuation.
- **Background Wake-Up**: Standalone background activity launch allowing smartwatch buttons to wake the phone even when sleeping or locked.

#### Wear OS (:wear)
- **High-Contrast Courtside UI**: Large, bold touch targets optimized for round Wear OS displays.
- **Tactile Haptic Feedback**: Distinct haptic click vibrations for point awarded, fault call, undo, and set victory.
- **Hardware Debounce Protection**: 300ms input debounce filter eliminating accidental double-taps.
- **Full Action Palette**: Primary screen (FAULT!, + PT A, + PT B, SCORE, UNDO) and secondary screen (LET, DOUBLE FAULT, Service Toggle, Reset Match).

#### Shared Core (:shared)
- Pure Kotlin multiplatform shared logic with 100% test coverage across scoring, service rotations, priority queues, and network payloads.
