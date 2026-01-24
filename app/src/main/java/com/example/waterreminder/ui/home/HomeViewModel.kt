package com.example.waterreminder.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.waterreminder.domain.model.GoalConfig
import com.example.waterreminder.domain.model.ReminderConfig
import com.example.waterreminder.domain.model.WaterIntake
import com.example.waterreminder.domain.usecase.LogWaterUseCase
import com.example.waterreminder.domain.usecase.ObserveGoalConfigUseCase
import com.example.waterreminder.domain.usecase.ObserveReminderConfigUseCase
import com.example.waterreminder.domain.usecase.ObserveTodayIntakeUseCase
import com.example.waterreminder.domain.usecase.ObserveHistoryUseCase
import com.example.waterreminder.util.DateUtils
import com.example.waterreminder.worker.SyncScheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

data class HomeUiState(
    val todayIntakeMl: Int = 0,
    val dailyGoalMl: Int = 2000,
    val cupSizeMl: Int = 250,
    val progress: Float = 0f,
    val nextReminder: String = "--",
    val streakDays: Int = 0
)

class HomeViewModel(
    private val appContext: Context,
    private val logWaterUseCase: LogWaterUseCase,
    private val observeTodayIntakeUseCase: ObserveTodayIntakeUseCase,
    private val observeGoalConfigUseCase: ObserveGoalConfigUseCase,
    private val observeReminderConfigUseCase: ObserveReminderConfigUseCase,
    private val observeHistoryUseCase: ObserveHistoryUseCase
) : ViewModel() {

    private val ticker = flow {
        while (true) {
            emit(LocalDateTime.now())
            delay(60_000)
        }
    }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(
                observeTodayIntakeUseCase(),
                observeGoalConfigUseCase(),
                observeReminderConfigUseCase(),
                observeHistoryUseCase(
                    DateUtils.startOfDayMillis(LocalDate.now().minusDays(30)),
                    DateUtils.endOfDayMillis()
                ),
                ticker
            ) { todayTotal, goal, reminder, history, now ->
                val progress = if (goal.dailyGoalMl > 0) {
                    todayTotal.toFloat() / goal.dailyGoalMl
                } else 0f
                val streak = calculateStreak(history, goal)
                val nextReminderText = formatNextReminder(now, reminder)
                HomeUiState(
                    todayIntakeMl = todayTotal,
                    dailyGoalMl = goal.dailyGoalMl,
                    cupSizeMl = goal.cupSizeMl,
                    progress = progress,
                    nextReminder = nextReminderText,
                    streakDays = streak
                )
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }

    fun logWater() {
        viewModelScope.launch {
            val cupSize = uiState.value.cupSizeMl
            logWaterUseCase(cupSize)
            SyncScheduler.enqueueNow(appContext)
        }
    }

    private fun calculateStreak(entries: List<WaterIntake>, goal: GoalConfig): Int {
        val totalsByDate = entries.groupBy { millisToDate(it.timestamp) }
            .mapValues { (_, list) -> list.sumOf { it.amountMl } }
        var streak = 0
        var date = LocalDate.now()
        while (true) {
            val total = totalsByDate[date] ?: 0
            if (total < goal.dailyGoalMl) break
            streak++
            date = date.minusDays(1)
        }
        return streak
    }

    private fun millisToDate(millis: Long): LocalDate {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    private fun formatNextReminder(now: LocalDateTime, config: ReminderConfig): String {
        if (!config.enabled) return "Reminders off"
        val next = DateUtils.nextReminderTime(now, config.startTime, config.endTime, config.intervalMinutes)
            ?: return "--"
        val duration = Duration.between(now, next).coerceAtLeast(Duration.ZERO)
        val hours = duration.toHours()
        val minutes = duration.minusHours(hours).toMinutes()
        return if (hours > 0) {
            "Next in ${hours}h ${minutes}m"
        } else {
            "Next in ${minutes}m"
        }
    }

    class Factory(
        private val context: Context,
        private val logWaterUseCase: LogWaterUseCase,
        private val observeTodayIntakeUseCase: ObserveTodayIntakeUseCase,
        private val observeGoalConfigUseCase: ObserveGoalConfigUseCase,
        private val observeReminderConfigUseCase: ObserveReminderConfigUseCase,
        private val observeHistoryUseCase: ObserveHistoryUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(
                    context.applicationContext,
                    logWaterUseCase,
                    observeTodayIntakeUseCase,
                    observeGoalConfigUseCase,
                    observeReminderConfigUseCase,
                    observeHistoryUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
