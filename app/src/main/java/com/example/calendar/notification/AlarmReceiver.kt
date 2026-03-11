package com.example.calendar.notification

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.calendar.R

class AlarmReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Reminder"
        val message = intent.getStringExtra("message") ?: "Event Reminder"
        showNotification(context, title, message)
    }
}


fun scheduleEventNotification(
    context: Context,
    date: String,
    time: String,
    title: String,
    reminderMinutesAfter: Int
) {
    try {

        val formatter = java.time.format.DateTimeFormatter.ofPattern("d/M/yyyy hh:mm a")

        val eventDateTime =
            java.time.LocalDateTime.parse("$date $time", formatter)

        // reminder after selected minutes
        val reminderTime =
            eventDateTime.plusMinutes(reminderMinutesAfter.toLong())

        val triggerMillis =
            reminderTime.atZone(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

        scheduleNotification(
            context = context,
            triggerTime = triggerMillis,
            title = title,
            message = "Event Reminder",
            requestCode = System.currentTimeMillis().toInt()
        )

        Log.d("Alarm", "Notification scheduled for $reminderTime")

    } catch (e: Exception) {
        Log.e("Alarm", "Error scheduling notification: ${e.message}")
    }
}

@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
fun showNotification(context: Context, title: String, message: String) {
    createNotificationChannel(context)

    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, "event_channel")
        .setSmallIcon(R.drawable.calendar)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)          // notification auto dismiss
        .setContentIntent(pendingIntent) // click action
        .build()

    NotificationManagerCompat.from(context)
        .notify(System.currentTimeMillis().toInt(), notification)
}

fun scheduleNotification(
    context: Context,
    triggerTime: Long,
    title: String,
    message: String,
    requestCode: Int
) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!alarmManager.canScheduleExactAlarms()) {
            requestExactAlarmPermission(context)
            Log.w("Alarm", "SCHEDULE_EXACT_ALARM permission denied → asking user")
            return
        }
    }

    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("title", title)
        putExtra("message", message)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    try {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
        Log.d("Alarm", "✅ Scheduled exact alarm for $triggerTime")
    } catch (e: SecurityException) {
        Log.e("Alarm", "Exact alarm failed: ${e.message}")
    }
}

fun requestExactAlarmPermission(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    try {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        })
    }
}

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = android.app.NotificationChannel(
            "event_channel",
            "Event Reminders",
            android.app.NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Calendar event reminders"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.createNotificationChannel(channel)
    }
}