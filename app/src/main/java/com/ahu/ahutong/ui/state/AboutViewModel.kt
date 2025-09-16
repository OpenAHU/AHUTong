package com.ahu.ahutong.ui.state


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arch.sink.utils.Utils
import com.ahu.ahutong.data.api.AHUService
import com.ahu.ahutong.data.model.AppVersion
import com.ahu.ahutong.ext.launchSafe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @Author: SinkDev
 * @Date: 2021/8/25-下午1:10
 * @Email: 468766131@qq.com
 */
class AboutViewModel : ViewModel() {
    val versionName: String? by lazy {
        val packageInfo = Utils.getApp().packageManager.getPackageInfo(
            Utils.getApp().packageName,
            0
        )
        packageInfo.versionName
    }

    val newVersionDialogState = mutableStateOf<AppVersion?>(null)
    val tipState = mutableStateOf<String?>(null)

    fun checkForUpdates() {
        viewModelScope.launchSafe {
            withContext(Dispatchers.IO) {
                try {
                    val response = AHUService.API.getLatestVersion()
                    if (!response.isSuccessful) {
                        tipState.value = "检查更新失败：${response.msg}"
                        return@withContext
                    }
                    if (response.data.version == versionName) {
                        tipState.value = "当前已是最新版本！"
                        return@withContext
                    }
                    newVersionDialogState.value = response.data
                } catch (e: Exception) {
                    tipState.value = "检查更新失败：${e.message}"
                }
            }
        }
    }
}
