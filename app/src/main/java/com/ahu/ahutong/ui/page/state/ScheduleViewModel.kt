package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arch.sink.utils.TimeUtils
import com.ahu.ahutong.common.SingleLiveEvent
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.ui.widget.schedule.bean.DefaultDataUtils
import com.ahu.ahutong.ui.widget.schedule.bean.ScheduleConfigBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * @Author SinkDev
 * @Date 2021/7/27-19:16
 * @Email 468766131@qq.com
 */
class ScheduleViewModel : ViewModel() {
    val timetable by lazy {
        mapOf(
            1 to "08:20-09:05",
            2 to "09:15-10:00",
            3 to "10:20-11:05",
            4 to "11:15-12:00",
            5 to "14:00-14:45",
            6 to "14:55-15:40",
            7 to "15:50-16:35",
            8 to "16:45-17:30",
            9 to "19:00-19:45",
            10 to "19:55-20:40",
            11 to "20:50-21:35"
        )
    }

    val schedule = MutableLiveData<Result<List<Course>>>()

    val showSelectTimeDialog = SingleLiveEvent<Boolean>()

    val schoolYear: String
        get() = AHUCache.getSchoolYear() ?: "2022-2023"

    val schoolTerm: String
        get() = AHUCache.getSchoolTerm() ?: "1"

    val scheduleConfig = MutableLiveData<ScheduleConfigBean>()

    /**
     * @param from "HH:mm-HH:mm"
     * @param to "HH:mm-HH:mm"
     */
    private fun getTimeRangeInMinutes(
        from: String,
        to: String = from
    ): IntRange {
        val format = SimpleDateFormat("HH:mm", Locale.CHINA)
        val start = format.parse(from.take(5)).let {
            val calendar = Calendar.getInstance(Locale.CHINA)
            calendar.time = it!!
            calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        }
        val end = format.parse(to.takeLast(5)).let {
            val calendar = Calendar.getInstance(Locale.CHINA)
            calendar.time = it!!
            calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        }
        return start..end
    }

    fun getCourseTimeRangeInMinutes(course: Course): IntRange {
        return getTimeRangeInMinutes(
            from = timetable.getValue(course.startTime),
            to = timetable.getValue(course.startTime + course.length - 1)
        )
    }

    fun findCurrentTimeByMinutes(minutes: Int): Int? {
        return timetable.toList().find {
            minutes <= getTimeRangeInMinutes(it.second).first
        }?.first
    }

    // 更新周
    fun changeWeek(week: Int) {
        val configBean = scheduleConfig.value!!
        configBean.week = week
        scheduleConfig.value = configBean
    }

    /**
     * 刷新课表
     * @param schoolYear String
     * @param schoolTerm String
     * @param isRefresh Boolean
     */
    fun refreshSchedule(
        schoolYear: String = this.schoolYear,
        schoolTerm: String = this.schoolTerm,
        isRefresh: Boolean = false
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            if (!AHUCache.isLogin()) {
                schedule.value = Result.failure(Throwable("请先登录！"))
                return@launch
            }
            val result = AHURepository.getSchedule(schoolYear, schoolTerm, isRefresh)
            schedule.value = result
        }
    }

    fun loadConfig() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                var time = AHUCache.getSchoolTermStartTime(schoolYear, schoolTerm)
                if (time == null) {
                    showSelectTimeDialog.callFromOtherThread()
                    time = "2022-2-21"
                }
                // 创建课程表配置
                val scheduleConfigBean = ScheduleConfigBean()
                // 课程表主题
                scheduleConfigBean.theme =
                    AHUCache.getScheduleTheme() ?: DefaultDataUtils.getDefaultTheme()
                // 是否显示全部课程
                scheduleConfigBean.isShowAll = AHUCache.isShowAllCourse()
                // 根据开学时间， 获取当前周数
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
                    .parse(time)
                // 开学时间
                scheduleConfigBean.startTime = date
                // 当前周数
                scheduleConfigBean.week = (TimeUtils.getTimeDistance(Date(), date) / 7 + 1).toInt()
                // 当前周几
                scheduleConfigBean.weekDay = (Calendar.getInstance(Locale.CHINA)[Calendar.DAY_OF_WEEK] - 1)
                    .takeIf { it != 0 } ?: 7
                scheduleConfigBean
            }.let {
                scheduleConfig.postValue(it)
            }
        }
    }

    /**
     * 保存时间
     * @param schoolYear String
     * @param schoolTerm String
     * @param week Int
     */
    fun saveTime(schoolYear: String, schoolTerm: String, week: Int) {
        AHUCache.saveSchoolYear(schoolYear)
        AHUCache.saveSchoolTerm(schoolTerm)
        // 推算开学日期
        val instance = Calendar.getInstance(Locale.CHINA)
        instance.add(Calendar.DATE, (week - 1) * -7)
        instance.firstDayOfWeek = Calendar.MONDAY
        instance.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        // 修改当前的开学时间和周数
        scheduleConfig.value?.startTime = instance.time
        changeWeek(week)
        AHUCache.saveSchoolTermStartTime(
            schoolYear,
            schoolTerm,
            SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(instance.time)
        )
    }
}
