package com.example.waterreminder.data.repository

import com.example.waterreminder.data.local.GoalConfigDao
import com.example.waterreminder.data.local.entities.GoalConfigEntity
import com.example.waterreminder.domain.model.GoalConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GoalRepository(private val dao: GoalConfigDao) {
    private val defaultConfig = GoalConfig(
        dailyGoalMl = 2000,
        cupSizeMl = 250,
        adaptive = false
    )

    fun observeConfig(): Flow<GoalConfig> {
        return dao.observeConfig().map { entity ->
            entity?.toDomain() ?: defaultConfig
        }
    }

    suspend fun getConfig(): GoalConfig {
        return dao.getConfig()?.toDomain() ?: defaultConfig
    }

    suspend fun update(config: GoalConfig) {
        dao.upsert(config.toEntity())
    }

    suspend fun ensureDefault() {
        if (dao.getConfig() == null) {
            dao.upsert(defaultConfig.toEntity())
        }
    }

    private fun GoalConfigEntity.toDomain() = GoalConfig(
        dailyGoalMl = dailyGoalMl,
        cupSizeMl = cupSizeMl,
        adaptive = adaptive
    )

    private fun GoalConfig.toEntity() = GoalConfigEntity(
        dailyGoalMl = dailyGoalMl,
        cupSizeMl = cupSizeMl,
        adaptive = adaptive
    )
}
