package com.cabelas.smokercompanion.data.ble

import com.cabelas.smokercompanion.data.model.SmokerPowerMode
import com.cabelas.smokercompanion.data.model.SmokerState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SmokerPacketCodecTest {

    @Test
    fun testEncodeAndDecodeStatusPacket() {
        val initial = SmokerState(
            powerMode = SmokerPowerMode.RUNNING,
            currentChamberTempF = 225,
            targetChamberTempF = 250,
            smokeLevel = 8,
            isAugerActive = true,
            isFanActive = true
        )

        val packet = SmokerPacketCodec.encodeMockStatusPacket(initial)
        assertNotNull(packet)
        assertTrue(packet.isNotEmpty())

        val decoded = SmokerPacketCodec.decodeStatusPacket(packet, SmokerState())
        assertNotNull(decoded)
        assertEquals(SmokerPowerMode.RUNNING, decoded?.powerMode)
        assertEquals(225, decoded?.currentChamberTempF)
        assertEquals(250, decoded?.targetChamberTempF)
        assertEquals(8, decoded?.smokeLevel)
        assertTrue(decoded?.isAugerActive == true)
        assertTrue(decoded?.isFanActive == true)
    }

    @Test
    fun testBuildSetTemperaturePacket() {
        val packet = SmokerPacketCodec.buildSetTemperaturePacket(225)
        assertEquals(0xA5.toByte(), packet[0])
        assertEquals(0x04.toByte(), packet[1]) // Length
        assertEquals(SmokerPacketCodec.CMD_SET_TARGET_TEMP, packet[2])
        val temp = ((packet[3].toInt() and 0xFF) shl 8) or (packet[4].toInt() and 0xFF)
        assertEquals(225, temp)
    }

    @Test
    fun testBuildSetSmokeLevelPacket() {
        val packet = SmokerPacketCodec.buildSetSmokeLevelPacket(9)
        assertEquals(0xA5.toByte(), packet[0])
        assertEquals(SmokerPacketCodec.CMD_SET_SMOKE_LEVEL, packet[2])
        assertEquals(9.toByte(), packet[3])
    }

    @Test
    fun testBuildWifiProvisionPacket() {
        val packet = SmokerPacketCodec.buildWifiProvisionPacket("MyHomeBBQ", "SecretPass123")
        assertEquals(0xA5.toByte(), packet[0])
        assertEquals(SmokerPacketCodec.CMD_WIFI_PROVISION, packet[2])
    }
}
