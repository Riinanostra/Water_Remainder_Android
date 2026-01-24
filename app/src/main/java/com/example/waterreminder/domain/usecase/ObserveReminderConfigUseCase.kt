package com.example.waterreminder.domain.usecase

import com.example.waterreminder.data.repository.ReminderRepository
import com.example.waterreminder.domain.model.ReminderConfig
import kotlinx.coroutines.flow.Flow

class ObserveReminderConfigUseCase(private val repository: ReminderRepository) {
    operator fun invoke(): Flow<ReminderConfig> = repository.observeConfig()
}
