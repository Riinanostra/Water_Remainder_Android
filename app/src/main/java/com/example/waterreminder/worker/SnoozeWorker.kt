package com.example.waterreminder.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.waterreminder.notification.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SnoozeWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        NotificationHelper.showReminder(
            applicationContext,
            title = "Time to hydrate",
            message = "Snoozed reminder: take a sip now."
        )
        Result.success()
    }
}
