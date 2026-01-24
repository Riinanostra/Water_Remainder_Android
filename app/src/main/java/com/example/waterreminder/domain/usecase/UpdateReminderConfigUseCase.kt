package com.example.waterreminder.domain.usecase

import com.example.waterreminder.data.repository.ReminderRepository
import com.example.waterreminder.domain.model.ReminderConfig

class UpdateReminderConfigUseCase(private val repository: ReminderRepository) {
    suspend operator fun invoke(config: ReminderConfig) {
        repository.update(config)
    }
}
