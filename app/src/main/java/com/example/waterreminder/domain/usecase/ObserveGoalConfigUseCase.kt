package com.example.waterreminder.domain.usecase

import com.example.waterreminder.data.repository.GoalRepository
import com.example.waterreminder.domain.model.GoalConfig
import kotlinx.coroutines.flow.Flow

class ObserveGoalConfigUseCase(private val repository: GoalRepository) {
    operator fun invoke(): Flow<GoalConfig> = repository.observeConfig()
}
