package com.example.waterreminder.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.waterreminder.data.local.WaterDatabase
import com.example.waterreminder.data.local.entities.ReminderConfigEntity
import com.example.waterreminder.worker.ReminderWorker
import com.example.waterreminder.worker.SnoozeWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_SNOOZE -> handleSnooze(context)
            ACTION_STOP -> handleStop(context)
        }
    }

    private fun handleSnooze(context: Context) {
        NotificationManagerCompat.from(context).cancel(NotificationHelper.NOTIFICATION_ID)
        val request = OneTimeWorkRequestBuilder<SnoozeWorker>()
            .setInitialDelay(SNOOZE_MINUTES.toLong(), TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }

    private fun handleStop(context: Context) {
        NotificationManagerCompat.from(context).cancel(NotificationHelper.NOTIFICATION_ID)
        ReminderWorker.cancel(context)
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val dao = WaterDatabase.getInstance(context).reminderConfigDao()
            val current = dao.getConfig()
            val updated = current?.copy(enabled = false) ?: ReminderConfigEntity(
                intervalMinutes = DEFAULT_INTERVAL_MINUTES,
                startTime = DEFAULT_START_TIME,
                endTime = DEFAULT_END_TIME,
                enabled = false
            )
            dao.upsert(updated)
            pendingResult.finish()
        }
    }

    companion object {
        const val ACTION_SNOOZE = "com.example.waterreminder.action.SNOOZE"
        const val ACTION_STOP = "com.example.waterreminder.action.STOP"
        private const val SNOOZE_MINUTES = 10
        private const val DEFAULT_INTERVAL_MINUTES = 60
        private val DEFAULT_START_TIME = LocalTime.of(8, 0)
        private val DEFAULT_END_TIME = LocalTime.of(22, 0)
    }
}
