package com.ahu.ahutong.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.ahu.ahutong.MainActivity
import com.ahu.ahutong.R
import com.ahu.ahutong.notification.model.CourseReminderPayload

object CourseLiveUpdateHelper {
    private const val LIVE_NOTIFICATION_ID = 4096

    fun showLiveUpdate(
        context: Context,
        payload: CourseReminderPayload
    ): Boolean {
        val courseStartAtMillis = payload.courseStartAtMillis ?: return false
        val remainingDurationMs = courseStartAtMillis - System.currentTimeMillis()
        if (remainingDurationMs <= 0L) return false

        val contentText = buildString {
            append("课程即将开始")
            if (!payload.location.isNullOrBlank()) {
                append(" · ")
                append(payload.location)
            }
        }
        val expandedText = buildString {
            append(contentText)
            if (!payload.timeText.isNullOrBlank()) {
                append(" · ")
                append(payload.timeText)
            }
        }

        val notification = NotificationCompat.Builder(context, CourseReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(payload.courseName)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setAutoCancel(false)
            .setShowWhen(true)
            .setWhen(courseStartAtMillis)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setTimeoutAfter(remainingDurationMs)
            .setRequestPromotedOngoing(true)
            .setContentIntent(buildContentIntent(context, payload.notificationId))
            .build()

        NotificationManagerCompat.from(context).notify(LIVE_NOTIFICATION_ID, notification)
        return true
    }

    fun cancel(context: Context) {
        NotificationManagerCompat.from(context).cancel(LIVE_NOTIFICATION_ID)
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
}
