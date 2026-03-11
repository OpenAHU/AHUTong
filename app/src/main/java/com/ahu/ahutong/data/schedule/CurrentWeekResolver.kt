package com.ahu.ahutong.data.schedule

import com.ahu.ahutong.data.crawler.api.jwxt.JwxtApi
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.debug.DebugClock
import com.ahu.ahutong.data.model.ScheduleConfigBean
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale

object CurrentWeekResolver {

    enum class Source {
        LOCAL,
        REMOTE,
        DEFAULT
    }

    data class SemesterKey(
        val raw: String,
        val schoolYear: String,
        val schoolTerm: String
    )

    data class ResolvedConfig(
        val config: ScheduleConfigBean,
        val source: Source
    )

    fun buildSemesterKey(schoolYear: String, schoolTerm: String): String {
        return "${schoolYear.trim()}-${schoolTerm.trim()}"
    }

    fun parseSemesterKey(raw: String?): SemesterKey? {
        if (raw.isNullOrBlank()) {
            return null
        }
        val normalized = raw.trim()
        val parts = normalized.split("-")
        return when {
            parts.size == 3 -> SemesterKey(
                raw = normalized,
                schoolYear = "${parts[0]}-${parts[1]}",
                schoolTerm = parts[2]
            )

            parts.size == 1 && AHUCache.getSchoolYear() != null -> {
                val schoolYear = AHUCache.getSchoolYear().orEmpty()
                SemesterKey(
                    raw = buildSemesterKey(schoolYear, normalized),
                    schoolYear = schoolYear,
                    schoolTerm = normalized
                )
            }

            else -> null
        }
    }

    fun getCachedSemesterKey(): SemesterKey? {
        return parseSemesterKey(AHUCache.getSchoolTerm())
    }

    fun getCurrentWeekDay(calendar: Calendar = DebugClock.nowCalendar(Locale.CHINA)): Int {
        return (calendar[Calendar.DAY_OF_WEEK] - 1).takeIf { it != 0 } ?: 7
    }

    fun resolveLocalConfig(now: LocalDate = DebugClock.nowLocalDate()): ResolvedConfig? {
        val semesterKey = getCachedSemesterKey() ?: return null
        val startTime = AHUCache.getSchoolTermStartTime(
            semesterKey.schoolYear,
            semesterKey.schoolTerm
        ) ?: return null
        val startDate = runCatching { LocalDate.parse(startTime) }.getOrNull() ?: return null
        val days = ChronoUnit.DAYS.between(startDate, now)
        val week = (days.coerceAtLeast(0) / 7L).toInt() + 1
        return ResolvedConfig(
            config = buildScheduleConfig(
                week = week,
                weekDay = now.dayOfWeek.value,
                startDate = startDate
            ),
            source = Source.LOCAL
        )
    }

    suspend fun resolveLocalFirst(now: LocalDate = DebugClock.nowLocalDate()): ResolvedConfig {
        if (DebugClock.isMocked()) {
            return resolveLocalConfig(now) ?: defaultConfig(now)
        }
        return resolveLocalConfig(now)
            ?: runCatching { syncRemoteConfig(now) }.getOrNull()
            ?: defaultConfig(now)
    }

    suspend fun syncRemoteConfig(now: LocalDate = DebugClock.nowLocalDate()): ResolvedConfig? {
        if (DebugClock.isMocked()) {
            return null
        }
        val response = JwxtApi.API.getCurrentTeachWeek()
        val semesterKey = parseSemesterKey(response.currentSemester)

        semesterKey?.let {
            AHUCache.saveSchoolYear(it.schoolYear)
            AHUCache.saveSchoolTerm(it.raw)
        } ?: AHUCache.saveSchoolTerm(response.currentSemester)

        val weekDay = now.dayOfWeek.value
        val mondayOfCurrentWeek = now.minusDays((weekDay - 1).toLong())
        val currentWeek = response.weekIndex.coerceAtLeast(1)
        val startDate = mondayOfCurrentWeek.minusDays((currentWeek - 1).toLong() * 7L)

        semesterKey?.let {
            AHUCache.saveSchoolTermStartTime(
                it.schoolYear,
                it.schoolTerm,
                startDate.toString()
            )
        }

        return ResolvedConfig(
            config = buildScheduleConfig(
                week = currentWeek,
                weekDay = weekDay,
                startDate = startDate
            ),
            source = Source.REMOTE
        )
    }

    fun defaultConfig(now: LocalDate = DebugClock.nowLocalDate()): ResolvedConfig {
        val weekDay = now.dayOfWeek.value
        val mondayOfCurrentWeek = now.minusDays((weekDay - 1).toLong())
        return ResolvedConfig(
            config = buildScheduleConfig(
                week = 1,
                weekDay = weekDay,
                startDate = mondayOfCurrentWeek
            ),
            source = Source.DEFAULT
        )
    }

    private fun buildScheduleConfig(
        week: Int,
        weekDay: Int,
        startDate: LocalDate
    ): ScheduleConfigBean {
        return ScheduleConfigBean().apply {
            this.week = week
            this.weekDay = weekDay
            this.startTime = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
            this.isShowAll = AHUCache.isShowAllCourse()
        }
    }
}
