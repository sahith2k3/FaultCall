# FaultCall — Complete Installation & Hardware Verification Guide

This guide provides practical, step-by-step instructions to install and configure **FaultCall** on your **Samsung Galaxy S24 Ultra** (Phone) and **Samsung Galaxy Watch8** (Smartwatch), connect your Bluetooth speaker, and test the entire system on the tennis court.

---

## 1. APK Files Overview

Pre-built debug APKs are located in the workspace:
- **Phone Application (S24 Ultra)**:  
  `release_apks\FaultCall-Mobile-v1.0.0.apk`  
  *(Source path: `FaultCall\mobile\build\outputs\apk\debug\mobile-debug.apk`)*
- **Smartwatch Application (Galaxy Watch8)**:  
  `release_apks\FaultCall-GalaxyWatch8-v1.0.0.apk`  
  *(Source path: `FaultCall\wear\build\outputs\apk\debug\wear-debug.apk`)*

ADB executable is installed at:  
`C:\Users\sahith\android-sdk\platform-tools\adb.exe`

---

## 2. Phone Installation (Samsung Galaxy S24 Ultra)

### Step 2.1 — Enable Developer Options & USB Debugging
1. On your Galaxy S24 Ultra, open **Settings** → **About phone** → **Software information**.
2. Tap **Build number** 7 times until you see the message *"Developer mode has been enabled"*.
3. Go back to **Settings** → **Developer options**.
4. Enable **USB debugging**.

### Step 2.2 — Install via USB Cable
1. Connect your phone to your Windows PC with a USB-C cable.
2. If prompted on your phone, select **"Always allow from this computer"** and tap **Allow**.
3. In a Windows PowerShell terminal, run:
```powershell
& "C:\Users\sahith\android-sdk\platform-tools\adb.exe" devices
```
You should see your phone listed, e.g. `RFXXXXXXXXX device`.

4. Install the phone APK:
```powershell
& "C:\Users\sahith\android-sdk\platform-tools\adb.exe" -d install -r "c:\Users\sahith\Documents\antigravity\magical-bohr\release_apks\FaultCall-Mobile-v1.0.0.apk"
```
5. You will see `Success`. The **FaultCall** app icon (Tennis ball with red call badge) will appear on your phone home screen.

### Step 2.3 — Grant Permissions
Launch FaultCall on your phone and allow:
- **Notifications** (required for the background match service)
- **Nearby Devices / Bluetooth** (required for speaker routing detection)

---

## 3. Smartwatch Installation (Samsung Galaxy Watch8)

### Step 3.1 — Enable Wireless Debugging on Galaxy Watch8
1. Ensure your Watch8 is connected to the **same Wi-Fi network** as your Windows PC.
2. On your Galaxy Watch8, open **Settings** → **About watch** → **Software info**.
3. Tap **Software version** 7 times to enable Developer options.
4. Go back to **Settings** → **Developer options**.
5. Enable **ADB debugging**.
6. Scroll down and enable **Wireless debugging**.
7. Tap **Wireless debugging** to view the **IP address and Port** (e.g., `192.168.1.50:41235`).
8. Tap **Pair new device with pairing code**. Note the **Wi-Fi pairing code** and the **IP & port for pairing**.

### Step 3.2 — Pair & Connect via ADB
In PowerShell on your Windows PC:
```powershell
# 1. Pair with the watch (replace with IP, port, and code displayed on your watch)
& "C:\Users\sahith\android-sdk\platform-tools\adb.exe" pair 192.168.1.50:PORT_FOR_PAIRING PAIRING_CODE

# 2. Connect to the watch's wireless debugging port
& "C:\Users\sahith\android-sdk\platform-tools\adb.exe" connect 192.168.1.50:WIRELESS_PORT

# 3. Verify watch connection
& "C:\Users\sahith\android-sdk\platform-tools\adb.exe" devices
```
You should see `192.168.1.50:WIRELESS_PORT device`.

### Step 3.3 — Install the Wear OS APK
```powershell
& "C:\Users\sahith\android-sdk\platform-tools\adb.exe" -s 192.168.1.50:WIRELESS_PORT install -r "c:\Users\sahith\Documents\antigravity\magical-bohr\release_apks\FaultCall-GalaxyWatch8-v1.0.0.apk"
```
Once complete, you will see `Success`. **FaultCall** is now installed on your Galaxy Watch8!

---

## 4. Courtside Bluetooth Speaker Setup

1. **Pair your Bluetooth speaker** (e.g. JBL Flip, Bose SoundLink, UE Megaboom) to your **Samsung Galaxy S24 Ultra** through the phone's standard Bluetooth settings.
2. Open **FaultCall** on your phone.
3. Look at the **Diagnostics Card** at the top of the dashboard:
   - It will display: `BT Speaker Connected (Bluetooth A2DP: [Your Speaker Name])`.
4. Tap **TEST SPEAKER**.
   - Your speaker will immediately speak:
     > *"FaultCall audio test. The announcement system is ready."*
5. Set your speaker volume to an audible court level.

---

## 5. End-to-End Tennis Match Verification Checklist

Follow this workflow to verify the full physical experience on the court:

### 1. Launch & Connection Check
- [ ] Open **FaultCall** on your Samsung phone.
- [ ] Open **FaultCall** on your Galaxy Watch8.
- [ ] On the Watch8 screen, verify the top header shows a green connection dot and `0-0`.
- [ ] On the phone dashboard, verify `Watch Connected: Galaxy Watch8`.

### 2. FAULT Announcement Test
- [ ] Tap the giant red **FAULT!** button on your Watch8.
- [ ] Watch delivers an instant sharp haptic pulse (`heavyFault`).
- [ ] Bluetooth speaker immediately booms: **"FAULT!"**.
- [ ] The score remains unchanged (`0-0`).

### 3. Point Scoring Test
- [ ] Tap **+ PT A** on your Watch8.
- [ ] Watch delivers click haptic; score updates optimistically to `15 - 0`.
- [ ] Speaker announces: *"Fifteen-Love."*
- [ ] Tap **+ PT B** on your Watch8.
- [ ] Speaker announces: *"Fifteen-all."*

### 4. Score Request Test
- [ ] Tap **SCORE** on your Watch8.
- [ ] Speaker announces: *"Fifteen-all."*

### 5. Deuce & Advantage Test
- [ ] Award points to reach 40-40.
- [ ] Speaker announces: *"Deuce."*
- [ ] Tap **+ PT A**. Speaker announces: *"Advantage, Player A."*
- [ ] Tap **+ PT B**. Speaker announces: *"Deuce."*
- [ ] Tap **+ PT A** twice. Speaker announces: *"Game, Player A. Score is 1 games to 0."*
- [ ] Current server rotates automatically.

### 6. Undo Test
- [ ] Tap **UNDO** on your Watch8.
- [ ] Watch delivers double pulse haptic; score rolls back to the exact previous state (`Advantage, Player A`).
- [ ] Speaker announces: *"Score restored. Advantage, Player A."*

### 7. Background / Screen-Off Verification
- [ ] Lock your Samsung phone screen and place the phone courtside in your bag or bench.
- [ ] The persistent foreground notification stays active: *"FaultCall — Match Active"*.
- [ ] Tap **FAULT!** or **+ PT A** from your Watch8 across the court.
- [ ] The Bluetooth speaker announces the call instantly without waking the phone screen.

---

## 6. Troubleshooting Guide

| Issue | Cause | Solution |
|-------|-------|----------|
| Watch displays `DISCONNECTED` | Phone and Watch Bluetooth or Data Layer not synced | Ensure Galaxy Wearable app is connected; tap **MORE ⚙** → **SYNC FROM PHONE** on watch. |
| Audio plays from phone speaker, not Bluetooth speaker | Bluetooth speaker not set as active media output | Open phone Quick Settings → Media Output → select your Bluetooth speaker; tap **TEST SPEAKER** in FaultCall. |
| Wireless debugging fails to connect | IP or port changed on Watch8 | Watch8 generates a new port on each Wi-Fi reconnect. Verify port in Developer Options → Wireless Debugging. |
| TTS sounds too quiet | Speech volume is linked to media volume | Turn up media volume on your Samsung phone using physical volume rocker. |
| Watch button double-fires | Accidental double tap | FaultCall includes a hardware debounce filter (250ms). Ensure single firm tap. |
