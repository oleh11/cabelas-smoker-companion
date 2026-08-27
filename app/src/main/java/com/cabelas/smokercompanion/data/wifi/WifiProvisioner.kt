package com.cabelas.smokercompanion.data.wifi

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WifiScanItem(
    val ssid: String,
    val signalLevel: Int,
    val is24Ghz: Boolean
)

class WifiProvisioner(private val context: Context) {
    private val connectivityManager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private val _availableNetworks = MutableStateFlow<List<WifiScanItem>>(
        listOf(
            WifiScanItem("Home_BBQ_2.4G", -55, true),
            WifiScanItem("Backyard_Extender", -62, true),
            WifiScanItem("Guest_Network", -75, true)
        )
    )
    val availableNetworks: StateFlow<List<WifiScanItem>> = _availableNetworks.asStateFlow()

    fun getConnectedSsid(): String? {
        val wifiInfo: WifiInfo? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val activeNetwork = connectivityManager?.activeNetwork
            val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
            capabilities?.transportInfo as? WifiInfo
        } else {
            @Suppress("DEPRECATION")
            wifiManager?.connectionInfo
        }

        val raw = wifiInfo?.ssid ?: return null
        return raw.removePrefix("\"").removeSuffix("\"").takeIf { it.isNotBlank() && it != "<unknown ssid>" }
    }
}
