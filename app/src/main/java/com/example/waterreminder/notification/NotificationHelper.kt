package com.example.waterreminder.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat.Action
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.waterreminder.R
import com.example.waterreminder.notification.ReminderActionReceiver.Companion.ACTION_SNOOZE
import com.example.waterreminder.notification.ReminderActionReceiver.Companion.ACTION_STOP

object NotificationHelper {
    const val CHANNEL_ID = "water_reminder"
    const val NOTIFICATION_ID = 1001
    private const val CHANNEL_NAME = "Water Reminders"
    private const val CHANNEL_DESCRIPTION = "Scheduled water reminder notifications"
    private const val SNOOZE_REQUEST_CODE = 2001
    private const val STOP_REQUEST_CODE = 2002
    private const val SNOOZE_LABEL = "Snooze 10 min"
    private const val STOP_LABEL = "Stop"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showReminder(context: Context, title: String, message: String) {
        ensureChannel(context)
        val snoozeIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
        }
        val stopIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ACTION_STOP
        }
        val pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            SNOOZE_REQUEST_CODE,
            snoozeIntent,
            pendingFlags
        )
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            STOP_REQUEST_CODE,
            stopIntent,
            pendingFlags
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(Action(android.R.drawable.ic_lock_idle_alarm, SNOOZE_LABEL, snoozePendingIntent))
            .addAction(Action(android.R.drawable.ic_delete, STOP_LABEL, stopPendingIntent))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
