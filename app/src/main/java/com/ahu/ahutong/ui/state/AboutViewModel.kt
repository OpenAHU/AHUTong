package com.ahu.ahutong.ui.state


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import arch.sink.utils.Utils

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

    val tipState = mutableStateOf<String?>(null)

}
