# FaultCall — System Architecture

FaultCall is composed of three primary modules:
- `:shared` — Core scoring engine, tiebreak state machine, and data models.
- `:mobile` — Android Phone application managing audio routing, voice providers, and audio focus.
- `:wear` — Wear OS application providing high-contrast courtside remote controls.

---

## High-Level Component Flow

```
┌─────────────────────────┐          Wearable DataLayer / Bluetooth          ┌─────────────────────────┐
│     Wear OS Device      │ ───────────────────────────────────────────────> │    Android Phone Host   │
│  (Galaxy Watch 8 / 5)   │ <─────────────────────────────────────────────── │  (Galaxy S24 Ultra)     │
└─────────────────────────┘                   State Sync                     └─────────────────────────┘
                                                                                          │
                                                                                          ▼
                                                                             ┌─────────────────────────┐
                                                                             │  AnnouncementManager    │
                                                                             │  • Priority Queue       │
                                                                             │  • Audio Ducking (5%)   │
                                                                             └─────────────────────────┘
                                                                                          │
                                                                         ┌────────────────┼────────────────┐
                                                                         ▼                ▼                ▼
                                                                   Local Clips     ElevenLabs AI      Android TTS
                                                                   (0ms latency)    (RP British)       (Offline)
                                                                         │                │                │
                                                                         └────────────────┼────────────────┘
                                                                                          ▼
                                                                             Bluetooth Outdoor Speaker
                                                                               (Xiaomi Sound Outdoor)
```

---

## Audio Ducking & Restoration Lifecycle

When an announcement is scheduled:
1. `AnnouncementManager` requests transient audio ducking focus using `USAGE_ASSISTANCE_NAVIGATION_GUIDANCE` with `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK`.
2. The Android audio policy manager attenuates active background music (such as Spotify) to ~5% volume.
3. If back-to-back announcements are queued (e.g. score changes in rapid succession), ducking focus is retained without redundant focus requests.
4. When the announcement queue is empty, `AnnouncementManager` abandons focus via `abandonAudioFocusRequest()`, immediately restoring background music to 100% volume.
