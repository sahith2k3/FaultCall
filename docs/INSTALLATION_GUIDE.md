# FaultCall — Sideloading & Installation Guide

This guide walks through installing **FaultCall** on your Android smartphone and Wear OS smartwatch via ADB.

---

## 1. Phone Installation

1. Connect your phone to your computer via USB (or wireless ADB).
2. Confirm device connection:
   ```bash
   adb devices
   ```
3. Install the phone APK:
   ```bash
   adb install -r releases/FaultCall-Mobile-v1.0.0.apk
   ```

---

## 2. Wear OS Smartwatch Installation (Over Wi-Fi)

1. Connect your watch and computer to the **same Wi-Fi network**.
2. On your watch, open **Settings > Developer options**:
   - Turn **ADB debugging** ON.
   - Turn **Wireless debugging** ON.
3. Tap **Pair new device** in Wireless debugging. You will see:
   - Wi-Fi pairing code (e.g. `123456`)
   - IP address & port (e.g. `192.168.1.100:41235`)
4. Pair with the watch from your terminal:
   ```bash
   adb pair <WATCH_IP>:<PAIR_PORT> <PAIR_CODE>
   ```
5. Note the main wireless debugging IP and port (e.g. `192.168.1.100:38501`), then connect:
   ```bash
   adb connect <WATCH_IP>:<CONNECT_PORT>
   ```
6. Install the watch APK:
   ```bash
   adb -s <WATCH_IP>:<CONNECT_PORT> install -r releases/FaultCall-GalaxyWatch8-v1.0.0.apk
   ```

---

## 3. Pairing Watch & Phone

1. Open FaultCall on your phone.
2. Open FaultCall on your watch.
3. The phone dashboard will indicate **"Watch Connected"** with a green indicator.
4. Tap **TEST SPEAKER** on your phone to confirm Bluetooth speaker audio output.
