package com.cabelas.smokercompanion.data.repository

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.cabelas.smokercompanion.data.ble.BleManager
import com.cabelas.smokercompanion.data.model.ConnectionState
import com.cabelas.smokercompanion.data.model.CookDataPoint
import com.cabelas.smokercompanion.data.model.SmokerPowerMode
import com.cabelas.smokercompanion.data.model.SmokerState
import com.cabelas.smokercompanion.data.model.TemperatureUnit
import com.cabelas.smokercompanion.data.simulator.MockSmokerEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SmokerRepositoryImpl(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) : SmokerRepository {

    private val bleManager = BleManager(context, scope)
    private val simulatorEngine = MockSmokerEngine(scope)

    private val _isSimulatorMode = MutableStateFlow(true) // Defaults to simulator for instant testing
    override val isSimulatorMode: StateFlow<Boolean> = _isSimulatorMode.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.SimulatorActive())
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _smokerState = MutableStateFlow(SmokerState())
    override val smokerState: StateFlow<SmokerState> = _smokerState.asStateFlow()

    override val discoveredDevices: StateFlow<List<BluetoothDevice>> = bleManager.discoveredDevices

    private val _cookHistory = MutableStateFlow<List<CookDataPoint>>(emptyList())
    override val cookHistory: StateFlow<List<CookDataPoint>> = _cookHistory.asStateFlow()

    init {
        // Collect BLE state
        scope.launch {
            bleManager.smokerState.collect { bleState ->
                if (!_isSimulatorMode.value) {
                    _smokerState.value = bleState
                }
            }
        }
        scope.launch {
            bleManager.connectionState.collect { connState ->
                if (!_isSimulatorMode.value) {
                    _connectionState.value = connState
                }
            }
        }

        // Collect Simulator state
        scope.launch {
            simulatorEngine.state.collect { simState ->
                if (_isSimulatorMode.value) {
                    _smokerState.value = simState
                }
            }
        }

        // Periodic temperature history recording
        scope.launch {
            while (isActive) {
                delay(5000)
                val current = _smokerState.value
                val dataPoint = CookDataPoint(
                    timestampMs = System.currentTimeMillis(),
                    grillTempF = current.currentChamberTempF,
                    probe1TempF = if (current.probe1.isConnected) current.probe1.currentTempF else 0,
                    probe2TempF = if (current.probe2.isConnected) current.probe2.currentTempF else 0,
                    probe3TempF = if (current.probe3.isConnected) current.probe3.currentTempF else 0,
                    probe4TempF = if (current.probe4.isConnected) current.probe4.currentTempF else 0
                )
                val currentList = _cookHistory.value
                // Keep last 100 points
                _cookHistory.value = (currentList + dataPoint).takeLast(100)
            }
        }
    }

    override fun startScan() {
        if (!_isSimulatorMode.value) {
            bleManager.startScan()
        }
    }

    override fun stopScan() {
        bleManager.stopScan()
    }

    override fun connectBle(device: BluetoothDevice) {
        _isSimulatorMode.value = false
        bleManager.connect(device)
    }

    override fun disconnect() {
        if (_isSimulatorMode.value) {
            _connectionState.value = ConnectionState.Disconnected
        } else {
            bleManager.disconnect()
        }
    }

    override fun setSimulatorMode(enabled: Boolean) {
        _isSimulatorMode.value = enabled
        if (enabled) {
            bleManager.disconnect()
            _connectionState.value = ConnectionState.SimulatorActive()
            _smokerState.value = simulatorEngine.state.value
            simulatorEngine.startSimulationLoop()
        } else {
            simulatorEngine.stopSimulation()
            _connectionState.value = ConnectionState.Disconnected
            _smokerState.value = SmokerState()
        }
    }

    override fun setTargetTemperature(tempF: Int) {
        if (_isSimulatorMode.value) {
            simulatorEngine.setTargetTemperature(tempF)
        } else {
            bleManager.setTargetTemperature(tempF)
        }
    }

    override fun setSmokeLevel(level: Int) {
        if (_isSimulatorMode.value) {
            simulatorEngine.setSmokeLevel(level)
        } else {
            bleManager.setSmokeLevel(level)
        }
    }

    override fun setProbeTarget(probeId: Int, targetF: Int) {
        if (_isSimulatorMode.value) {
            simulatorEngine.setProbeTarget(probeId, targetF)
        } else {
            val current = _smokerState.value
            _smokerState.value = when (probeId) {
                1 -> current.copy(probe1 = current.probe1.copy(targetTempF = targetF, isAlarmEnabled = true))
                2 -> current.copy(probe2 = current.probe2.copy(targetTempF = targetF, isAlarmEnabled = true))
                3 -> current.copy(probe3 = current.probe3.copy(targetTempF = targetF, isAlarmEnabled = true))
                4 -> current.copy(probe4 = current.probe4.copy(targetTempF = targetF, isAlarmEnabled = true))
                else -> current
            }
        }
    }

    override fun setPowerMode(mode: SmokerPowerMode) {
        if (_isSimulatorMode.value) {
            simulatorEngine.setPowerMode(mode)
        } else {
            when (mode) {
                SmokerPowerMode.RUNNING, SmokerPowerMode.STARTUP -> bleManager.powerStartup()
                SmokerPowerMode.SHUTDOWN, SmokerPowerMode.OFF -> bleManager.powerShutdown()
                else -> Unit
            }
        }
    }

    override fun setTemperatureUnit(unit: TemperatureUnit) {
        _smokerState.value = _smokerState.value.copy(temperatureUnit = unit)
    }

    override fun provisionWifi(ssid: String, pass: String) {
        if (!_isSimulatorMode.value) {
            bleManager.provisionWifi(ssid, pass)
        } else {
            _smokerState.value = _smokerState.value.copy(
                wifiConnected = true,
                wifiSsid = ssid
            )
        }
    }

    override fun resetCookSession() {
        _cookHistory.value = emptyList()
        _smokerState.value = _smokerState.value.copy(
            cookStartTimeMs = System.currentTimeMillis()
        )
    }
}
