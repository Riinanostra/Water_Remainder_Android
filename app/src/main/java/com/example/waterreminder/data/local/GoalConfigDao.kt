package com.example.waterreminder.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.waterreminder.data.local.entities.GoalConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalConfigDao {
    @Query("SELECT * FROM goal_config WHERE id = 1")
    fun observeConfig(): Flow<GoalConfigEntity?>

    @Query("SELECT * FROM goal_config WHERE id = 1")
    suspend fun getConfig(): GoalConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: GoalConfigEntity)
}
