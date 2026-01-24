package com.example.waterreminder.domain.model

/**
 * User goal configuration for daily water intake.
 */
data class GoalConfig(
    val dailyGoalMl: Int,
    val cupSizeMl: Int,
    val adaptive: Boolean
)
