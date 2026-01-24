package com.example.waterreminder.domain.usecase

import com.example.waterreminder.data.repository.WaterRepository
import kotlinx.coroutines.flow.Flow

class ObserveTodayIntakeUseCase(private val repository: WaterRepository) {
    operator fun invoke(): Flow<Int> = repository.observeTodayTotal()
}
