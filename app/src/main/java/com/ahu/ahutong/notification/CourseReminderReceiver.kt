package com.ahu.ahutong.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ahu.ahutong.notification.model.CourseReminderPayload

class CourseReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_REMIND) return
        val payload = CourseReminderPayload.fromIntent(intent) ?: return
        CourseReminderNotifier.showReminder(context, payload)
        CourseReminderScheduler.reschedule(context)
    }

    companion object {
        const val ACTION_REMIND = "com.ahu.ahutong.notification.ACTION_REMIND_COURSE"
    }
}
