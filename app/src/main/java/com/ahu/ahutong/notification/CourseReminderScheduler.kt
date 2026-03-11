package com.ahu.ahutong.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
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
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.dao.PreferencesManager
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.schedule.CurrentWeekResolver
import com.ahu.ahutong.ui.state.ScheduleViewModel
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object CourseReminderScheduler {
    private const val CHANNEL_ID = "course_reminder"
    private const val CHANNEL_NAME = "课前提醒"
    private const val REQUEST_CODE = 2001
    private const val DEBUG_REQUEST_CODE_BASE = 2100
    private const val EXTRA_COURSE_NAME = "extra_course_name"
    private const val EXTRA_LOCATION = "extra_location"
    private const val EXTRA_TIME_TEXT = "extra_time_text"
    private const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    private const val SCAN_DAYS = 21L
    private const val REMINDER_MINUTES = 10L

    data class ReminderCandidate(
        val triggerAt: LocalDateTime,
        val course: Course
    )

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "上课前 10 分钟提醒"
        }
        manager.createNotificationChannel(channel)
    }

    fun reschedule(context: Context) {
        cancel(context)
        if (!isReminderEnabled(context)) return

        val nextReminder = findNextReminder() ?: return
        val triggerAtMillis = nextReminder.triggerAt
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val pendingIntent = buildPendingIntent(context, nextReminder)
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

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                Intent(context, CourseReminderReceiver::class.java).setAction(CourseReminderReceiver.ACTION_REMIND),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    fun scheduleDebugReminder(context: Context, delayMinutes: Int) {
        createNotificationChannel(context)
        val triggerAtMillis = System.currentTimeMillis() + delayMinutes * 60_000L
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DEBUG_REQUEST_CODE_BASE + delayMinutes,
            Intent(context, CourseReminderReceiver::class.java).apply {
                action = CourseReminderReceiver.ACTION_REMIND
                putExtra(EXTRA_COURSE_NAME, "课前提醒测试")
                putExtra(EXTRA_LOCATION, "预计 $delayMinutes 分钟后触发")
                putExtra(EXTRA_TIME_TEXT, "测试通知")
                putExtra(EXTRA_NOTIFICATION_ID, DEBUG_REQUEST_CODE_BASE + delayMinutes)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
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

    fun notifyReminder(
        context: Context,
        courseName: String,
        location: String?,
        timeText: String?,
        notificationId: Int
    ) {
        createNotificationChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val openIntent = Intent(context, MainActivity::class.java)
        val contentIntent = TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(openIntent)
            .getPendingIntent(notificationId, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val contentText = buildString {
            append("10 分钟后上课")
            if (!timeText.isNullOrBlank()) append(" · $timeText")
            if (!location.isNullOrBlank()) append(" · $location")
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(courseName)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun isReminderEnabled(context: Context): Boolean {
        return runBlocking {
            PreferencesManager(context).courseReminderEnabled.first()
        }
    }

    private fun buildPendingIntent(
        context: Context,
        reminder: ReminderCandidate
    ): PendingIntent {
        val startRange = ScheduleViewModel.getCourseTimeRangeInMinutes(reminder.course)
        val startMinutes = startRange.first
        val startTimeText = "%02d:%02d".format(startMinutes / 60, startMinutes % 60)
        val sections = "${reminder.course.startTime}-${reminder.course.startTime + reminder.course.length - 1}节"
        val timeText = "$sections $startTimeText"
        val notificationId = reminder.triggerAt.hashCode()

        val intent = Intent(context, CourseReminderReceiver::class.java).apply {
            action = CourseReminderReceiver.ACTION_REMIND
            putExtra(EXTRA_COURSE_NAME, reminder.course.name)
            putExtra(EXTRA_LOCATION, reminder.course.location)
            putExtra(EXTRA_TIME_TEXT, timeText)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun findNextReminder(now: LocalDateTime = LocalDateTime.now()): ReminderCandidate? {
        val semesterKey = CurrentWeekResolver.getCachedSemesterKey() ?: return null
        val startTime = AHUCache.getSchoolTermStartTime(
            semesterKey.schoolYear,
            semesterKey.schoolTerm
        ) ?: return null
        val termStartDate = runCatching { LocalDate.parse(startTime) }.getOrNull() ?: return null
        val schedule = AHUCache.getSchedule(semesterKey.raw).orEmpty()
        if (schedule.isEmpty()) return null

        var best: ReminderCandidate? = null
        for (offset in 0..SCAN_DAYS) {
            val targetDate = now.toLocalDate().plusDays(offset)
            val weekIndex = calculateWeekIndex(termStartDate, targetDate)
            if (weekIndex < 1) continue
            schedule.forEach { course ->
                if (!isCourseActiveOnDate(course, targetDate, weekIndex)) return@forEach
                val courseStart = targetDate.atTime(courseStartLocalTime(course))
                val reminderAt = courseStart.minusMinutes(REMINDER_MINUTES)
                if (!reminderAt.isAfter(now)) return@forEach
                val candidate = ReminderCandidate(reminderAt, course)
                if (best == null || candidate.triggerAt.isBefore(best!!.triggerAt)) {
                    best = candidate
                }
            }
            if (best != null && Duration.between(now, best!!.triggerAt).toDays() < offset) {
                break
            }
        }
        return best
    }

    private fun calculateWeekIndex(termStartDate: LocalDate, targetDate: LocalDate): Int {
        val days = Duration.between(
            termStartDate.atStartOfDay(),
            targetDate.atStartOfDay()
        ).toDays()
        return (days / 7L).toInt() + 1
    }

    private fun isCourseActiveOnDate(course: Course, date: LocalDate, weekIndex: Int): Boolean {
        return date.dayOfWeek.value == course.weekday && weekIndex in course.weekIndexes
    }

    private fun courseStartLocalTime(course: Course): LocalTime {
        val range = ScheduleViewModel.timetable.getValue(course.startTime)
        return LocalTime.parse(range.substringBefore("-"))
    }

    fun extractCourseName(intent: Intent): String? = intent.getStringExtra(EXTRA_COURSE_NAME)

    fun extractLocation(intent: Intent): String? = intent.getStringExtra(EXTRA_LOCATION)

    fun extractTimeText(intent: Intent): String? = intent.getStringExtra(EXTRA_TIME_TEXT)

    fun extractNotificationId(intent: Intent): Int = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 1001)
}
