package com.cabelas.smokercompanion.data.model

data class PelletHopperState(
    val levelPercentage: Int = 100,
    val isLowPelletAlert: Boolean = false,
    val woodFlavor: String = "Hickory Blend"
)
