package com.ahu.ahutong.ui.page.state

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arch.sink.utils.TimeUtils
import arch.sink.utils.Utils
import com.ahu.ahutong.AHUApplication
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.Banner
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.ui.adapter.DiscoveryAdapter
import kotlinx.coroutines.launch
import java.lang.StringBuilder
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

    val activityBean = MutableLiveData<DiscoveryAdapter.ActivityBean>()

    fun loadActivityBean() {
        viewModelScope.launch {
            AHURepository.getBathRooms()
                .onSuccess {
                    val stringBuilder =StringBuilder()
                    it.stream().forEach {
                        stringBuilder.append(it.bathroom+":"+it.openStatus.replace("wm","均可").replace("w","女生").replace("m","男生"))
                        stringBuilder.append("\n")
                    }
                    AHURepository.getCardMoney()
                        .onSuccess {
                            activityBean.value =
                                DiscoveryAdapter.ActivityBean(it.balance.toString(),it.transitionBalance.toString(), stringBuilder.toString())
                        }.onFailure {
                            activityBean.value = DiscoveryAdapter.ActivityBean("0.00","0.00",  stringBuilder.toString())
                        }
                }.onFailure {
                    activityBean.value = DiscoveryAdapter.ActivityBean("0.00","0.00", "桔园:女生\n竹园:均可\n惠园:均可")
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
        val courses = AHUCache.getSchedule(year, term) ?: emptyList()
        val thisWeek = Calendar.getInstance()[Calendar.DAY_OF_WEEK] - 1
        return courses.filter { t ->
            t.weekday == thisWeek && t.startWeek <= week && t.endWeek >= week
        }.sortedBy {
            it.startTime
        }
    }
}