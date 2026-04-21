package com.ahu.ahutong.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.ahu.ahutong.MainActivity
import com.ahu.ahutong.R
import com.ahu.ahutong.notification.model.CourseReminderPayload

object CourseLiveUpdateHelper {
    private const val LIVE_NOTIFICATION_ID = 4096
    private const val LIVE_UPDATE_REQUEST_CODE = 4097
    private const val ONE_MINUTE_MS = 60_000L

    fun showLiveUpdate(
        context: Context,
        payload: CourseReminderPayload
    ): Boolean {
        val courseStartAtMillis = payload.courseStartAtMillis ?: return false
        val remainingDurationMs = courseStartAtMillis - System.currentTimeMillis()
        val remainingMinutes = calculateRemainingMinutes(remainingDurationMs)
        if (remainingMinutes <= 0) {
            cancel(context)
            cancelScheduledUpdate(context)
            return false
        }

        val countdownText = buildCountdownText(remainingMinutes)
        val collapsedText = buildCollapsedText(payload, countdownText)
        val expandedText = buildExpandedText(payload, countdownText)
        val notification = NotificationCompat.Builder(context, CourseReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(payload.courseName)
            .setContentText(collapsedText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setAutoCancel(false)
            .setShowWhen(false)
            .setTimeoutAfter(remainingDurationMs)
            .setRequestPromotedOngoing(true)
            .setShortCriticalText(buildChipText(remainingMinutes))
            .setContentIntent(buildContentIntent(context, payload.notificationId))
            .build()

        NotificationManagerCompat.from(context).notify(LIVE_NOTIFICATION_ID, notification)
        return true
    }

    fun scheduleNextUpdate(
        context: Context,
        payload: CourseReminderPayload
    ) {
        val courseStartAtMillis = payload.courseStartAtMillis ?: return
        val remainingMinutes = calculateRemainingMinutes(
            courseStartAtMillis - System.currentTimeMillis()
        )
        if (remainingMinutes <= 1) {
            cancelScheduledUpdate(context)
            return
        }

        val nextUpdateAtMillis = courseStartAtMillis - (remainingMinutes - 1L) * ONE_MINUTE_MS
        val pendingIntent = buildUpdatePendingIntent(context, payload)
        cancelScheduledUpdate(context)
        scheduleAlarm(context, nextUpdateAtMillis, pendingIntent)
    }

    fun cancel(context: Context) {
        NotificationManagerCompat.from(context).cancel(LIVE_NOTIFICATION_ID)
    }

    fun cancelScheduledUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            LIVE_UPDATE_REQUEST_CODE,
            Intent(context, CourseReminderReceiver::class.java).apply {
                action = CourseReminderReceiver.ACTION_UPDATE_LIVE_COUNTDOWN
            },
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun buildContentIntent(
        context: Context,
        requestCode: Int
    ): PendingIntent? {
        val openIntent = Intent(context, MainActivity::class.java)
        return TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(openIntent)
            .getPendingIntent(
                requestCode,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
    }

    private fun buildUpdatePendingIntent(
        context: Context,
        payload: CourseReminderPayload?
    ): PendingIntent {
        val intent = Intent(context, CourseReminderReceiver::class.java).apply {
            action = CourseReminderReceiver.ACTION_UPDATE_LIVE_COUNTDOWN
            payload?.writeToIntent(this)
        }
        return PendingIntent.getBroadcast(
            context,
            LIVE_UPDATE_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun scheduleAlarm(
        context: Context,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun calculateRemainingMinutes(
        remainingDurationMs: Long
    ): Int {
        if (remainingDurationMs <= 0L) return 0
        return ((remainingDurationMs - 1L) / ONE_MINUTE_MS + 1L).toInt()
    }

    private fun buildChipText(
        remainingMinutes: Int
    ): String = "${remainingMinutes}分钟"

    private fun buildCountdownText(
        remainingMinutes: Int
    ): String = "${remainingMinutes} 分钟后上课"

    private fun buildExpandedText(
        payload: CourseReminderPayload,
        countdownText: String
    ): String {
        return buildString {
            if (!payload.location.isNullOrBlank()) {
                append(payload.location)
                append('\n')
            }
            append(countdownText)
        }
    }

    private fun buildCollapsedText(
        payload: CourseReminderPayload,
        countdownText: String
    ): String {
        val location = payload.location?.takeIf { it.isNotBlank() } ?: return countdownText
        return "$location · $countdownText"
    }
}