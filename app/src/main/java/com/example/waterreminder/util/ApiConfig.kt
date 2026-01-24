package com.example.waterreminder.util

import android.content.Context
import com.example.waterreminder.BuildConfig
import org.json.JSONObject
import java.io.File

object ApiConfig {
    private const val CONFIG_FILE_NAME = "server_config.json"

    data class DeviceConfig(
        val baseUrl: String? = null,
        val apiKey: String? = null
    )

    fun resolveBaseUrl(context: Context): String {
        val override = readConfig(context)?.baseUrl?.trim().orEmpty()
        if (override.isNotBlank()) {
            return if (override.endsWith("/")) override else "$override/"
        }
        return BuildConfig.BASE_URL
    }

    fun resolveApiKey(context: Context): String {
        return readConfig(context)?.apiKey?.trim().orEmpty().ifBlank { BuildConfig.API_KEY }
    }

    fun configPath(context: Context): String {
        return File(context.filesDir, CONFIG_FILE_NAME).absolutePath
    }

    private fun readConfig(context: Context): DeviceConfig? {
        val file = File(context.filesDir, CONFIG_FILE_NAME)
        if (!file.exists()) return null
        return runCatching {
            val json = JSONObject(file.readText())
            DeviceConfig(
                baseUrl = json.optString("baseUrl").takeIf { it.isNotBlank() },
                apiKey = json.optString("apiKey").takeIf { it.isNotBlank() }
            )
        }.getOrNull()
    }
}
