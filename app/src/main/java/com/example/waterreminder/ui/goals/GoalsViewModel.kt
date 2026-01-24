package com.example.waterreminder.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.waterreminder.data.datastore.PreferencesManager
import com.example.waterreminder.data.repository.GoalRepository
import com.example.waterreminder.domain.model.GoalConfig
import com.example.waterreminder.util.UnitSystem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GoalsUiState(
    val dailyGoalMl: Int = 2000,
    val cupSizeMl: Int = 250,
    val adaptive: Boolean = false,
    val weeklyTargetDays: Int = 7,
    val unitSystem: UnitSystem = UnitSystem.ML,
    val onboardingShown: Boolean = false
)

class GoalsViewModel(
    private val goalRepository: GoalRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState

    init {
        viewModelScope.launch {
            goalRepository.ensureDefault()
            combine(
                goalRepository.observeConfig(),
                preferencesManager.unitSystem,
                preferencesManager.weeklyTarget,
                preferencesManager.onboardingGoalsShown
            ) { config, unit, weekly, onboardingShown ->
                GoalsUiState(
                    dailyGoalMl = config.dailyGoalMl,
                    cupSizeMl = config.cupSizeMl,
                    adaptive = config.adaptive,
                    weeklyTargetDays = weekly,
                    unitSystem = unit,
                    onboardingShown = onboardingShown
                )
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }

    fun updateDailyGoal(valueMl: Int) {
        viewModelScope.launch {
            val config = currentConfig().copy(dailyGoalMl = valueMl)
            goalRepository.update(config)
        }
    }

    fun updateCupSize(valueMl: Int) {
        viewModelScope.launch {
            val config = currentConfig().copy(cupSizeMl = valueMl)
            goalRepository.update(config)
        }
    }

    fun updateAdaptive(enabled: Boolean) {
        viewModelScope.launch {
            val config = currentConfig().copy(adaptive = enabled)
            goalRepository.update(config)
        }
    }

    fun updateWeeklyTarget(days: Int) {
        viewModelScope.launch {
            preferencesManager.setWeeklyTarget(days)
        }
    }

    fun markOnboardingShown() {
        viewModelScope.launch { preferencesManager.setOnboardingGoalsShown() }
    }

    private suspend fun currentConfig(): GoalConfig = goalRepository.getConfig()

    class Factory(
        private val goalRepository: GoalRepository,
        private val preferencesManager: PreferencesManager
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GoalsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GoalsViewModel(goalRepository, preferencesManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
