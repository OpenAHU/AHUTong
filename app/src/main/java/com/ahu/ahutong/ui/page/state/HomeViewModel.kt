package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.api.AHUService
import com.ahu.ahutong.data.model.AppVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @Author SinkDev
 * @Date 2021/7/27-18:50
 * @Email 468766131@qq.com
 */
class HomeViewModel : ViewModel() {

    val latestVersions: MutableLiveData<Result<AHUResponse<AppVersion>>> = MutableLiveData()

    /**
     * App 更新
     * @return Job
     */
    fun getAppLatestVersion() = viewModelScope.launch {
        latestVersions.value = withContext(Dispatchers.IO) {
            try {
                Result.success(AHUService.API.getLatestVersion())
            } catch (e: Exception) {
                Result.failure(Throwable("网络连接异常，获取最新版本失败！"))
            }
        }
    }
}
