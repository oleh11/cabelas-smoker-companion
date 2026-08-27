package com.cabelas.smokercompanion.data.simulator

import com.cabelas.smokercompanion.data.model.SmokerPowerMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockSmokerEngineTest {

    @Test
    fun testSetTargetAndPowerMode() {
        val engine = MockSmokerEngine()
        engine.setTargetTemperature(275)
        assertEquals(275, engine.state.value.targetChamberTempF)

        engine.setSmokeLevel(9)
        assertEquals(9, engine.state.value.smokeLevel)

        engine.setPowerMode(SmokerPowerMode.SHUTDOWN)
        assertEquals(SmokerPowerMode.SHUTDOWN, engine.state.value.powerMode)
    }

    @Test
    fun testProbeTargetAlarmTrigger() {
        val engine = MockSmokerEngine()
        engine.setProbeTarget(1, 160)
        assertEquals(160, engine.state.value.probe1.targetTempF)
        assertTrue(engine.state.value.probe1.isAlarmEnabled)
    }
}
