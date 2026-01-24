package com.example.waterreminder.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.waterreminder.util.PreferencesKeys
import com.example.waterreminder.util.ThemeMode
import com.example.waterreminder.util.UnitSystem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("water_prefs")

class PreferencesManager(private val context: Context) {
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        val value = prefs[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        runCatching { ThemeMode.valueOf(value) }.getOrDefault(ThemeMode.SYSTEM)
    }

    val unitSystem: Flow<UnitSystem> = context.dataStore.data.map { prefs ->
        val value = prefs[PreferencesKeys.UNIT_SYSTEM] ?: UnitSystem.ML.name
        if (value != UnitSystem.ML.name) {
            UnitSystem.ML
        } else {
            UnitSystem.ML
        }
    }

    val smartOffice: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.SMART_OFFICE] ?: true
    }

    val smartWorkout: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.SMART_WORKOUT] ?: true
    }

    val smartTravel: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.SMART_TRAVEL] ?: false
    }

    val notificationPermissionAsked: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.NOTIFICATION_PERMISSION_ASKED] ?: false
    }

    val weekView: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.WEEK_VIEW] ?: true
    }

    val weeklyTarget: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.WEEKLY_TARGET] ?: 7
    }

    val onboardingRemindersShown: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.ONBOARD_REMINDERS_SHOWN] ?: false
    }

    val onboardingGoalsShown: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.ONBOARD_GOALS_SHOWN] ?: false
    }

    val lastTabRoute: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.LAST_TAB_ROUTE] ?: "home"
    }

    val homeScroll: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.HOME_SCROLL] ?: 0
    }

    val remindersScroll: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.REMINDERS_SCROLL] ?: 0
    }

    val goalsScroll: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.GOALS_SCROLL] ?: 0
    }

    val historyScroll: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.HISTORY_SCROLL] ?: 0
    }

    val profileScroll: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.PROFILE_SCROLL] ?: 0
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode.name }
    }

    suspend fun setUnitSystem(system: UnitSystem) {
        context.dataStore.edit { it[PreferencesKeys.UNIT_SYSTEM] = UnitSystem.ML.name }
    }

    suspend fun setSmartOffice(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SMART_OFFICE] = enabled }
    }

    suspend fun setSmartWorkout(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SMART_WORKOUT] = enabled }
    }

    suspend fun setSmartTravel(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SMART_TRAVEL] = enabled }
    }

    suspend fun setNotificationPermissionAsked() {
        context.dataStore.edit { it[PreferencesKeys.NOTIFICATION_PERMISSION_ASKED] = true }
    }

    suspend fun setWeekView(value: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.WEEK_VIEW] = value }
    }

    suspend fun setWeeklyTarget(value: Int) {
        context.dataStore.edit { it[PreferencesKeys.WEEKLY_TARGET] = value }
    }

    suspend fun setOnboardingRemindersShown() {
        context.dataStore.edit { it[PreferencesKeys.ONBOARD_REMINDERS_SHOWN] = true }
    }

    suspend fun setOnboardingGoalsShown() {
        context.dataStore.edit { it[PreferencesKeys.ONBOARD_GOALS_SHOWN] = true }
    }

    suspend fun setLastTabRoute(route: String) {
        context.dataStore.edit { it[PreferencesKeys.LAST_TAB_ROUTE] = route }
    }

    suspend fun setHomeScroll(value: Int) {
        context.dataStore.edit { it[PreferencesKeys.HOME_SCROLL] = value }
    }

    suspend fun setRemindersScroll(value: Int) {
        context.dataStore.edit { it[PreferencesKeys.REMINDERS_SCROLL] = value }
    }

    suspend fun setGoalsScroll(value: Int) {
        context.dataStore.edit { it[PreferencesKeys.GOALS_SCROLL] = value }
    }

    suspend fun setHistoryScroll(value: Int) {
        context.dataStore.edit { it[PreferencesKeys.HISTORY_SCROLL] = value }
    }

    suspend fun setProfileScroll(value: Int) {
        context.dataStore.edit { it[PreferencesKeys.PROFILE_SCROLL] = value }
    }
}
