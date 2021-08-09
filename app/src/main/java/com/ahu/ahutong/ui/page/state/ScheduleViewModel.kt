package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import arch.sink.utils.TimeUtils
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.Course
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.SimpleFormatter

/**
 * @Author SinkDev
 * @Date 2021/7/27-19:16
 * @Email 468766131@qq.com
 */
class ScheduleViewModel : ViewModel() {

    val schedule = MutableLiveData<Result<List<Course>>>()

    val isShowAllCourse:  MutableLiveData<Boolean> by lazy {
        MutableLiveData(AHUCache.isShowAllCourse())
    }

    var schoolYear: String
        get() = AHUCache.getSchoolYear() ?: ""
        private set(value) = AHUCache.saveSchoolYear(value)

    var schoolTerm: String
        get() = AHUCache.getSchoolTerm() ?: ""
        private set(value) = AHUCache.saveSchoolTerm(value)


    val week: MutableLiveData<Int> by lazy {
        // 获取开学时间
        val time = AHUCache.getSchoolTermStartTime(schoolYear, schoolTerm)
        if (time == null){
            return@lazy MutableLiveData(0)
        }
        //根据开学时间， 获取当前周数
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
            .parse(time)
        MutableLiveData((TimeUtils.getTimeDistance(Date(), date) / 7 + 1).toInt())
    }


    //开学时间
    val startTime: MutableLiveData<Date> by lazy {
        val time = AHUCache.getSchoolTermStartTime(schoolYear, schoolTerm)
        if (time == null){
            return@lazy MutableLiveData(SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
                .parse("2021-08-02"))
        }
        MutableLiveData(SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
            .parse(time))
    }


    /**
     * 加载课表
     * @param schoolYear String
     * @param schoolTerm String
     */
    fun loadSchedule(schoolYear: String = this.schoolYear, schoolTerm: String = this.schoolTerm){
        schedule.switchMap {
            AHURepository.getSchedule(schoolYear, schoolTerm)
        }
    }

    /**
     * 刷新课表
     * @param schoolYear String
     * @param schoolTerm String
     */
    fun refreshSchedule(schoolYear: String = this.schoolYear, schoolTerm: String = this.schoolTerm) {
        schedule.switchMap {
            AHURepository.getSchedule(schoolYear, schoolTerm, true)
        }
    }

    /**
     * 保存时间
     * @param schoolYear String
     * @param schoolTerm String
     * @param week Int
     */
    fun saveTime(schoolYear: String, schoolTerm: String, week: Int){
        this.schoolTerm = schoolTerm
        this.schoolYear = schoolYear
        this.week.value = week
        //推算开学日期
        val instance = Calendar.getInstance(Locale.CHINA)
        instance.add(Calendar.DATE, (week-1) * -7)
        instance.firstDayOfWeek = Calendar.MONDAY
        instance.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        startTime.value = instance.time
        AHUCache.saveSchoolTermStartTime(schoolYear, schoolTerm,
            SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(instance.time))
    }

}