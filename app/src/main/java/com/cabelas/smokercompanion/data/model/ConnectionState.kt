package com.cabelas.smokercompanion.data.model

sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data class Scanning(val message: String = "Searching for smoker...") : ConnectionState
    data class Connecting(val deviceName: String, val address: String) : ConnectionState
    data class Authenticating(val deviceName: String) : ConnectionState
    data class Connected(
        val deviceName: String,
        val address: String,
        val isWifiActive: Boolean = false,
        val signalDbm: Int = -60
    ) : ConnectionState
    data class SimulatorActive(val scenario: String = "Live Simulation") : ConnectionState
    data class Error(val message: String) : ConnectionState
}

enum class SmokerPowerMode {
    OFF,
    STARTUP,
    RUNNING,
    SHUTDOWN,
    ERROR
}
