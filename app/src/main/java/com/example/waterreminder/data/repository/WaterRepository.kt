package com.example.waterreminder.data.repository

import com.example.waterreminder.data.local.WaterIntakeDao
import com.example.waterreminder.data.local.entities.WaterIntakeEntity
import com.example.waterreminder.domain.model.WaterIntake
import com.example.waterreminder.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class WaterRepository(private val dao: WaterIntakeDao) {
    suspend fun logWater(amountMl: Int) {
        val entity = WaterIntakeEntity(
            timestamp = DateUtils.nowMillis(),
            amountMl = amountMl
        )
        dao.insert(entity)
    }

    fun observeTodayTotal(): Flow<Int> {
        val start = DateUtils.startOfDayMillis()
        val end = DateUtils.endOfDayMillis()
        return dao.observeTotalBetween(start, end)
    }

    fun observeTodayEntries(): Flow<List<WaterIntake>> {
        val start = DateUtils.startOfDayMillis()
        val end = DateUtils.endOfDayMillis()
        return dao.observeBetween(start, end).map { list -> list.map { it.toDomain() } }
    }

    fun observeHistory(startMillis: Long, endMillis: Long): Flow<List<WaterIntake>> {
        return dao.observeBetween(startMillis, endMillis).map { list -> list.map { it.toDomain() } }
    }

    fun observeLastIntake(): Flow<WaterIntake?> {
        return dao.observeLast().map { it?.toDomain() }
    }

    suspend fun getAll(): List<WaterIntake> {
        return dao.getAll().map { it.toDomain() }
    }

    suspend fun getTodayEntries(): List<WaterIntake> {
        val start = DateUtils.startOfDayMillis()
        val end = DateUtils.endOfDayMillis()
        return dao.observeBetween(start, end).map { list -> list.map { it.toDomain() } }.first()
    }

    suspend fun resetTodayIntake() {
        val start = DateUtils.startOfDayMillis()
        val end = DateUtils.endOfDayMillis()
        dao.deleteBetween(start, end)
    }

    suspend fun restoreEntries(entries: List<WaterIntake>) {
        if (entries.isEmpty()) return
        dao.insertAll(entries.map { WaterIntakeEntity(id = it.id, timestamp = it.timestamp, amountMl = it.amountMl) })
    }

    private fun WaterIntakeEntity.toDomain() = WaterIntake(id = id, timestamp = timestamp, amountMl = amountMl)
}
