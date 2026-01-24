package com.example.waterreminder.domain.usecase

import com.example.waterreminder.data.repository.WaterRepository
import com.example.waterreminder.domain.model.WaterIntake
import kotlinx.coroutines.flow.Flow

class ObserveTodayEntriesUseCase(private val repository: WaterRepository) {
    operator fun invoke(): Flow<List<WaterIntake>> = repository.observeTodayEntries()
}
