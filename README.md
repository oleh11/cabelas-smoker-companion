# Cabela's Pro Series 36" Wi-Fi Pellet Grill Companion App (Android)

A modern, high-performance Android companion application designed specifically for the **Cabela's Pro Series 36" Wi-Fi Pellet Grill** (powered by Camp Chef PID Wi-Fi/BLE controller technology).

---

## 🌟 Highlights & Features

- **Zero Deprecated APIs & Modern Stack**:
  - **Android 15 (Target SDK 35)**, Min SDK 26 (Android 8.0 Oreo).
  - Modern API 33+ Bluetooth Low Energy GATT callbacks (`onCharacteristicRead`, `onCharacteristicChanged`, `writeCharacteristic`, `writeDescriptor`).
  - Granular runtime permissions: `BLUETOOTH_SCAN` (`neverForLocation`), `BLUETOOTH_CONNECT`, and `POST_NOTIFICATIONS`.
  - **100% Jetpack Compose** with Material 3 Dark Charcoal & Ember Theme.
  - Kotlin 2.1.0 with Compose Compiler Gradle plugin and Version Catalog (`libs.versions.toml`).
- **Dual Connectivity (BLE + Wi-Fi)**:
  - **Direct Bluetooth Low Energy (BLE)**: Connects directly to the smoker controller with sequential GATT operation queue preventing error 133 collisions.
  - **2.4 GHz Wi-Fi Provisioning**: Transmits home Wi-Fi credentials over BLE so the grill can join your home network.
- **Grill Simulator Engine**:
  - Built-in realistic thermal physics engine simulating PID heating curves, 4 meat probe thermal absorption, and pellet hopper consumption for full testing without the physical grill.
- **Background Cook Monitoring**:
  - Android Foreground Service with live temperature status in the notification shade and audible/vibrating alarms when meat probes reach target temperatures.
- **BBQ Pitmaster Recipes & Presets**:
  - Built-in profiles for Texas Brisket, Pork Shoulder, 3-2-1 Ribs, Smoked Salmon, Spatchcock Turkey, and Reverse-Sear Steaks that automatically set grill temperature, smoke level, and probe alarm thresholds.

---

## 📱 Project Architecture

```
com.cabelas.smokercompanion/
├── SmokerApp.kt                 # Application class & service initializer
├── MainActivity.kt              # Edge-to-edge Compose host & permissions
├── data/
│   ├── ble/                     # Modern BLE manager, GATT UUIDs & packet codec
│   ├── model/                   # Domain models (SmokerState, MeatProbe, Presets)
│   ├── repository/              # Reactive repository layer with StateFlow
│   ├── simulator/               # Realistic thermal & physics simulator engine
│   └── wifi/                    # Wi-Fi network scanner & provisioning
├── service/
│   └── CookMonitoringService.kt # Foreground service with persistent notification & alarms
└── ui/
    ├── components/              # Radial temp gauge, probe cards, smoke dial, live charts
    ├── navigation/              # Compose navigation suite & destinations
    ├── screens/                 # Dashboard, Scanner, BBQ Planner, Wi-Fi, Settings
    └── theme/                   # Material 3 BBQ Ember & Charcoal theme
```

---

## 🚀 Building & Running

### Option 1: Android Studio
1. Open Android Studio.
2. Select **Open** and choose `C:\Users\oleha\.gemini\antigravity\scratch\cabelas-smoker-companion`.
3. Allow Gradle to sync and click **Run** (Green Play button).

### Option 2: Command Line
```powershell
cd C:\Users\oleha\.gemini\antigravity\scratch\cabelas-smoker-companion
.\gradlew.bat assembleDebug testDebugUnitTest
```
