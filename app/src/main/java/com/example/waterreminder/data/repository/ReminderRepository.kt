package com.example.waterreminder.data.repository

import com.example.waterreminder.data.local.ReminderConfigDao
import com.example.waterreminder.data.local.entities.ReminderConfigEntity
import com.example.waterreminder.domain.model.ReminderConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

class ReminderRepository(private val dao: ReminderConfigDao) {
    private val defaultConfig = ReminderConfig(
        intervalMinutes = 60,
        startTime = LocalTime.of(8, 0),
        endTime = LocalTime.of(22, 0),
        enabled = false
    )

    fun observeConfig(): Flow<ReminderConfig> {
        return dao.observeConfig().map { entity ->
            entity?.toDomain() ?: defaultConfig
        }
    }

    suspend fun getConfig(): ReminderConfig {
        return dao.getConfig()?.toDomain() ?: defaultConfig
    }

    suspend fun update(config: ReminderConfig) {
        dao.upsert(config.toEntity())
    }

    suspend fun ensureDefault() {
        if (dao.getConfig() == null) {
            dao.upsert(defaultConfig.toEntity())
        }
    }

    private fun ReminderConfigEntity.toDomain() = ReminderConfig(
        intervalMinutes = intervalMinutes,
        startTime = startTime,
        endTime = endTime,
        enabled = enabled
    )

    private fun ReminderConfig.toEntity() = ReminderConfigEntity(
        intervalMinutes = intervalMinutes,
        startTime = startTime,
        endTime = endTime,
        enabled = enabled
    )
}
