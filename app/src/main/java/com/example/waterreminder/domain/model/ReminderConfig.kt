package com.example.waterreminder.domain.model

import java.time.LocalTime

/**
 * Reminder configuration for background notifications.
 */
data class ReminderConfig(
    val intervalMinutes: Int,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val enabled: Boolean
)
