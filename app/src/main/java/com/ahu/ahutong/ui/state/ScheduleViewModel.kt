package com.ahu.ahutong.ui.state

import android.graphics.Color
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arch.sink.utils.TimeUtils
import com.ahu.ahutong.common.SingleLiveEvent
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.crawler.api.jwxt.JwxtApi
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.dao.PreferencesManager
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.ScheduleConfigBean
import com.ahu.ahutong.ext.GlobalCoroutineExceptionHandler
import com.ahu.ahutong.ext.launchSafe
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

/**
 * @Author SinkDev
 * @Date 2021/7/27-19:16
 * @Email 468766131@qq.com
 */
class ScheduleViewModel () : ViewModel() {
    val TAG = "ScheduleViewModel"
    val schedule = MutableLiveData<Result<List<Course>>>()

    val showSelectTimeDialog = SingleLiveEvent<Boolean>()

    val schoolYear: String
        get() = AHUCache.getSchoolYear() ?: "2022-2023"

    val schoolTerm: String
        get() = AHUCache.getSchoolTerm() ?: "1"

    val scheduleConfig = MutableLiveData<ScheduleConfigBean>()

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
        viewModelScope.launchSafe {
            withContext(Dispatchers.Main){
                if (!AHUCache.isLogin()) {
                    schedule.value = Result.failure(Throwable("请先登录！"))
                    return@withContext
                }

                val result = AHURepository.getSchedule(isRefresh = false)
                schedule.value = result
            }

        }
    }

    fun loadConfig() {
        viewModelScope.launchSafe {
            withContext(Dispatchers.IO) {
//                var time = AHUCache.getSchoolTermStartTime(schoolYear, schoolTerm)
//                if (time == null) {
//                    showSelectTimeDialog.callFromOtherThread()
//                    time = "2022-2-21"
//                }
                // 创建课程表配置
                val scheduleConfigBean = ScheduleConfigBean()
                // 是否显示全部课程
                scheduleConfigBean.isShowAll = AHUCache.isShowAllCourse()
                // 根据开学时间， 获取当前周数
//                val date = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
//                    .parse(time)
                // 开学时间
//                scheduleConfigBean.startTime = date
                // 当前周数

                val scheduleConfigResult = JwxtApi.API.getCurrentTeachWeek()

                AHUCache.saveSchoolTerm(scheduleConfigResult.currentSemester)

                scheduleConfigBean.week = scheduleConfigResult.weekIndex
                // 当前周几
                scheduleConfigBean.weekDay = (Calendar.getInstance(Locale.CHINA)[Calendar.DAY_OF_WEEK] - 1)
                    .takeIf { it != 0 } ?: 7

                // 计算startTime
                val currentDate = LocalDate.now()
                val pastDate = currentDate.plusDays(scheduleConfigResult.dayIndex.toLong())
                val startDate: Date = Date.from(pastDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                scheduleConfigBean.startTime = startDate

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

    companion object {
        val timetable by lazy {
            mapOf(
                1 to "08:00-08:45",
                2 to "08:50-09:35",
                3 to "09:50-10:35",
                4 to "10:40-11:25",
                5 to "11:30-12:15",
                6 to "14:00-14:45",
                7 to "14:50-15:35",
                8 to "15:50-16:35",
                9 to "16:40-17:25",
                10 to "17:30-18:15",
                11 to "19:00-19:45",
                12 to "19:50-20:35",
                13 to "20:40-21:25"
            )
        }

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
    }
}
