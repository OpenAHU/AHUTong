package com.ahu.ahutong.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CourseReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_REMIND) return
        CourseReminderScheduler.notifyReminder(
            context = context,
            courseName = CourseReminderScheduler.extractCourseName(intent).orEmpty(),
            location = CourseReminderScheduler.extractLocation(intent),
            timeText = CourseReminderScheduler.extractTimeText(intent),
            notificationId = CourseReminderScheduler.extractNotificationId(intent)
        )
        CourseReminderScheduler.reschedule(context)
    }

    companion object {
        const val ACTION_REMIND = "com.ahu.ahutong.notification.ACTION_REMIND_COURSE"
    }
}
