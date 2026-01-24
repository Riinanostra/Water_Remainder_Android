package com.example.waterreminder.domain.usecase

import com.example.waterreminder.data.repository.WaterRepository

class LogWaterUseCase(private val repository: WaterRepository) {
    suspend operator fun invoke(amountMl: Int) {
        repository.logWater(amountMl)
    }
}
