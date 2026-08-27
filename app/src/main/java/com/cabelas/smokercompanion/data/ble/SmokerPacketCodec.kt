package com.cabelas.smokercompanion.data.ble

import com.cabelas.smokercompanion.data.model.MeatProbe
import com.cabelas.smokercompanion.data.model.PelletHopperState
import com.cabelas.smokercompanion.data.model.SmokerPowerMode
import com.cabelas.smokercompanion.data.model.SmokerState

object SmokerPacketCodec {
    private const val HEADER: Byte = 0xA5.toByte()

    const val CMD_STATUS_REPORT: Byte = 0x00
    const val CMD_SET_TARGET_TEMP: Byte = 0x01
    const val CMD_SET_SMOKE_LEVEL: Byte = 0x02
    const val CMD_POWER_STARTUP: Byte = 0x04
    const val CMD_POWER_SHUTDOWN: Byte = 0x03
    const val CMD_WIFI_PROVISION: Byte = 0x10

    /**
     * Parses incoming byte payload from the smoker controller into a [SmokerState].
     */
    fun decodeStatusPacket(bytes: ByteArray, currentState: SmokerState): SmokerState? {
        if (bytes.size < 18 || bytes[0] != HEADER) {
            return null
        }

        // Verify simple additive checksum
        var sum = 0
        for (i in 0 until bytes.size - 1) {
            sum += (bytes[i].toInt() and 0xFF)
        }
        val expectedChecksum = (sum and 0xFF).toByte()
        if (bytes.last() != expectedChecksum) {
            // If checksum fails, reject packet
            return null
        }

        val powerModeByte = bytes[2].toInt() and 0xFF
        val powerMode = when (powerModeByte) {
            0 -> SmokerPowerMode.OFF
            1 -> SmokerPowerMode.STARTUP
            2 -> SmokerPowerMode.RUNNING
            3 -> SmokerPowerMode.SHUTDOWN
            else -> SmokerPowerMode.ERROR
        }

        val currentChamberTemp = ((bytes[3].toInt() and 0xFF) shl 8) or (bytes[4].toInt() and 0xFF)
        val targetChamberTemp = ((bytes[5].toInt() and 0xFF) shl 8) or (bytes[6].toInt() and 0xFF)
        val smokeLevel = (bytes[7].toInt() and 0xFF).coerceIn(1, 10)

        val p1Temp = ((bytes[8].toInt() and 0xFF) shl 8) or (bytes[9].toInt() and 0xFF)
        val p2Temp = ((bytes[10].toInt() and 0xFF) shl 8) or (bytes[11].toInt() and 0xFF)
        val p3Temp = ((bytes[12].toInt() and 0xFF) shl 8) or (bytes[13].toInt() and 0xFF)
        val p4Temp = ((bytes[14].toInt() and 0xFF) shl 8) or (bytes[15].toInt() and 0xFF)

        val pelletPct = (bytes[16].toInt() and 0xFF).coerceIn(0, 100)
        val flags = bytes[17].toInt() and 0xFF

        val isAugerActive = (flags and 0x01) != 0
        val isFanActive = (flags and 0x02) != 0
        val isIgniterActive = (flags and 0x04) != 0
        val isLowPellet = pelletPct < 20

        return currentState.copy(
            powerMode = powerMode,
            currentChamberTempF = currentChamberTemp,
            targetChamberTempF = targetChamberTemp,
            smokeLevel = smokeLevel,
            probe1 = currentState.probe1.copy(
                isConnected = p1Temp in 32..500,
                currentTempF = if (p1Temp in 32..500) p1Temp else 0,
                isTargetReached = currentState.probe1.targetTempF > 0 && p1Temp >= currentState.probe1.targetTempF
            ),
            probe2 = currentState.probe2.copy(
                isConnected = p2Temp in 32..500,
                currentTempF = if (p2Temp in 32..500) p2Temp else 0,
                isTargetReached = currentState.probe2.targetTempF > 0 && p2Temp >= currentState.probe2.targetTempF
            ),
            probe3 = currentState.probe3.copy(
                isConnected = p3Temp in 32..500,
                currentTempF = if (p3Temp in 32..500) p3Temp else 0,
                isTargetReached = currentState.probe3.targetTempF > 0 && p3Temp >= currentState.probe3.targetTempF
            ),
            probe4 = currentState.probe4.copy(
                isConnected = p4Temp in 32..500,
                currentTempF = if (p4Temp in 32..500) p4Temp else 0,
                isTargetReached = currentState.probe4.targetTempF > 0 && p4Temp >= currentState.probe4.targetTempF
            ),
            hopper = PelletHopperState(
                levelPercentage = pelletPct,
                isLowPelletAlert = isLowPellet
            ),
            isAugerActive = isAugerActive,
            isFanActive = isFanActive,
            isIgniterActive = isIgniterActive
        )
    }

    /**
     * Encodes a command packet to set the target grill temperature.
     */
    fun buildSetTemperaturePacket(targetTempF: Int): ByteArray {
        val payload = byteArrayOf(
            HEADER,
            0x04, // Length
            CMD_SET_TARGET_TEMP,
            ((targetTempF shr 8) and 0xFF).toByte(),
            (targetTempF and 0xFF).toByte()
        )
        return appendChecksum(payload)
    }

    /**
     * Encodes a command packet to set smoke level (1 to 10).
     */
    fun buildSetSmokeLevelPacket(level: Int): ByteArray {
        val payload = byteArrayOf(
            HEADER,
            0x03,
            CMD_SET_SMOKE_LEVEL,
            level.coerceIn(1, 10).toByte()
        )
        return appendChecksum(payload)
    }

    /**
     * Encodes a command packet to power ON / start ignition sequence.
     */
    fun buildStartupPacket(): ByteArray {
        val payload = byteArrayOf(
            HEADER,
            0x02,
            CMD_POWER_STARTUP
        )
        return appendChecksum(payload)
    }

    /**
     * Encodes a command packet to initiate shutdown cycle.
     */
    fun buildShutdownPacket(): ByteArray {
        val payload = byteArrayOf(
            HEADER,
            0x02,
            CMD_POWER_SHUTDOWN
        )
        return appendChecksum(payload)
    }

    /**
     * Encodes Wi-Fi credentials for grill provisioning.
     */
    fun buildWifiProvisionPacket(ssid: String, pass: String): ByteArray {
        val ssidBytes = ssid.toByteArray(Charsets.UTF_8)
        val passBytes = pass.toByteArray(Charsets.UTF_8)
        val len = 2 + ssidBytes.size + 1 + passBytes.size

        val out = mutableListOf<Byte>()
        out.add(HEADER)
        out.add(len.toByte())
        out.add(CMD_WIFI_PROVISION)
        out.add(ssidBytes.size.toByte())
        ssidBytes.forEach { out.add(it) }
        out.add(passBytes.size.toByte())
        passBytes.forEach { out.add(it) }

        return appendChecksum(out.toByteArray())
    }

    /**
     * Encodes a mock status packet for unit testing and simulations.
     */
    fun encodeMockStatusPacket(state: SmokerState): ByteArray {
        val modeByte = when (state.powerMode) {
            SmokerPowerMode.OFF -> 0
            SmokerPowerMode.STARTUP -> 1
            SmokerPowerMode.RUNNING -> 2
            SmokerPowerMode.SHUTDOWN -> 3
            SmokerPowerMode.ERROR -> 4
        }.toByte()

        var flags = 0
        if (state.isAugerActive) flags = flags or 0x01
        if (state.isFanActive) flags = flags or 0x02
        if (state.isIgniterActive) flags = flags or 0x04

        val payload = byteArrayOf(
            HEADER,
            18.toByte(), // Length
            modeByte,
            ((state.currentChamberTempF shr 8) and 0xFF).toByte(),
            (state.currentChamberTempF and 0xFF).toByte(),
            ((state.targetChamberTempF shr 8) and 0xFF).toByte(),
            (state.targetChamberTempF and 0xFF).toByte(),
            state.smokeLevel.toByte(),
            ((state.probe1.currentTempF shr 8) and 0xFF).toByte(),
            (state.probe1.currentTempF and 0xFF).toByte(),
            ((state.probe2.currentTempF shr 8) and 0xFF).toByte(),
            (state.probe2.currentTempF and 0xFF).toByte(),
            ((state.probe3.currentTempF shr 8) and 0xFF).toByte(),
            (state.probe3.currentTempF and 0xFF).toByte(),
            ((state.probe4.currentTempF shr 8) and 0xFF).toByte(),
            (state.probe4.currentTempF and 0xFF).toByte(),
            state.hopper.levelPercentage.toByte(),
            flags.toByte()
        )
        return appendChecksum(payload)
    }

    private fun appendChecksum(bytes: ByteArray): ByteArray {
        var sum = 0
        for (b in bytes) {
            sum += (b.toInt() and 0xFF)
        }
        val checksum = (sum and 0xFF).toByte()
        return bytes + checksum
    }
}
