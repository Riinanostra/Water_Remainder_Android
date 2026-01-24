package com.example.waterreminder.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.waterreminder.data.local.entities.ReminderConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderConfigDao {
    @Query("SELECT * FROM reminder_config WHERE id = 1")
    fun observeConfig(): Flow<ReminderConfigEntity?>

    @Query("SELECT * FROM reminder_config WHERE id = 1")
    suspend fun getConfig(): ReminderConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ReminderConfigEntity)
}
