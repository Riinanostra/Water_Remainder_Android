package com.example.waterreminder.domain.model

/**
 * Represents a single water intake event.
 */
data class WaterIntake(
    val id: Long,
    val timestamp: Long,
    val amountMl: Int
)
