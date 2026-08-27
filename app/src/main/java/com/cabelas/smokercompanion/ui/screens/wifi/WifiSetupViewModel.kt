package com.cabelas.smokercompanion.ui.screens.wifi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cabelas.smokercompanion.data.model.SmokerState
import com.cabelas.smokercompanion.data.repository.SmokerRepository
import com.cabelas.smokercompanion.data.wifi.WifiProvisioner
import com.cabelas.smokercompanion.data.wifi.WifiScanItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WifiSetupViewModel(
    private val repository: SmokerRepository,
    private val provisioner: WifiProvisioner
) : ViewModel() {

    val smokerState: StateFlow<SmokerState> = repository.smokerState
    val availableNetworks: StateFlow<List<WifiScanItem>> = provisioner.availableNetworks

    private val _selectedSsid = MutableStateFlow(provisioner.getConnectedSsid() ?: "Home_BBQ_2.4G")
    val selectedSsid: StateFlow<String> = _selectedSsid.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isProvisioning = MutableStateFlow(false)
    val isProvisioning: StateFlow<Boolean> = _isProvisioning.asStateFlow()

    private val _provisionSuccess = MutableStateFlow(false)
    val provisionSuccess: StateFlow<Boolean> = _provisionSuccess.asStateFlow()

    fun selectSsid(ssid: String) {
        _selectedSsid.value = ssid
    }

    fun setPassword(pass: String) {
        _password.value = pass
    }

    fun submitWifiCredentials() {
        if (_selectedSsid.value.isBlank()) return
        viewModelScope.launch {
            _isProvisioning.value = true
            repository.provisionWifi(_selectedSsid.value, _password.value)
            kotlinx.coroutines.delay(1200)
            _isProvisioning.value = false
            _provisionSuccess.value = true
        }
    }

    class Factory(
        private val repository: SmokerRepository,
        private val provisioner: WifiProvisioner
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WifiSetupViewModel(repository, provisioner) as T
        }
    }
}
