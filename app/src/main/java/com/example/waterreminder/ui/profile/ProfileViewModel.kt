package com.example.waterreminder.ui.profile

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.waterreminder.data.datastore.PreferencesManager
import com.example.waterreminder.data.repository.SyncRepository
import com.example.waterreminder.data.repository.WaterRepository
import com.example.waterreminder.util.ThemeMode
import com.example.waterreminder.util.UnitSystem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ProfileUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val unitSystem: UnitSystem = UnitSystem.ML,
    val exportStatus: String = "",
    val resetStatus: String = "",
    val syncStatus: String = ""
)

class ProfileViewModel(
    private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val waterRepository: WaterRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState
    private var lastResetEntries: List<com.example.waterreminder.domain.model.WaterIntake> = emptyList()

    init {
        viewModelScope.launch {
            combine(preferencesManager.themeMode, preferencesManager.unitSystem) { theme, unit ->
                ProfileUiState(themeMode = theme, unitSystem = unit)
            }.collect { state ->
                _uiState.update { it.copy(themeMode = state.themeMode, unitSystem = state.unitSystem) }
            }
        }
    }

    fun updateTheme(mode: ThemeMode) {
        viewModelScope.launch { preferencesManager.setThemeMode(mode) }
    }

    fun updateUnits(system: UnitSystem) {
        viewModelScope.launch { preferencesManager.setUnitSystem(system) }
    }

    fun exportData() {
        viewModelScope.launch {
            runCatching {
                val entries = waterRepository.getAll()
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val csv = buildString {
                    append("id,timestamp,amount_ml\n")
                    entries.forEach { entry ->
                        val time = formatter.format(Date(entry.timestamp))
                        append("${entry.id},$time,${entry.amountMl}\n")
                    }
                }
                val fileName = "water_export_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.csv"
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Downloads.IS_PENDING, 1)
                    }
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: error("Unable to create export file")
                resolver.openOutputStream(uri)?.use { stream ->
                    stream.write(csv.toByteArray())
                } ?: error("Unable to write export file")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                }
                "Exported to Downloads"
            }.onSuccess { message ->
                _uiState.update { it.copy(exportStatus = message, resetStatus = "") }
            }.onFailure {
                _uiState.update { it.copy(exportStatus = "Export failed", resetStatus = "") }
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            runCatching {
                val historyResult = syncRepository.uploadHistory()
                val deviceResult = syncRepository.uploadDeviceInfo()
                if (historyResult.isSuccess && deviceResult.isSuccess) {
                    "Sync complete"
                } else {
                    val historyError = historyResult.exceptionOrNull()?.message
                    val deviceError = deviceResult.exceptionOrNull()?.message
                    val details = listOfNotNull(historyError, deviceError).joinToString(" | ")
                    if (details.isNotBlank()) "Sync failed: $details" else "Sync failed"
                }
            }.onSuccess { message ->
                _uiState.update { it.copy(syncStatus = message, exportStatus = "", resetStatus = "") }
            }.onFailure { error ->
                val msg = error.message ?: "Sync failed"
                _uiState.update { it.copy(syncStatus = "Sync failed: $msg", exportStatus = "", resetStatus = "") }
            }
        }
    }

    fun resetDailyIntake() {
        viewModelScope.launch {
            runCatching {
                lastResetEntries = waterRepository.getTodayEntries()
                waterRepository.resetTodayIntake()
                "Daily intake reset"
            }.onSuccess { message ->
                _uiState.update { it.copy(resetStatus = message, exportStatus = "") }
            }.onFailure {
                _uiState.update { it.copy(resetStatus = "Reset failed", exportStatus = "") }
            }
        }
    }

    fun undoResetDailyIntake() {
        viewModelScope.launch {
            runCatching {
                waterRepository.restoreEntries(lastResetEntries)
                lastResetEntries = emptyList()
                "Reset undone"
            }.onSuccess { message ->
                _uiState.update { it.copy(resetStatus = message) }
            }.onFailure {
                _uiState.update { it.copy(resetStatus = "Undo failed") }
            }
        }
    }

    fun clearResetStatus() {
        _uiState.update { it.copy(resetStatus = "") }
    }

    fun clearExportStatus() {
        _uiState.update { it.copy(exportStatus = "") }
    }

    fun clearSyncStatus() {
        _uiState.update { it.copy(syncStatus = "") }
    }

    class Factory(
        private val context: Context,
        private val preferencesManager: PreferencesManager,
        private val waterRepository: WaterRepository,
        private val syncRepository: SyncRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(context, preferencesManager, waterRepository, syncRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
