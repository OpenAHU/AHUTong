package com.ahu.ahutong.appwidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.glance.appwidget.updateAll
import com.ahu.ahutong.data.debug.DebugClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId

import android.os.Handler
import android.os.Looper

class WidgetUpdateScheduler : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_UPDATE_WIDGETS) {
            Log.e(TAG, "onReceive: Triggering widget update (Test Mode)")
            
            // 1. Update Glance Widget
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    ScheduleAppWidget().updateAll(context)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to update Glance widget", e)
                }
            }

            // 2. Update Adaptive Widget
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, ScheduleAdaptiveWidgetProvider::class.java)
            )
            if (ids.isNotEmpty()) {
                val updateIntent = Intent(context, ScheduleAdaptiveWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(updateIntent)
            }

            // 3. Schedule next update
            scheduleNext(context)
        }
    }

    companion object {
        private const val TAG = "WidgetUpdateScheduler"
        const val ACTION_UPDATE_WIDGETS = "com.ahu.ahutong.appwidget.ACTION_UPDATE_WIDGETS"
        private const val REQUEST_CODE = 3001

        fun scheduleNext(context: Context) {
            val now = LocalDateTime.now()
            val nextTrigger = calculateNextTrigger(now)
            
            val triggerMillis = nextTrigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val delayMillis = triggerMillis - System.currentTimeMillis()
            Log.i(TAG, "scheduleNext: Scheduling next update at $nextTrigger (delay: ${delayMillis}ms)")

            // 测试模式：如果延时小于 10 秒，直接使用 Handler，绕过 AlarmManager 的限制
//            if (delayMillis < 10000) {
//                Handler(Looper.getMainLooper()).postDelayed({
//                    val intent = Intent(context, WidgetUpdateScheduler::class.java).apply {
//                        action = ACTION_UPDATE_WIDGETS
//                    }
//                    context.sendBroadcast(intent)
//                }, delayMillis.coerceAtLeast(0))
//                return
//            }

            val intent = Intent(context, WidgetUpdateScheduler::class.java).apply {
                action = ACTION_UPDATE_WIDGETS
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                 // Fallback or request permission handled elsewhere, just try best effort
                 alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            } else {
                 alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        }

        fun calculateNextTrigger(now: LocalDateTime): LocalDateTime {
            // Strategy:
            // 1. 01:00
            // 2. 07:30 to 22:00 every 30 minutes
            
            val today = now.toLocalDate()
            val tomorrow = today.plusDays(1)

            val candidates = mutableListOf<LocalDateTime>()

            // Add 01:00 today
            candidates.add(today.atTime(1, 0))

            // Add 07:30 to 22:00 today
            var t = today.atTime(7, 30)
            val endTime = today.atTime(22, 0)
            while (!t.isAfter(endTime)) {
                candidates.add(t)
                t = t.plusMinutes(30)
            }

            // Add 01:00 tomorrow (as a fallback if all today's passed)
            candidates.add(tomorrow.atTime(1, 0))
            
            // Also add tomorrow's start sequence just in case (e.g. now is 23:00)
             var tNext = tomorrow.atTime(7, 30)
             val endTimeNext = tomorrow.atTime(22, 0)
             while (!tNext.isAfter(endTimeNext)) {
                 candidates.add(tNext)
                 tNext = tNext.plusMinutes(30)
             }

            // Find first candidate strictly after now
            return candidates.firstOrNull { it.isAfter(now) } ?: tomorrow.atTime(1, 0)

        }
        
        fun cancel(context: Context) {
             val intent = Intent(context, WidgetUpdateScheduler::class.java).apply {
                action = ACTION_UPDATE_WIDGETS
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.cancel(pendingIntent)
        }
    }
}
