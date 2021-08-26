package com.ahu.ahutong.ui.page.state

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arch.sink.utils.TimeUtils
import arch.sink.utils.Utils
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.model.Banner
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
            AHURepository.getCardMoney()
                .onSuccess {
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
                        .parse("2021-08-26")
                    val timeDistance = TimeUtils.getTimeDistance(Date(), date).toInt() % 2
                    val north = if (timeDistance == 0) "男生" else "女生"
                    val south = if (timeDistance == 1) "男生" else "女生"

                    activityBean.value = DiscoveryAdapter.ActivityBean(it, north, south)

                }.onFailure {
                    Toast.makeText(Utils.getApp(), it.message, Toast.LENGTH_SHORT).show()
                }
        }
    }
}