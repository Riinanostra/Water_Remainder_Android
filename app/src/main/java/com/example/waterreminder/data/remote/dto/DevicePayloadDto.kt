package com.example.waterreminder.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DevicePayloadDto(
    val deviceId: String?,
    val manufacturer: String,
    val model: String,
    val sdkInt: Int,
    val appVersion: String,
    val locale: String,
    val timeZone: String,
    val unitSystem: String,
    val themeMode: String,
    val dailyGoalMl: Int,
    val cupSizeMl: Int,
    val adaptive: Boolean,
    val weeklyTargetDays: Int
)
