package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arch.sink.utils.Utils
import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.api.AHUService
import com.ahu.ahutong.data.model.AppVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @Author: SinkDev
 * @Date: 2021/8/25-下午1:10
 * @Email: 468766131@qq.com
 */
class AboutViewModel : ViewModel() {
    val versionName: String by lazy {
        val packageInfo = Utils.getApp().packageManager.getPackageInfo(
            Utils.getApp().packageName,
            0
        )
        packageInfo.versionName
    }

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
