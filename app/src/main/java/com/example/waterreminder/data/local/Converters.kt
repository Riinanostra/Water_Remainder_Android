package com.example.waterreminder.data.local

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime

class Converters {
    @TypeConverter
    fun localTimeToMinutes(value: LocalTime?): Int? = value?.toSecondOfDay()?.div(60)

    @TypeConverter
    fun minutesToLocalTime(value: Int?): LocalTime? = value?.let { LocalTime.ofSecondOfDay((it * 60).toLong()) }

    @TypeConverter
    fun localDateToEpochDay(value: LocalDate?): Long? = value?.toEpochDay()

    @TypeConverter
    fun epochDayToLocalDate(value: Long?): LocalDate? = value?.let { LocalDate.ofEpochDay(it) }
}
