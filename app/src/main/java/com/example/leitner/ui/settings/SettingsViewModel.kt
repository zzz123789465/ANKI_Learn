package com.example.leitner.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leitner.data.settings.StudySettingsRepositoryImpl
import com.example.leitner.domain.settings.StudySettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val dailyLimitInput: String = StudySettingsRepositoryImpl.DEFAULT_DAILY_LIMIT.toString(),
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repository: StudySettingsRepository) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init { viewModelScope.launch { _state.value = _state.value.copy(dailyLimitInput = repository.getDailyLimit().toString()) } }

    fun updateDailyLimit(value: String) {
        if (value.all(Char::isDigit) && value.length <= 3) _state.value = _state.value.copy(dailyLimitInput = value, saved = false, error = null)
    }

    fun save() = viewModelScope.launch {
        val value = _state.value.dailyLimitInput.toIntOrNull()
        if (value == null || value !in StudySettingsRepositoryImpl.MIN_DAILY_LIMIT..StudySettingsRepositoryImpl.MAX_DAILY_LIMIT) {
            _state.value = _state.value.copy(error = "請輸入 ${StudySettingsRepositoryImpl.MIN_DAILY_LIMIT}～${StudySettingsRepositoryImpl.MAX_DAILY_LIMIT} 張")
        } else {
            repository.setDailyLimit(value)
            _state.value = _state.value.copy(saved = true, error = null)
        }
    }
}
