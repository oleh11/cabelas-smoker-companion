package com.cabelas.smokercompanion.data.model

data class CookDataPoint(
    val timestampMs: Long,
    val grillTempF: Int,
    val probe1TempF: Int,
    val probe2TempF: Int,
    val probe3TempF: Int,
    val probe4TempF: Int
)
