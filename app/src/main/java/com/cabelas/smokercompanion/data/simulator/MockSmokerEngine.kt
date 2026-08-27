package com.cabelas.smokercompanion.data.simulator

import com.cabelas.smokercompanion.data.model.MeatProbe
import com.cabelas.smokercompanion.data.model.PelletHopperState
import com.cabelas.smokercompanion.data.model.SmokerPowerMode
import com.cabelas.smokercompanion.data.model.SmokerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Realistic thermal and physical simulation engine for Cabela's Pro Series Pellet Grill.
 * Simulates ignition cycle, PID temperature ramping, meat internal thermal conduction,
 * pellet consumption, and target alarms.
 */
class MockSmokerEngine(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val _state = MutableStateFlow(
        SmokerState(
            powerMode = SmokerPowerMode.RUNNING,
            currentChamberTempF = 224,
            targetChamberTempF = 225,
            smokeLevel = 7,
            probe1 = MeatProbe(
                id = 1,
                name = "Brisket Flat",
                isConnected = true,
                currentTempF = 162,
                targetTempF = 203,
                isAlarmEnabled = true,
                meatType = "Beef Brisket"
            ),
            probe2 = MeatProbe(
                id = 2,
                name = "Pork Shoulder",
                isConnected = true,
                currentTempF = 145,
                targetTempF = 203,
                isAlarmEnabled = true,
                meatType = "Pork Butt"
            ),
            probe3 = MeatProbe(
                id = 3,
                name = "Probe 3",
                isConnected = false
            ),
            probe4 = MeatProbe(
                id = 4,
                name = "Probe 4",
                isConnected = false
            ),
            hopper = PelletHopperState(
                levelPercentage = 78,
                isLowPelletAlert = false,
                woodFlavor = "Apple & Hickory Blend"
            ),
            isAugerActive = true,
            isFanActive = true,
            isIgniterActive = false,
            cookStartTimeMs = System.currentTimeMillis() - (3 * 3600 * 1000 + 42 * 60 * 1000)
        )
    )
    val state: StateFlow<SmokerState> = _state.asStateFlow()

    private var simulationJob: Job? = null

    init {
        startSimulationLoop()
    }

    fun startSimulationLoop() {
        simulationJob?.cancel()
        simulationJob = scope.launch {
            while (isActive) {
                delay(1500)
                tickPhysics()
            }
        }
    }

    fun stopSimulation() {
        simulationJob?.cancel()
    }

    private fun tickPhysics() {
        val current = _state.value
        if (current.powerMode == SmokerPowerMode.OFF) return

        // 1. Chamber Temp PID Ramping
        val target = current.targetChamberTempF
        var chamber = current.currentChamberTempF
        val diff = target - chamber

        val step = when {
            diff > 20 -> 4
            diff > 5 -> 2
            diff < -20 -> -4
            diff < -5 -> -2
            else -> Random.nextInt(-1, 2)
        }
        chamber = (chamber + step).coerceIn(70, 500)

        // 2. Meat Probe Thermal Conduction
        fun updateProbe(probe: MeatProbe): MeatProbe {
            if (!probe.isConnected) return probe
            val probeDiff = target - probe.currentTempF
            val probeStep = if (probeDiff > 0 && Random.nextFloat() > 0.6f) 1 else 0
            val newTemp = (probe.currentTempF + probeStep).coerceAtMost(target)
            val reached = probe.targetTempF > 0 && newTemp >= probe.targetTempF
            return probe.copy(
                currentTempF = newTemp,
                isTargetReached = reached
            )
        }

        // 3. Pellet Consumption
        val newPelletPct = if (Random.nextFloat() > 0.95f) {
            (current.hopper.levelPercentage - 1).coerceAtLeast(0)
        } else current.hopper.levelPercentage

        _state.value = current.copy(
            currentChamberTempF = chamber,
            probe1 = updateProbe(current.probe1),
            probe2 = updateProbe(current.probe2),
            probe3 = updateProbe(current.probe3),
            probe4 = updateProbe(current.probe4),
            hopper = current.hopper.copy(
                levelPercentage = newPelletPct,
                isLowPelletAlert = newPelletPct <= 15
            ),
            isAugerActive = diff > 0,
            isFanActive = current.powerMode == SmokerPowerMode.RUNNING
        )
    }

    fun setTargetTemperature(tempF: Int) {
        _state.value = _state.value.copy(targetChamberTempF = tempF.coerceIn(160, 500))
    }

    fun setSmokeLevel(level: Int) {
        _state.value = _state.value.copy(smokeLevel = level.coerceIn(1, 10))
    }

    fun setProbeTarget(probeId: Int, targetF: Int) {
        val current = _state.value
        _state.value = when (probeId) {
            1 -> current.copy(probe1 = current.probe1.copy(targetTempF = targetF, isAlarmEnabled = true))
            2 -> current.copy(probe2 = current.probe2.copy(targetTempF = targetF, isAlarmEnabled = true))
            3 -> current.copy(probe3 = current.probe3.copy(targetTempF = targetF, isAlarmEnabled = true))
            4 -> current.copy(probe4 = current.probe4.copy(targetTempF = targetF, isAlarmEnabled = true))
            else -> current
        }
    }

    fun toggleProbeConnection(probeId: Int) {
        val current = _state.value
        _state.value = when (probeId) {
            1 -> current.copy(probe1 = current.probe1.copy(isConnected = !current.probe1.isConnected))
            2 -> current.copy(probe2 = current.probe2.copy(isConnected = !current.probe2.isConnected))
            3 -> current.copy(probe3 = current.probe3.copy(isConnected = !current.probe3.isConnected, currentTempF = 65, targetTempF = 195))
            4 -> current.copy(probe4 = current.probe4.copy(isConnected = !current.probe4.isConnected, currentTempF = 65, targetTempF = 165))
            else -> current
        }
    }

    fun setPowerMode(mode: SmokerPowerMode) {
        _state.value = _state.value.copy(powerMode = mode)
    }
}
