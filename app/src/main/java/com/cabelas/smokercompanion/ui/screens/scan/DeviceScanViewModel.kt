package com.cabelas.smokercompanion.ui.screens.scan

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cabelas.smokercompanion.data.model.ConnectionState
import com.cabelas.smokercompanion.data.repository.SmokerRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DeviceScanViewModel(
    private val repository: SmokerRepository
) : ViewModel() {

    val connectionState: StateFlow<ConnectionState> = repository.connectionState
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = repository.discoveredDevices
    val isSimulatorMode: StateFlow<Boolean> = repository.isSimulatorMode

    fun startScan() {
        viewModelScope.launch {
            repository.startScan()
        }
    }

    fun stopScan() {
        viewModelScope.launch {
            repository.stopScan()
        }
    }

    fun connectDevice(device: BluetoothDevice) {
        viewModelScope.launch {
            repository.connectBle(device)
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            repository.disconnect()
        }
    }

    fun setSimulatorMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSimulatorMode(enabled)
        }
    }

    class Factory(private val repository: SmokerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DeviceScanViewModel(repository) as T
        }
    }
}
