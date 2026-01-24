package com.example.waterreminder.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.waterreminder.data.local.entities.GoalConfigEntity
import com.example.waterreminder.data.local.entities.ReminderConfigEntity
import com.example.waterreminder.data.local.entities.WaterIntakeEntity

@Database(
    entities = [
        WaterIntakeEntity::class,
        ReminderConfigEntity::class,
        GoalConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WaterDatabase : RoomDatabase() {
    abstract fun waterIntakeDao(): WaterIntakeDao
    abstract fun reminderConfigDao(): ReminderConfigDao
    abstract fun goalConfigDao(): GoalConfigDao

    companion object {
        @Volatile
        private var instance: WaterDatabase? = null

        fun getInstance(context: Context): WaterDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    WaterDatabase::class.java,
                    "water_db"
                ).build().also { instance = it }
            }
        }
    }
}
