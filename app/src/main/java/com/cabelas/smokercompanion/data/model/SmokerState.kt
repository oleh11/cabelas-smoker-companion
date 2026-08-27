package com.cabelas.smokercompanion.data.model

data class SmokerState(
    val powerMode: SmokerPowerMode = SmokerPowerMode.OFF,
    val currentChamberTempF: Int = 72,
    val targetChamberTempF: Int = 225,
    val smokeLevel: Int = 5, // 1 to 10
    val probe1: MeatProbe = MeatProbe(id = 1, isConnected = true, currentTempF = 68, targetTempF = 203, meatType = "Brisket"),
    val probe2: MeatProbe = MeatProbe(id = 2, isConnected = true, currentTempF = 65, targetTempF = 165, meatType = "Pork"),
    val probe3: MeatProbe = MeatProbe(id = 3, isConnected = false),
    val probe4: MeatProbe = MeatProbe(id = 4, isConnected = false),
    val hopper: PelletHopperState = PelletHopperState(levelPercentage = 85, isLowPelletAlert = false),
    val isAugerActive: Boolean = false,
    val isFanActive: Boolean = false,
    val isIgniterActive: Boolean = false,
    val errorCode: String? = null,
    val cookStartTimeMs: Long? = null,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.FAHRENHEIT,
    val wifiConnected: Boolean = false,
    val wifiSsid: String? = null
) {
    val activeProbes: List<MeatProbe>
        get() = listOf(probe1, probe2, probe3, probe4).filter { it.isConnected }

    val formattedCookDuration: String
        get() {
            val start = cookStartTimeMs ?: return "00:00:00"
            val diffSec = (System.currentTimeMillis() - start) / 1000
            val hours = diffSec / 3600
            val minutes = (diffSec % 3600) / 60
            val seconds = diffSec % 60
            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
}
