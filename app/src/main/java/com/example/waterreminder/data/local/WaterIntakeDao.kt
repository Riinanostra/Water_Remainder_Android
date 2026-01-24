package com.example.waterreminder.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.waterreminder.data.local.entities.WaterIntakeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterIntakeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WaterIntakeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<WaterIntakeEntity>)

    @Query("SELECT * FROM water_intake WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp ASC")
    fun observeBetween(start: Long, end: Long): Flow<List<WaterIntakeEntity>>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM water_intake WHERE timestamp BETWEEN :start AND :end")
    fun observeTotalBetween(start: Long, end: Long): Flow<Int>

    @Query("SELECT * FROM water_intake ORDER BY timestamp DESC LIMIT 1")
    fun observeLast(): Flow<WaterIntakeEntity?>

    @Query("SELECT * FROM water_intake ORDER BY timestamp DESC")
    suspend fun getAll(): List<WaterIntakeEntity>

    @Query("DELETE FROM water_intake WHERE timestamp BETWEEN :start AND :end")
    suspend fun deleteBetween(start: Long, end: Long)
}
