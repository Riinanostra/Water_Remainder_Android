package com.example.waterreminder.ui.reminders

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.waterreminder.data.datastore.PreferencesManager
import com.example.waterreminder.data.repository.ReminderRepository
import com.example.waterreminder.domain.model.ReminderConfig
import com.example.waterreminder.worker.ReminderWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime

data class RemindersUiState(
    val enabled: Boolean = false,
    val intervalMinutes: Int = 60,
    val startTime: LocalTime = LocalTime.of(8, 0),
    val endTime: LocalTime = LocalTime.of(22, 0),
    val smartOffice: Boolean = true,
    val smartWorkout: Boolean = true,
    val smartTravel: Boolean = false,
    val onboardingShown: Boolean = false
)

class RemindersViewModel(
    private val context: Context,
    private val reminderRepository: ReminderRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RemindersUiState())
    val uiState: StateFlow<RemindersUiState> = _uiState

    init {
        viewModelScope.launch {
            reminderRepository.ensureDefault()
            combine(
                reminderRepository.observeConfig(),
                preferencesManager.smartOffice,
                preferencesManager.smartWorkout,
                preferencesManager.smartTravel,
                preferencesManager.onboardingRemindersShown
            ) { config, office, workout, travel, onboardingShown ->
                RemindersUiState(
                    enabled = config.enabled,
                    intervalMinutes = config.intervalMinutes,
                    startTime = config.startTime,
                    endTime = config.endTime,
                    smartOffice = office,
                    smartWorkout = workout,
                    smartTravel = travel,
                    onboardingShown = onboardingShown
                )
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }

    fun toggleEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val newConfig = currentConfig().copy(enabled = enabled)
            reminderRepository.update(newConfig)
            if (enabled) {
                ReminderWorker.schedule(context, newConfig.intervalMinutes)
            } else {
                ReminderWorker.cancel(context)
            }
        }
    }

    fun updateInterval(intervalMinutes: Int) {
        viewModelScope.launch {
            val newConfig = currentConfig().copy(intervalMinutes = intervalMinutes)
            reminderRepository.update(newConfig)
            if (newConfig.enabled) {
                ReminderWorker.schedule(context, intervalMinutes)
            }
        }
    }

    fun updateStartTime(time: LocalTime) {
        viewModelScope.launch {
            val newConfig = currentConfig().copy(startTime = time)
            reminderRepository.update(newConfig)
        }
    }

    fun updateEndTime(time: LocalTime) {
        viewModelScope.launch {
            val newConfig = currentConfig().copy(endTime = time)
            reminderRepository.update(newConfig)
        }
    }

    fun updateSmartOffice(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setSmartOffice(enabled)
        }
    }

    fun updateSmartWorkout(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setSmartWorkout(enabled)
        }
    }

    fun updateSmartTravel(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setSmartTravel(enabled)
        }
    }

    fun markOnboardingShown() {
        viewModelScope.launch { preferencesManager.setOnboardingRemindersShown() }
    }

    private suspend fun currentConfig(): ReminderConfig = reminderRepository.getConfig()

    class Factory(
        private val context: Context,
        private val reminderRepository: ReminderRepository,
        private val preferencesManager: PreferencesManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RemindersViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RemindersViewModel(context, reminderRepository, preferencesManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
