package com.ahu.ahutong.ui.state

import android.graphics.Bitmap
import android.util.Log
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
import com.ahu.ahutong.data.crawler.api.adwmh.AdwmhApi
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.Banner
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.ext.launchSafe
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

/**
 * @Author Simon
 * @Date 2021/8/3-22:12
 * @Email 330771794@qq.com
 */


@HiltViewModel
class DiscoveryViewModel @Inject constructor() : ViewModel() {

    val TAG = DiscoveryViewModel::class.java.simpleName

    val bathroom = mutableStateMapOf<String, String>()
    var balance by mutableStateOf(0.0)
    var transitionBalance by mutableStateOf(0.0)

    val visibilities = mutableStateListOf<Int>()

    val bannerData: MutableLiveData<List<Banner>> by lazy {
        MutableLiveData<List<Banner>>()
    }

    var qrcode = MutableStateFlow<Bitmap?>(null)
    var state = MutableStateFlow<Boolean>(false);

    fun loadActivityBean() {
        viewModelScope.launchSafe {

            AHURepository.getCardMoney().onSuccess {
                balance = it.balance
//                transitionBalance = it.transitionBalance
            }

            AHURepository.getBathRooms().onSuccess {
                it.stream().forEach {
                    bathroom += it.bathroom to it.openStatus
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

    fun loadQrCode() {
        viewModelScope.launchSafe {
            withContext(Dispatchers.IO){
                state.value = false
                try {
                    val response = withContext(Dispatchers.IO) {
                        AdwmhApi.API.getQrcode()
                    }
                    if (response.code == 10000) {
                        val encoder = BarcodeEncoder()
                        qrcode.value = encoder.encodeBitmap(
                            response.`object`,
                            BarcodeFormat.QR_CODE,
                            400,
                            400
                        )
                    } else {
                        Log.e("QR", "接口返回错误: ${response.msg}")
                    }
                } catch (e: IOException) {
                    Log.e("QR", "网络异常", e)
                } catch (e: Exception) {
                    Log.e("QR", "未知异常", e)
                }
                state.value = true
            }
        }

    }

}
