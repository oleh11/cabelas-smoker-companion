package com.cabelas.smokercompanion.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cabelas.smokercompanion.data.model.SmokerState
import com.cabelas.smokercompanion.data.model.TemperatureUnit
import com.cabelas.smokercompanion.data.repository.SmokerRepository
import com.cabelas.smokercompanion.service.CookMonitoringService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SmokerRepository,
    private val context: Context
) : ViewModel() {

    val smokerState: StateFlow<SmokerState> = repository.smokerState
    val isSimulatorMode: StateFlow<Boolean> = repository.isSimulatorMode

    private val _isBackgroundServiceActive = MutableStateFlow(true)
    val isBackgroundServiceActive: StateFlow<Boolean> = _isBackgroundServiceActive.asStateFlow()

    private val _isSoundAlarmEnabled = MutableStateFlow(true)
    val isSoundAlarmEnabled: StateFlow<Boolean> = _isSoundAlarmEnabled.asStateFlow()

    fun setTemperatureUnit(unit: TemperatureUnit) {
        viewModelScope.launch {
            repository.setTemperatureUnit(unit)
        }
    }

    fun setSimulatorMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSimulatorMode(enabled)
        }
    }

    fun toggleBackgroundService(enabled: Boolean) {
        _isBackgroundServiceActive.value = enabled
        if (enabled) {
            CookMonitoringService.startService(context)
        } else {
            CookMonitoringService.stopService(context)
        }
    }

    fun toggleSoundAlarm(enabled: Boolean) {
        _isSoundAlarmEnabled.value = enabled
    }

    class Factory(
        private val repository: SmokerRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repository, context) as T
        }
    }
}
