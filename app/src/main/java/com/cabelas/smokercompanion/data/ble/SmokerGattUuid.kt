package com.cabelas.smokercompanion.data.ble

import java.util.UUID

object SmokerGattUuid {
    // Primary Camp Chef / Cabela's Pellet Grill Service UUIDs
    val SERVICE_CAMP_CHEF_MAIN: UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
    val CHAR_READ_NOTIFY: UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
    val CHAR_WRITE_COMMAND: UUID = UUID.fromString("0000ffe2-0000-1000-8000-00805f9b34fb")

    // Nordic UART Service (NUS) profile widely used across custom smart grill controllers
    val SERVICE_NUS: UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
    val CHAR_NUS_RX: UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e") // Write to grill
    val CHAR_NUS_TX: UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e") // Read/Notify from grill

    // Wi-Fi Configuration Service
    val SERVICE_WIFI_PROVISION: UUID = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb")
    val CHAR_WIFI_SSID_PASS: UUID = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb")
    val CHAR_WIFI_STATUS: UUID = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb")

    // Standard Client Characteristic Configuration Descriptor (CCCD)
    val CLIENT_CHARACTERISTIC_CONFIG: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    val MATCHING_DEVICE_PREFIXES = listOf(
        "Cabelas",
        "Cabela",
        "CampChef",
        "Camp Chef",
        "ProSeries",
        "Grill",
        "SmokeMaster",
        "PID-"
    )
}
