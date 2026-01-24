package com.example.waterreminder.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.waterreminder.data.datastore.PreferencesManager
import com.example.waterreminder.domain.model.WaterIntake
import com.example.waterreminder.domain.usecase.ObserveGoalConfigUseCase
import com.example.waterreminder.domain.usecase.ObserveHistoryUseCase
import com.example.waterreminder.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class ChartEntry(val date: LocalDate, val totalMl: Int)

data class HeatmapDay(val date: LocalDate, val intensity: Float)

data class HistoryUiState(
    val weekView: Boolean = true,
    val chartEntries: List<ChartEntry> = emptyList(),
    val streakSummary: String = "",
    val heatmapDays: List<HeatmapDay> = emptyList(),
    val exportStatus: String = ""
)

class HistoryViewModel(
    private val observeHistoryUseCase: ObserveHistoryUseCase,
    private val observeGoalConfigUseCase: ObserveGoalConfigUseCase,
    private val preferencesManager: PreferencesManager,
    private val syncRepository: com.example.waterreminder.data.repository.SyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState

    init {
        viewModelScope.launch {
            preferencesManager.weekView
                .combine(observeGoalConfigUseCase()) { weekView, goal -> weekView to goal }
                .flatMapLatest { (weekView, goal) ->
                    val days = if (weekView) 7 else 30
                    val start = DateUtils.startOfDayMillis(LocalDate.now().minusDays(days.toLong() - 1))
                    val end = DateUtils.endOfDayMillis()
                    observeHistoryUseCase(start, end).map { list ->
                        val chartEntries = buildChartEntries(list, days)
                        val streak = calculateStreak(list, goal.dailyGoalMl)
                        val heatmap = buildHeatmap(list, goal.dailyGoalMl)
                        HistoryUiState(
                            weekView = weekView,
                            chartEntries = chartEntries,
                            streakSummary = "Current streak: $streak days",
                            heatmapDays = heatmap
                        )
                    }
                }
                .collect { state -> _uiState.update { state } }
        }
    }

    fun toggleWeekView(weekView: Boolean) {
        viewModelScope.launch {
            preferencesManager.setWeekView(weekView)
        }
    }

    fun exportCsv() {
        _uiState.update { it.copy(exportStatus = "Exported") }
    }

    fun clearExportStatus() {
        _uiState.update { it.copy(exportStatus = "") }
    }

    private fun buildChartEntries(entries: List<WaterIntake>, days: Int): List<ChartEntry> {
        val totalsByDate = entries.groupBy { millisToDate(it.timestamp) }
            .mapValues { (_, list) -> list.sumOf { it.amountMl } }
        val startDate = LocalDate.now().minusDays(days.toLong() - 1)
        return (0 until days).map { offset ->
            val date = startDate.plusDays(offset.toLong())
            ChartEntry(date, totalsByDate[date] ?: 0)
        }
    }

    private fun buildHeatmap(entries: List<WaterIntake>, dailyGoalMl: Int): List<HeatmapDay> {
        val totalsByDate = entries.groupBy { millisToDate(it.timestamp) }
            .mapValues { (_, list) -> list.sumOf { it.amountMl } }
        val days = 28
        val startDate = LocalDate.now().minusDays(days.toLong() - 1)
        return (0 until days).map { offset ->
            val date = startDate.plusDays(offset.toLong())
            val total = totalsByDate[date] ?: 0
            val intensity = if (dailyGoalMl > 0) total.toFloat() / dailyGoalMl else 0f
            HeatmapDay(date, intensity.coerceIn(0f, 1f))
        }
    }

    private fun calculateStreak(entries: List<WaterIntake>, dailyGoalMl: Int): Int {
        val totalsByDate = entries.groupBy { millisToDate(it.timestamp) }
            .mapValues { (_, list) -> list.sumOf { it.amountMl } }
        var streak = 0
        var date = LocalDate.now()
        while (true) {
            val total = totalsByDate[date] ?: 0
            if (total < dailyGoalMl) break
            streak++
            date = date.minusDays(1)
        }
        return streak
    }

    private fun millisToDate(millis: Long): LocalDate {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    class Factory(
        private val observeHistoryUseCase: ObserveHistoryUseCase,
        private val observeGoalConfigUseCase: ObserveGoalConfigUseCase,
        private val preferencesManager: PreferencesManager,
        private val syncRepository: com.example.waterreminder.data.repository.SyncRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HistoryViewModel(observeHistoryUseCase, observeGoalConfigUseCase, preferencesManager, syncRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
