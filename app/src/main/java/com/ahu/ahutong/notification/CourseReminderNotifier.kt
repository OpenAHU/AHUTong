package com.ahu.ahutong.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.ahu.ahutong.MainActivity
import com.ahu.ahutong.R
import com.ahu.ahutong.notification.model.CourseReminderPayload

object CourseReminderNotifier {
    fun showReminder(
        context: Context,
        payload: CourseReminderPayload
    ): Boolean {
        if (!canPostNotifications(context)) return false

        if (CourseReminderCapability.shouldTryLiveCountdown(context, payload)) {
            val shown = CourseLiveUpdateHelper.showLiveUpdate(context, payload)
            if (shown) return true
        }

        CourseLiveUpdateHelper.cancel(context)
        CourseLiveUpdateHelper.cancelScheduledUpdate(context)
        showStandardReminder(context, payload)
        return false
    }

    fun cancelActiveReminder(context: Context) {
        CourseLiveUpdateHelper.cancel(context)
        CourseLiveUpdateHelper.cancelScheduledUpdate(context)
    }

    private fun showStandardReminder(
        context: Context,
        payload: CourseReminderPayload
    ) {
        CourseReminderScheduler.createNotificationChannel(context)

        val contentText = buildString {
            append("10 分钟后上课")
            if (!payload.location.isNullOrBlank()) {
                append(" · ")
                append(payload.location)
            }
            if (!payload.timeText.isNullOrBlank()) {
                append(" · ")
                append(payload.timeText)
            }
        }

        val notification = NotificationCompat.Builder(context, CourseReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(payload.courseName)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(buildContentIntent(context, payload.notificationId))
            .build()

        NotificationManagerCompat.from(context).notify(payload.notificationId, notification)
    }

    private fun canPostNotifications(context: Context): Boolean {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return true
    }

    private fun buildContentIntent(
        context: Context,
        requestCode: Int
    ): PendingIntent? {
        return TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(Intent(context, MainActivity::class.java))
            .getPendingIntent(
                requestCode,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
    }
}
