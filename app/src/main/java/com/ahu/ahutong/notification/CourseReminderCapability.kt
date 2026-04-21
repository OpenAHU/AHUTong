package com.ahu.ahutong.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.ahu.ahutong.data.dao.PreferencesManager
import com.ahu.ahutong.notification.model.CourseReminderPayload
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object CourseReminderCapability {
    private const val ANDROID_16_API = 36

    fun isAndroid16Plus(): Boolean = Build.VERSION.SDK_INT >= ANDROID_16_API

    fun isLiveCountdownEnabled(context: Context): Boolean = runBlocking {
        PreferencesManager(context).courseReminderLiveCountdownEnabled.first()
    }

    fun canUsePromotedNotifications(context: Context): Boolean {
        if (!isAndroid16Plus()) return false
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) return false
        return notificationManager.canPostPromotedNotifications()
    }

    fun shouldTryLiveCountdown(
        context: Context,
        payload: CourseReminderPayload
    ): Boolean {
        if (!payload.supportsLiveCountdown) return false
        if (!isLiveCountdownEnabled(context)) return false
        return canUsePromotedNotifications(context)
    }

    fun createPromotionSettingsIntent(context: Context) =
        android.content.Intent(Settings.ACTION_APP_NOTIFICATION_PROMOTION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }

    fun createNotificationSettingsIntent(context: Context) =
        android.content.Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
}
