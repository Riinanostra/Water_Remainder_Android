package com.example.waterreminder.util

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val UNIT_SYSTEM = stringPreferencesKey("unit_system")
    val SMART_OFFICE = booleanPreferencesKey("smart_office")
    val SMART_WORKOUT = booleanPreferencesKey("smart_workout")
    val SMART_TRAVEL = booleanPreferencesKey("smart_travel")
    val NOTIFICATION_PERMISSION_ASKED = booleanPreferencesKey("notification_permission_asked")
    val WEEK_VIEW = booleanPreferencesKey("history_week_view")
    val WEEKLY_TARGET = intPreferencesKey("weekly_target")
    val ONBOARD_REMINDERS_SHOWN = booleanPreferencesKey("onboard_reminders_shown")
    val ONBOARD_GOALS_SHOWN = booleanPreferencesKey("onboard_goals_shown")
    val LAST_TAB_ROUTE = stringPreferencesKey("last_tab_route")
    val HOME_SCROLL = intPreferencesKey("home_scroll")
    val REMINDERS_SCROLL = intPreferencesKey("reminders_scroll")
    val GOALS_SCROLL = intPreferencesKey("goals_scroll")
    val HISTORY_SCROLL = intPreferencesKey("history_scroll")
    val PROFILE_SCROLL = intPreferencesKey("profile_scroll")
}

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class UnitSystem { ML }

enum class SmartMode { NONE, OFFICE, WORKOUT, TRAVEL }
