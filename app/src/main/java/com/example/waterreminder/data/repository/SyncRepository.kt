package com.example.waterreminder.data.repository

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.example.waterreminder.data.datastore.PreferencesManager
import com.example.waterreminder.data.remote.NetworkModule
import com.example.waterreminder.data.remote.dto.DevicePayloadDto
import com.example.waterreminder.data.remote.dto.HistoryEntryDto
import com.example.waterreminder.data.remote.dto.HistoryPayloadDto
import com.example.waterreminder.domain.model.WaterIntake
import kotlinx.coroutines.flow.first
import java.util.Locale
import java.util.TimeZone

class SyncRepository(
    private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val goalRepository: GoalRepository,
    private val waterRepository: WaterRepository
) {
    private val api by lazy { NetworkModule.apiService(context) }
    private val tag = "SyncRepository"

    suspend fun uploadHistory(): Result<Int> = runCatching {
        val entries = waterRepository.getAll()
        if (entries.isEmpty()) return@runCatching 0
        val payload = HistoryPayloadDto(
            deviceId = deviceId(),
            entries = entries.map { it.toDto() }
        )
        api.uploadHistory(payload)
        entries.size
    }.onFailure { error ->
        Log.e(tag, "uploadHistory failed", error)
    }

    suspend fun uploadDeviceInfo(): Result<Unit> = runCatching {
        val unitSystem = preferencesManager.unitSystem.first().name
        val themeMode = preferencesManager.themeMode.first().name
        val weeklyTarget = preferencesManager.weeklyTarget.first()
        val goalConfig = goalRepository.getConfig()
        val payload = DevicePayloadDto(
            deviceId = deviceId(),
            manufacturer = android.os.Build.MANUFACTURER,
            model = android.os.Build.MODEL,
            sdkInt = android.os.Build.VERSION.SDK_INT,
            appVersion = "${context.packageManager.getPackageInfo(context.packageName, 0).versionName}",
            locale = Locale.getDefault().toLanguageTag(),
            timeZone = TimeZone.getDefault().id,
            unitSystem = unitSystem,
            themeMode = themeMode,
            dailyGoalMl = goalConfig.dailyGoalMl,
            cupSizeMl = goalConfig.cupSizeMl,
            adaptive = goalConfig.adaptive,
            weeklyTargetDays = weeklyTarget
        )
        api.uploadDevice(payload)
    }.onFailure { error ->
        Log.e(tag, "uploadDeviceInfo failed", error)
    }

    private fun deviceId(): String? {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    private fun WaterIntake.toDto(): HistoryEntryDto {
        return HistoryEntryDto(entryId = id, timestamp = timestamp, amountMl = amountMl)
    }
}
