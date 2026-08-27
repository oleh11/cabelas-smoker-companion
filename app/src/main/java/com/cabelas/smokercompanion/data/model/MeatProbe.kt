package com.cabelas.smokercompanion.data.model

data class MeatProbe(
    val id: Int, // 1, 2, 3, or 4
    val name: String = "Probe $id",
    val currentTempF: Int = 0,
    val targetTempF: Int = 0,
    val isConnected: Boolean = false,
    val isAlarmEnabled: Boolean = false,
    val isTargetReached: Boolean = false,
    val meatType: String? = null
) {
    val progress: Float
        get() = if (targetTempF > 0 && currentTempF > 0) {
            (currentTempF.toFloat() / targetTempF.toFloat()).coerceIn(0f, 1f)
        } else 0f
}
