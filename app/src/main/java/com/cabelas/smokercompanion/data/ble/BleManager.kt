package com.cabelas.smokercompanion.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.util.Log
import com.cabelas.smokercompanion.data.model.ConnectionState
import com.cabelas.smokercompanion.data.model.SmokerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

@SuppressLint("MissingPermission")
class BleManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    private val tag = "BleManager"

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private val scanner: BluetoothLeScanner? get() = bluetoothAdapter?.bluetoothLeScanner

    private var activeGatt: BluetoothGatt? = null
    private var readNotifyCharacteristic: BluetoothGattCharacteristic? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = _discoveredDevices.asStateFlow()

    private val _smokerState = MutableStateFlow(SmokerState())
    val smokerState: StateFlow<SmokerState> = _smokerState.asStateFlow()

    private var isScanning = false
    private var scanTimeoutJob: Job? = null

    // Serial GATT Queue to prevent simultaneous GATT calls
    private val gattQueue = Channel<suspend () -> Unit>(Channel.UNLIMITED)

    init {
        scope.launch {
            for (action in gattQueue) {
                try {
                    action.invoke()
                } catch (e: Exception) {
                    Log.e(tag, "Error processing GATT queue action", e)
                }
            }
        }
    }

    val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    fun startScan(timeoutMs: Long = 15000) {
        if (isScanning || scanner == null || !isBluetoothEnabled) return
        _discoveredDevices.value = emptyList()
        _connectionState.value = ConnectionState.Scanning()
        isScanning = true

        val filters = listOf<ScanFilter>(
            // We can match by names or service UUIDs
            ScanFilter.Builder().build()
        )
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            scanner?.startScan(filters, settings, scanCallback)
            scanTimeoutJob?.cancel()
            scanTimeoutJob = scope.launch {
                delay(timeoutMs)
                stopScan()
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to start BLE scan", e)
            _connectionState.value = ConnectionState.Error("Scan failed: ${e.localizedMessage}")
            isScanning = false
        }
    }

    fun stopScan() {
        if (!isScanning) return
        try {
            scanner?.stopScan(scanCallback)
        } catch (e: Exception) {
            Log.e(tag, "Error stopping scan", e)
        } finally {
            isScanning = false
            scanTimeoutJob?.cancel()
            if (_connectionState.value is ConnectionState.Scanning) {
                _connectionState.value = ConnectionState.Disconnected
            }
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { device ->
                val name = device.name
                val address = device.address

                // Filter for Cabela's / Camp Chef devices or devices with matching names
                val isRelevant = name != null && SmokerGattUuid.MATCHING_DEVICE_PREFIXES.any { prefix ->
                    name.contains(prefix, ignoreCase = true)
                } || (name != null && name.isNotBlank())

                if (isRelevant) {
                    val currentList = _discoveredDevices.value
                    if (currentList.none { it.address == address }) {
                        val updated = (currentList + device).sortedWith(
                            compareByDescending<BluetoothDevice> { it.name?.startsWith("Cabelas", ignoreCase = true) == true }
                                .thenByDescending { it.name?.contains("Cabela", ignoreCase = true) == true }
                                .thenBy { it.name ?: "" }
                        )
                        _discoveredDevices.value = updated
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(tag, "BLE Scan failed with code: $errorCode")
            _connectionState.value = ConnectionState.Error("BLE scan failed (code $errorCode)")
            isScanning = false
        }
    }

    fun connect(device: BluetoothDevice) {
        stopScan()
        _connectionState.value = ConnectionState.Connecting(
            deviceName = device.name ?: "Cabela's Smoker",
            address = device.address
        )

        disconnect()

        activeGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            device.connectGatt(context, false, gattCallback)
        }
    }

    fun disconnect() {
        try {
            activeGatt?.disconnect()
            activeGatt?.close()
        } catch (e: Exception) {
            Log.e(tag, "Error closing GATT", e)
        } finally {
            activeGatt = null
            readNotifyCharacteristic = null
            writeCharacteristic = null
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val device = gatt.device
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    _connectionState.value = ConnectionState.Authenticating(device.name ?: "Smoker")
                    scope.launch {
                        delay(600) // Stabilize BLE connection before discovery
                        gatt.discoverServices()
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    disconnect()
                }
            } else {
                Log.e(tag, "GATT connection failed with status $status")
                disconnect()
                _connectionState.value = ConnectionState.Error("Connection error (Status: $status)")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                findAndConfigureCharacteristics(gatt)
                _connectionState.value = ConnectionState.Connected(
                    deviceName = gatt.device.name ?: "Cabela's Pro Series 36\"",
                    address = gatt.device.address
                )
            } else {
                Log.e(tag, "Service discovery failed with status $status")
                _connectionState.value = ConnectionState.Error("Failed to discover smoker services")
            }
        }

        // Modern API 33+ non-deprecated callback for Characteristic Read
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                handleIncomingBytes(value)
            }
        }

        // Backwards compatible callback for API < 33
        @Deprecated("Deprecated in Java")
        @Suppress("DEPRECATION")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && status == BluetoothGatt.GATT_SUCCESS) {
                characteristic.value?.let { handleIncomingBytes(it) }
            }
        }

        // Modern API 33+ non-deprecated callback for Characteristic Changed (Notification)
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            handleIncomingBytes(value)
        }

        // Backwards compatible callback for API < 33
        @Deprecated("Deprecated in Java")
        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                characteristic.value?.let { handleIncomingBytes(it) }
            }
        }
    }

    private fun findAndConfigureCharacteristics(gatt: BluetoothGatt) {
        for (service in gatt.services) {
            val charRead = service.getCharacteristic(SmokerGattUuid.CHAR_READ_NOTIFY)
                ?: service.getCharacteristic(SmokerGattUuid.CHAR_NUS_TX)
            val charWrite = service.getCharacteristic(SmokerGattUuid.CHAR_WRITE_COMMAND)
                ?: service.getCharacteristic(SmokerGattUuid.CHAR_NUS_RX)

            if (charRead != null) {
                readNotifyCharacteristic = charRead
                enableNotifications(gatt, charRead)
            }
            if (charWrite != null) {
                writeCharacteristic = charWrite
            }
        }
    }

    private fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        gattQueue.trySend {
            gatt.setCharacteristicNotification(characteristic, true)
            val descriptor = characteristic.getDescriptor(SmokerGattUuid.CLIENT_CHARACTERISTIC_CONFIG)
            if (descriptor != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                } else {
                    @Suppress("DEPRECATION")
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    @Suppress("DEPRECATION")
                    gatt.writeDescriptor(descriptor)
                }
            }
        }
    }

    private fun handleIncomingBytes(bytes: ByteArray) {
        val updated = SmokerPacketCodec.decodeStatusPacket(bytes, _smokerState.value)
        if (updated != null) {
            _smokerState.value = updated
        }
    }

    fun sendCommand(payload: ByteArray) {
        val gatt = activeGatt ?: return
        val char = writeCharacteristic ?: return

        gattQueue.trySend {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeCharacteristic(
                    char,
                    payload,
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                )
            } else {
                @Suppress("DEPRECATION")
                char.value = payload
                @Suppress("DEPRECATION")
                char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                @Suppress("DEPRECATION")
                gatt.writeCharacteristic(char)
            }
        }
    }

    fun setTargetTemperature(tempF: Int) {
        val packet = SmokerPacketCodec.buildSetTemperaturePacket(tempF)
        sendCommand(packet)
        _smokerState.value = _smokerState.value.copy(targetChamberTempF = tempF)
    }

    fun setSmokeLevel(level: Int) {
        val packet = SmokerPacketCodec.buildSetSmokeLevelPacket(level)
        sendCommand(packet)
        _smokerState.value = _smokerState.value.copy(smokeLevel = level)
    }

    fun powerStartup() {
        val packet = SmokerPacketCodec.buildStartupPacket()
        sendCommand(packet)
    }

    fun powerShutdown() {
        val packet = SmokerPacketCodec.buildShutdownPacket()
        sendCommand(packet)
    }

    fun provisionWifi(ssid: String, pass: String) {
        val packet = SmokerPacketCodec.buildWifiProvisionPacket(ssid, pass)
        sendCommand(packet)
    }
}
