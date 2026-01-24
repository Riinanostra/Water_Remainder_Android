package com.example.waterreminder.data.remote

import com.example.waterreminder.data.remote.dto.DevicePayloadDto
import com.example.waterreminder.data.remote.dto.HistoryPayloadDto
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("history")
    suspend fun uploadHistory(@Body payload: HistoryPayloadDto)

    @POST("device")
    suspend fun uploadDevice(@Body payload: DevicePayloadDto)
}
