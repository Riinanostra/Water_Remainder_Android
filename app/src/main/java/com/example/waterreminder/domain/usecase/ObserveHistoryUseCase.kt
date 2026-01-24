package com.example.waterreminder.domain.usecase

import com.example.waterreminder.data.repository.WaterRepository
import com.example.waterreminder.domain.model.WaterIntake
import kotlinx.coroutines.flow.Flow

class ObserveHistoryUseCase(private val repository: WaterRepository) {
    operator fun invoke(startMillis: Long, endMillis: Long): Flow<List<WaterIntake>> {
        return repository.observeHistory(startMillis, endMillis)
    }
}
