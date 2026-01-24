package com.example.waterreminder.domain.usecase

import com.example.waterreminder.data.repository.GoalRepository
import com.example.waterreminder.domain.model.GoalConfig

class UpdateGoalConfigUseCase(private val repository: GoalRepository) {
    suspend operator fun invoke(config: GoalConfig) {
        repository.update(config)
    }
}
