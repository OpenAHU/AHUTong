package com.ahu.ahutong.ui.page.state

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arch.sink.utils.TimeUtils
import arch.sink.utils.Utils
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.Banner
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.Tool
import com.ahu.ahutong.ui.adapter.DiscoveryAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


/**
 * @Author Simon
 * @Date 2021/8/3-22:12
 * @Email 330771794@qq.com
 */
class DiscoveryViewModel : ViewModel() {
    val bannerData: MutableLiveData<List<Banner>> by lazy {
        MutableLiveData<List<Banner>>()
    }

    val tools by lazy {
        Tool.defaultTools
    }

    val activityBean = MutableLiveData<DiscoveryAdapter.ActivityBean>()

    fun loadActivityBean() {
        viewModelScope.launch {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
                .parse("2021-08-26")
            val timeDistance = TimeUtils.getTimeDistance(Date(), date).toInt() % 2
            val north = if (timeDistance == 0) "男生" else "女生"
            val south = if (timeDistance == 1) "男生" else "女生"
            AHURepository.getCardMoney()
                .onSuccess {
                    activityBean.value = DiscoveryAdapter.ActivityBean(it, north, south)
                }.onFailure {
                    activityBean.value = DiscoveryAdapter.ActivityBean("0.00", north, south)
                    Toast.makeText(Utils.getApp(), it.message, Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun loadCourse(): List<Course> {
        val year = AHUCache.getSchoolYear()
        val term = AHUCache.getSchoolTerm()
        if (year == null || term == null) {
            Toast.makeText(Utils.getApp(), "请填写开学时间后再试", Toast.LENGTH_SHORT).show()
            return emptyList()
        }
        //获取第几周
        val time = AHUCache.getSchoolTermStartTime(year, term)
        if (time == null) {
            Toast.makeText(Utils.getApp(), "请填写开学时间后再试", Toast.LENGTH_SHORT).show()
            return emptyList()
        }
        //根据开学时间， 获取当前周数
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
            .parse(time)
        val week = (TimeUtils.getTimeDistance(Date(), date) / 7 + 1).toInt()
        val courses = AHUCache.getSchedule(year ?: "", term ?: "") ?: emptyList()
        val thisWeek = Calendar.getInstance()[Calendar.DAY_OF_WEEK] - 1
        return courses.filter { t ->
            t.weekday == thisWeek && t.startWeek <=week && t.endWeek >= week
        }.sortedBy {
            it.startTime
        }
    }
}