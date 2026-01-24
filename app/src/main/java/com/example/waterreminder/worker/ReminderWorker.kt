package com.example.waterreminder.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.waterreminder.data.local.WaterDatabase
import com.example.waterreminder.notification.NotificationHelper
import com.example.waterreminder.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val dao = WaterDatabase.getInstance(applicationContext).reminderConfigDao()
        val config = dao.getConfig()
        if (config == null || !config.enabled) {
            return@withContext Result.success()
        }
        val now = LocalTime.now()
        val within = DateUtils.isWithinActiveHours(now, config.startTime, config.endTime)
        if (!within) {
            return@withContext Result.success()
        }
        NotificationHelper.showReminder(
            applicationContext,
            title = "Time to hydrate",
            message = "Take a sip and keep your streak going."
        )
        Result.success()
    }

    companion object {
        private const val WORK_NAME = "water_reminder_worker"

        fun schedule(context: Context, intervalMinutes: Int) {
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(
                intervalMinutes.toLong(),
                TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
