package com.cabelas.smokercompanion.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CookPresetTest {

    @Test
    fun testDefaultPresetsExist() {
        val presets = CookPreset.DEFAULT_PRESETS
        assertTrue(presets.isNotEmpty())

        val brisket = presets.find { it.id == "brisket" }
        assertTrue(brisket != null)
        assertEquals(225, brisket?.recommendedGrillTempF)
        assertEquals(203, brisket?.targetInternalTempF)
        assertEquals(8, brisket?.recommendedSmokeLevel)
    }

    @Test
    fun testMeatProbeProgressCalculation() {
        val probe = MeatProbe(
            id = 1,
            currentTempF = 150,
            targetTempF = 200,
            isConnected = true
        )
        assertEquals(0.75f, probe.progress, 0.001f)
    }

    @Test
    fun testTemperatureUnitConversion() {
        val unitF = TemperatureUnit.FAHRENHEIT
        val unitC = TemperatureUnit.CELSIUS

        assertEquals("225°F", unitF.format(225))
        assertEquals("107°C", unitC.format(225))
        assertEquals(107, unitC.convertFromF(225))
    }
}
