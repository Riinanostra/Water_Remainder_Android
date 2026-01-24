package com.example.waterreminder.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object DateUtils {
    fun startOfDayMillis(date: LocalDate = LocalDate.now()): Long {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun endOfDayMillis(date: LocalDate = LocalDate.now()): Long {
        return date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
    }

    fun nowMillis(): Long = Instant.now().toEpochMilli()

    fun isWithinActiveHours(now: LocalTime, start: LocalTime, end: LocalTime): Boolean {
        return if (start <= end) {
            now >= start && now <= end
        } else {
            now >= start || now <= end
        }
    }

    fun nextReminderTime(
        now: LocalDateTime,
        start: LocalTime,
        end: LocalTime,
        intervalMinutes: Int
    ): LocalDateTime? {
        val todayStart = now.toLocalDate().atTime(start)
        val todayEnd = now.toLocalDate().atTime(end)
        val within = isWithinActiveHours(now.toLocalTime(), start, end)
        if (!within) {
            return if (now.toLocalTime() < start) todayStart else todayStart.plusDays(1)
        }
        val minutesSinceStart = java.time.Duration.between(todayStart, now).toMinutes().coerceAtLeast(0)
        val nextSlot = ((minutesSinceStart / intervalMinutes) + 1) * intervalMinutes
        val next = todayStart.plusMinutes(nextSlot)
        return if (start <= end) {
            if (next <= todayEnd) next else todayStart.plusDays(1)
        } else {
            val crossesMidnightEnd = todayEnd.plusDays(1)
            if (next <= crossesMidnightEnd) next else todayStart.plusDays(1)
        }
    }
}
