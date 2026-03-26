package com.ahu.ahutong.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.dao.PreferencesManager
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.schedule.CurrentWeekResolver
import com.ahu.ahutong.notification.model.CourseReminderPayload
import com.ahu.ahutong.ui.state.ScheduleViewModel
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object CourseReminderScheduler {
    internal const val CHANNEL_ID = "course_reminder_v2"

    private const val CHANNEL_NAME = "课前提醒"
    private const val CHANNEL_DESCRIPTION = "上课前 10 分钟提醒下一节课"
    private const val REQUEST_CODE = 2001
    private const val DEBUG_REQUEST_CODE_BASE = 2100
    private const val DEBUG_LIVE_COUNTDOWN_MINUTES = 3L
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
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = CHANNEL_DESCRIPTION
            enableLights(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun reschedule(context: Context) {
        cancelScheduledReminder(context)
        if (!isReminderEnabled(context)) return

        val nextReminder = findNextReminder() ?: return
        val triggerAtMillis = nextReminder.triggerAt
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val pendingIntent = buildPendingIntent(context, nextReminder)
        scheduleAlarm(context, triggerAtMillis, pendingIntent)
    }

    fun cancel(context: Context) {
        cancelScheduledReminder(context)
        CourseReminderNotifier.cancelActiveReminder(context)
    }

    fun scheduleDebugReminder(context: Context, delayMinutes: Int) {
        createNotificationChannel(context)
        val triggerAtMillis = System.currentTimeMillis() + delayMinutes * 10_000L
        val triggerDelaySeconds = delayMinutes * 10
        val payload = CourseReminderPayload(
            courseName = "课前提醒测试",
            location = "预计 $delayMinutes 分钟后触发",
            timeText = "调试通知",
            notificationId = DEBUG_REQUEST_CODE_BASE + delayMinutes,
            allowLiveCountdown = false
        )
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DEBUG_REQUEST_CODE_BASE + delayMinutes,
            Intent(context, CourseReminderReceiver::class.java).apply {
                action = CourseReminderReceiver.ACTION_REMIND
                payload.writeToIntent(this)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        scheduleAlarm(context, triggerAtMillis, pendingIntent)
    }

    fun scheduleDebugLiveUpdateReminder(context: Context, delayMinutes: Int) {
        createNotificationChannel(context)
        val triggerAtMillis = System.currentTimeMillis() + delayMinutes * 10_000L
        val payload = CourseReminderPayload(
            courseName = "课前岛卡测试",
            location = "调试入口",
            timeText = "1 分钟后开始",
            courseStartAtMillis = triggerAtMillis + 60_000L,
            notificationId = DEBUG_REQUEST_CODE_BASE + 100 + delayMinutes,
            allowLiveCountdown = true
        )
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DEBUG_REQUEST_CODE_BASE + 100 + delayMinutes,
            Intent(context, CourseReminderReceiver::class.java).apply {
                action = CourseReminderReceiver.ACTION_REMIND
                payload.writeToIntent(this)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        scheduleAlarm(context, triggerAtMillis, pendingIntent)
    }

    private fun cancelScheduledReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                Intent(context, CourseReminderReceiver::class.java).setAction(
                    CourseReminderReceiver.ACTION_REMIND
                ),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
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
        val courseStartAtMillis = reminder.triggerAt
            .plusMinutes(REMINDER_MINUTES)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val payload = CourseReminderPayload(
            courseName = reminder.course.name,
            location = reminder.course.location,
            timeText = timeText,
            courseStartAtMillis = courseStartAtMillis,
            notificationId = notificationId,
            allowLiveCountdown = true
        )

        val intent = Intent(context, CourseReminderReceiver::class.java).apply {
            action = CourseReminderReceiver.ACTION_REMIND
            payload.writeToIntent(this)
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
}
