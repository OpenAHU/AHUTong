package com.ahu.ahutong.data.debug

import com.ahu.ahutong.data.dao.AHUCache
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DebugClock {
    private val zoneId: ZoneId
        get() = ZoneId.systemDefault()

    private fun currentInstant(): Instant {
        val mocked = AHUCache.getMockCurrentTimeMillis()
        return if (mocked != null) Instant.ofEpochMilli(mocked) else Instant.now()
    }

    fun isMocked(): Boolean = AHUCache.getMockCurrentTimeMillis() != null

    fun nowDate(): Date = Date.from(currentInstant())

    fun nowLocalDate(): LocalDate = LocalDateTime.ofInstant(currentInstant(), zoneId).toLocalDate()

    fun nowLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(currentInstant(), zoneId)

    fun nowCalendar(locale: Locale = Locale.CHINA): Calendar {
        return Calendar.getInstance(locale).apply {
            time = nowDate()
        }
    }

    fun currentMinutes(locale: Locale = Locale.CHINA): Int {
        val calendar = nowCalendar(locale)
        return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
    }
}
