package com.cabelas.smokercompanion.data.repository

import android.bluetooth.BluetoothDevice
import com.cabelas.smokercompanion.data.model.ConnectionState
import com.cabelas.smokercompanion.data.model.CookDataPoint
import com.cabelas.smokercompanion.data.model.MeatProbe
import com.cabelas.smokercompanion.data.model.SmokerPowerMode
import com.cabelas.smokercompanion.data.model.SmokerState
import com.cabelas.smokercompanion.data.model.TemperatureUnit
import kotlinx.coroutines.flow.StateFlow

interface SmokerRepository {
    val connectionState: StateFlow<ConnectionState>
    val smokerState: StateFlow<SmokerState>
    val discoveredDevices: StateFlow<List<BluetoothDevice>>
    val cookHistory: StateFlow<List<CookDataPoint>>
    val isSimulatorMode: StateFlow<Boolean>

    fun startScan()
    fun stopScan()
    fun connectBle(device: BluetoothDevice)
    fun disconnect()
    fun setSimulatorMode(enabled: Boolean)

    fun setTargetTemperature(tempF: Int)
    fun setSmokeLevel(level: Int)
    fun setProbeTarget(probeId: Int, targetF: Int)
    fun setPowerMode(mode: SmokerPowerMode)
    fun setTemperatureUnit(unit: TemperatureUnit)
    fun provisionWifi(ssid: String, pass: String)
    fun resetCookSession()
}
