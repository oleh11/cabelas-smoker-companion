package com.cabelas.smokercompanion.ui.screens.cook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cabelas.smokercompanion.data.model.CookPreset
import com.cabelas.smokercompanion.data.model.TemperatureUnit
import com.cabelas.smokercompanion.data.repository.SmokerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CookPlannerViewModel(
    private val repository: SmokerRepository
) : ViewModel() {

    private val _presets = MutableStateFlow(CookPreset.DEFAULT_PRESETS)
    val presets: StateFlow<List<CookPreset>> = _presets.asStateFlow()

    private val _selectedPreset = MutableStateFlow<CookPreset?>(CookPreset.DEFAULT_PRESETS.first())
    val selectedPreset: StateFlow<CookPreset?> = _selectedPreset.asStateFlow()

    val temperatureUnit: StateFlow<TemperatureUnit> = MutableStateFlow(TemperatureUnit.FAHRENHEIT)

    fun selectPreset(preset: CookPreset) {
        _selectedPreset.value = preset
    }

    fun applyPresetToSmoker(preset: CookPreset, probeId: Int = 1) {
        viewModelScope.launch {
            repository.setTargetTemperature(preset.recommendedGrillTempF)
            repository.setSmokeLevel(preset.recommendedSmokeLevel)
            repository.setProbeTarget(probeId, preset.targetInternalTempF)
            repository.resetCookSession()
        }
    }

    class Factory(private val repository: SmokerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CookPlannerViewModel(repository) as T
        }
    }
}
