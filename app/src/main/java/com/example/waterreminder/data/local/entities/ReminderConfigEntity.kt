package com.example.waterreminder.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(tableName = "reminder_config")
data class ReminderConfigEntity(
    @PrimaryKey
    val id: Int = 1,
    val intervalMinutes: Int,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val enabled: Boolean
)
