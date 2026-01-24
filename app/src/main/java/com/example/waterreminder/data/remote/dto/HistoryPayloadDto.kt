package com.example.waterreminder.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HistoryEntryDto(
    val entryId: Long?,
    val timestamp: Long,
    val amountMl: Int
)

@JsonClass(generateAdapter = true)
data class HistoryPayloadDto(
    val deviceId: String?,
    val entries: List<HistoryEntryDto>
)
