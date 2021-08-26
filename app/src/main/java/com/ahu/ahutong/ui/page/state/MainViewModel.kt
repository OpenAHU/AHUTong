package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.ui.widget.schedule.bean.DefaultDataUtils

/**
 * @Author: SinkDev
 * @Date: 2021/8/9-上午9:34
 * @Email: 468766131@qq.com
 */
class MainViewModel : ViewModel() {
    val isLogin by lazy {
        MutableLiveData(AHUCache.isLogin())
    }

    fun logout() {
        isLogin.value = false
        AHUCache.clearCurrentUser();
    }

    val scheduleTheme by lazy {
        MutableLiveData(AHUCache.getScheduleTheme() ?: DefaultDataUtils.getDefaultTheme())
    }
}