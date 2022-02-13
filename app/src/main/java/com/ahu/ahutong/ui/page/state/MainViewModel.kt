package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.api.AHUService
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.AppVersion
import com.ahu.ahutong.ui.widget.schedule.bean.DefaultDataUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

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

    val latestVersions: MutableLiveData<Result<AHUResponse<AppVersion>>> = MutableLiveData()

    fun getAppLatestVersion() = viewModelScope.launch {
        latestVersions.value = withContext(Dispatchers.IO) {
            try {
                Result.success(AHUService.API.getLatestVersion())
            } catch (e: Exception) {
                Result.failure(Throwable("网络连接异常，获取最新版本失败！"))
            }
        }
    }

    fun addAppAccess() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            try {
                AHUService.API.addAppAccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}