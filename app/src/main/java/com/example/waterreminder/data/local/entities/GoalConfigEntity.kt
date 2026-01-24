package com.example.waterreminder.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goal_config")
data class GoalConfigEntity(
    @PrimaryKey
    val id: Int = 1,
    val dailyGoalMl: Int,
    val cupSizeMl: Int,
    val adaptive: Boolean
)
