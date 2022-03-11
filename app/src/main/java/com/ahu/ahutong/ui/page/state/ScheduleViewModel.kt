package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.*
import arch.sink.utils.TimeUtils
import com.ahu.ahutong.common.SingleLiveEvent
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.ui.widget.schedule.bean.DefaultDataUtils
import com.ahu.ahutong.ui.widget.schedule.bean.ScheduleConfigBean
import com.ahu.ahutong.ui.widget.schedule.bean.ScheduleTheme
import com.ahu.ahutong.ui.widget.schedule.bean.SimpleTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * @Author SinkDev
 * @Date 2021/7/27-19:16
 * @Email 468766131@qq.com
 */
class ScheduleViewModel : ViewModel() {

    val schedule = MutableLiveData<Result<List<Course>>>()

    val showSelectTimeDialog = SingleLiveEvent<Boolean>()

    val schoolYear: String
        get() = AHUCache.getSchoolYear() ?: ""


    val schoolTerm: String
        get() = AHUCache.getSchoolTerm() ?: ""

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
            scheduleConfig.value = async {
                var time = AHUCache.getSchoolTermStartTime(schoolYear, schoolTerm)
                if (time == null) {
                    showSelectTimeDialog.call()
                    time = "2022-2-21"
                }
                // 创建课程表配置
                val scheduleConfigBean = ScheduleConfigBean()
                // 课程表主题
                scheduleConfigBean.theme =
                    AHUCache.getScheduleTheme() ?: DefaultDataUtils.getDefaultTheme()
                // 是否显示全部课程
                scheduleConfigBean.isShowAll = AHUCache.isShowAllCourse()
                //根据开学时间， 获取当前周数
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
                    .parse(time)
                // 开学时间
                scheduleConfigBean.startTime = date
                // 当前周数
                scheduleConfigBean.week = (TimeUtils.getTimeDistance(Date(), date) / 7 + 1).toInt()
                // 当前周几
                scheduleConfigBean.weekDay =
                    Calendar.getInstance(Locale.CHINA)[Calendar.DAY_OF_WEEK]
                scheduleConfigBean
            }.await()
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
        //推算开学日期
        val instance = Calendar.getInstance(Locale.CHINA)
        instance.add(Calendar.DATE, (week - 1) * -7)
        instance.firstDayOfWeek = Calendar.MONDAY
        instance.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        // 修改当前的开学时间和周数
        scheduleConfig.value?.startTime = instance.time
        changeWeek(week)
        AHUCache.saveSchoolTermStartTime(
            schoolYear, schoolTerm,
            SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(instance.time)
        )
    }

}