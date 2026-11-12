# FaultCall — Releases & Packages Matrix

This directory hosts pre-built, tested release artifacts for **FaultCall v1.0.0**.

---

## Downloadable APKs

| Target Device | Module | Architecture | File Name | Size | SHA-256 Checksum |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Android Phone**<br>(Galaxy S24 Ultra & Android 8.0+) | `:mobile` | Universal (`arm64-v8a`, `armeabi-v7a`, `x86_64`) | [`FaultCall-Mobile-v1.0.0.apk`](releases/FaultCall-Mobile-v1.0.0.apk) | ~17.2 MB | `70BC7CB9F73153C1DAE91D4FC747E4F586D0D39ED7BB8B8C93EFBF6BF020D83D` |
| **Wear OS Smartwatch**<br>(Galaxy Watch8, Wear OS 5) | `:wear` | Universal Wear OS | [`FaultCall-GalaxyWatch8-v1.0.0.apk`](releases/FaultCall-GalaxyWatch8-v1.0.0.apk) | ~20.7 MB | `3DAD9F2BC15AD9EBF48272FA768C044AC49DA1EA007511537A981C5B8E15F36E` |
| **Wear OS Smartwatch**<br>(Galaxy Watch5 Pro, Wear OS 4.5) | `:wear` | Universal Wear OS | [`FaultCall-GalaxyWatch5Pro-v1.0.0.apk`](releases/FaultCall-GalaxyWatch5Pro-v1.0.0.apk) | ~20.7 MB | `3DAD9F2BC15AD9EBF48272FA768C044AC49DA1EA007511537A981C5B8E15F36E` |
| **Wear OS Universal**<br>(Pixel Watch, TicWatch, Wear OS 3+) | `:wear` | Universal Wear OS | [`FaultCall-Wear-Universal-v1.0.0.apk`](releases/FaultCall-Wear-Universal-v1.0.0.apk) | ~20.7 MB | `8E2095D42DF389BBA45D6AC543443F228E08ABE18EC6DFE876DF0FE5D4DF7688` |

---

## Quick Installation via ADB

### 1. Install on Phone
Connect your phone via USB or Wi-Fi debugging:
```bash
adb install -r releases/FaultCall-Mobile-v1.0.0.apk
```

### 2. Install on Wear OS Smartwatch (Wireless)
Enable Wireless Debugging on your watch (**Settings > Developer options > Wireless debugging**):
```bash
# Pair your watch
adb pair <WATCH_IP>:<PAIRING_PORT> <PAIRING_CODE>

# Connect to the watch
adb connect <WATCH_IP>:<CONNECT_PORT>

# Sideload the watch application
adb -s <WATCH_IP>:<CONNECT_PORT> install -r releases/FaultCall-GalaxyWatch8-v1.0.0.apk
```

---

## Verified Hardware Matrix

- **Phone**: Samsung Galaxy S24 Ultra (Android 14, One UI 6.1)
- **Watch 1**: Samsung Galaxy Watch8 (`SM-L335F`, Wear OS 5)
- **Watch 2**: Samsung Galaxy Watch5 Pro LTE (`SM-R925U`, Wear OS 4.5)
- **Audio Output**: Xiaomi Sound Outdoor Bluetooth Speaker (A2DP)
- **Background Audio App**: Spotify / YouTube Music (tested with 5% transient ducking and instant unduck restoration)
