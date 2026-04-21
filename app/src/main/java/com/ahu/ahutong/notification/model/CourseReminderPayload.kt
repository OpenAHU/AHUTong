package com.ahu.ahutong.notification.model

import android.content.Intent

data class CourseReminderPayload(
    val courseName: String,
    val location: String?,
    val timeText: String?,
    val courseStartAtMillis: Long? = null,
    val notificationId: Int = 1001,
    val allowLiveCountdown: Boolean = false
) {
    val supportsLiveCountdown: Boolean
        get() = allowLiveCountdown && courseStartAtMillis != null

    fun writeToIntent(intent: Intent): Intent = intent.apply {
        putExtra(EXTRA_COURSE_NAME, courseName)
        putExtra(EXTRA_LOCATION, location)
        putExtra(EXTRA_TIME_TEXT, timeText)
        putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        courseStartAtMillis?.let { putExtra(EXTRA_COURSE_START_AT_MILLIS, it) }
        putExtra(EXTRA_ALLOW_LIVE_COUNTDOWN, allowLiveCountdown)
    }

    companion object {
        private const val EXTRA_COURSE_NAME = "extra_course_name"
        private const val EXTRA_LOCATION = "extra_location"
        private const val EXTRA_TIME_TEXT = "extra_time_text"
        private const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        private const val EXTRA_COURSE_START_AT_MILLIS = "extra_course_start_at_millis"
        private const val EXTRA_ALLOW_LIVE_COUNTDOWN = "extra_allow_live_countdown"

        fun fromIntent(intent: Intent): CourseReminderPayload? {
            val courseName = intent.getStringExtra(EXTRA_COURSE_NAME)?.takeIf { it.isNotBlank() }
                ?: return null
            val courseStartAtMillis = intent
                .takeIf { it.hasExtra(EXTRA_COURSE_START_AT_MILLIS) }
                ?.getLongExtra(EXTRA_COURSE_START_AT_MILLIS, -1L)
                ?.takeIf { it > 0L }

            return CourseReminderPayload(
                courseName = courseName,
                location = intent.getStringExtra(EXTRA_LOCATION),
                timeText = intent.getStringExtra(EXTRA_TIME_TEXT),
                courseStartAtMillis = courseStartAtMillis,
                notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 1001),
                allowLiveCountdown = intent.getBooleanExtra(EXTRA_ALLOW_LIVE_COUNTDOWN, false)
            )
        }
    }
}
