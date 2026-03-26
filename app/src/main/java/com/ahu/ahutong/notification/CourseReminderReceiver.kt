package com.ahu.ahutong.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ahu.ahutong.notification.model.CourseReminderPayload

class CourseReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val payload = CourseReminderPayload.fromIntent(intent) ?: return
        when (intent.action) {
            ACTION_REMIND -> {
                val liveUpdateShown = CourseReminderNotifier.showReminder(context, payload)
                if (liveUpdateShown) {
                    CourseLiveUpdateHelper.scheduleNextUpdate(context, payload)
                }
                CourseReminderScheduler.reschedule(context)
            }

            ACTION_UPDATE_LIVE_COUNTDOWN -> {
                if (!CourseReminderCapability.shouldTryLiveCountdown(context, payload)) {
                    CourseReminderNotifier.cancelActiveReminder(context)
                    return
                }

                val liveUpdateShown = CourseLiveUpdateHelper.showLiveUpdate(context, payload)
                if (liveUpdateShown) {
                    CourseLiveUpdateHelper.scheduleNextUpdate(context, payload)
                } else {
                    CourseReminderNotifier.cancelActiveReminder(context)
                }
            }
        }
    }

    companion object {
        const val ACTION_REMIND = "com.ahu.ahutong.notification.ACTION_REMIND_COURSE"
        const val ACTION_UPDATE_LIVE_COUNTDOWN =
            "com.ahu.ahutong.notification.ACTION_UPDATE_LIVE_COUNTDOWN"
    }
}
