package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arch.sink.utils.TimeUtils
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.Room
import com.sink.library.log.SinkLog
import kotlinx.coroutines.launch
import okio.Sink
import java.text.SimpleDateFormat
import java.util.*

class ClassRoomViewModel : ViewModel() {
    val campus = MutableLiveData("磬苑校区")
    val time = MutableLiveData("1,2节")
    val rooms = MutableLiveData<Result<List<Room>>>()

    fun searchEmptyRoom(currentCampus: String? , time: String?) = viewModelScope.launch{
        if (currentCampus == null || time == null){
            rooms.value = Result.failure(Throwable("必须选择校区和时间。"))
            return@launch
        }
        val schoolTerm = AHUCache.getSchoolTerm()
        val schoolYear = AHUCache.getSchoolYear()
        if (schoolYear == null || schoolTerm == null){
            rooms.value = Result.failure(Throwable("未填写当前学年，学期。"))
            return@launch
        }
        // 获取开学时间
        val startTime = AHUCache.getSchoolTermStartTime(schoolYear, schoolTerm)
        if (startTime == null) {
            rooms.value = Result.failure(Throwable("开学时间未知"))
            return@launch
        }
        //根据开学时间， 获取当前周数
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
            .parse(startTime)
        val week = (TimeUtils.getTimeDistance(Date(), date) / 7 + 1).toInt()
        val instance = Calendar.getInstance(Locale.CHINA)
        instance.firstDayOfWeek = Calendar.MONDAY
        //获取空教室信息
        rooms.value = AHURepository.getEmptyRoom(
            (campuses.indexOf(currentCampus) + 1).toString(), instance[Calendar.DAY_OF_WEEK].toString(),
            week.toString(), (times.indexOf(time) + 1).toString());
    }

    companion object {
        val campuses = listOf(
            "磬苑校区",
            "龙河校区"
        )
        val times = listOf(
            "1,2节",
            "3,4节",
            "5,6节",
            "7,8节",
            "9,10,11节",
            "上午",
            "下午",
            "晚上",
            "白天",
            "整天",
        )
    }
}
