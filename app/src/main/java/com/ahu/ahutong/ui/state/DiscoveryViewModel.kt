package com.ahu.ahutong.ui.state

import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arch.sink.utils.TimeUtils
import arch.sink.utils.Utils
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.Banner
import com.ahu.ahutong.data.model.Course
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * @Author Simon
 * @Date 2021/8/3-22:12
 * @Email 330771794@qq.com
 */
class DiscoveryViewModel : ViewModel() {
    val bathroom = mutableStateMapOf<String, String>()
    var balance by mutableStateOf(0.0)
    var transitionBalance by mutableStateOf(0.0)

    val visibilities = mutableStateListOf<Int>()

    val bannerData: MutableLiveData<List<Banner>> by lazy {
        MutableLiveData<List<Banner>>()
    }

    fun loadActivityBean() {
        viewModelScope.launch {
            AHURepository.getBathRooms().onSuccess {
                it.stream().forEach {
                    bathroom += it.bathroom to it.openStatus
                }
                AHURepository.getCardMoney().onSuccess {
                    balance = it.balance
                    transitionBalance = it.transitionBalance
                }
            }
        }
    }

    fun loadBanner() {
        viewModelScope.launch {
            AHURepository.getBanner()
                .onSuccess {
                    bannerData.value = it
                }.onFailure {
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
        // 获取第几周
        val time = AHUCache.getSchoolTermStartTime(year, term)
        if (time == null) {
            Toast.makeText(Utils.getApp(), "请填写开学时间后再试", Toast.LENGTH_SHORT).show()
            return emptyList()
        }
        // 根据开学时间， 获取当前周数
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
            .parse(time)
        val week = (TimeUtils.getTimeDistance(Date(), date) / 7 + 1).toInt()
        val courses = AHUCache.getSchedule(year, term) ?: emptyList()
        val thisWeek = Calendar.getInstance()[Calendar.DAY_OF_WEEK] - 1
        return courses.filter { t ->
            t.weekday == thisWeek && t.startWeek <= week && t.endWeek >= week
        }.sortedBy {
            it.startTime
        }
    }
}
