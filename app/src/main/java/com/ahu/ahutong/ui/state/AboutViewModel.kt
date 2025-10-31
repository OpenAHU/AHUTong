package com.ahu.ahutong.ui.state


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ahu.ahutong.AHUApplication

/**
 * @Author: SinkDev
 * @Date: 2021/8/25-下午1:10
 * @Email: 468766131@qq.com
 */
class AboutViewModel : ViewModel() {
    val versionName: String? by lazy {
        val packageInfo = AHUApplication.getApp().packageManager.getPackageInfo(
            AHUApplication.getApp().packageName,
            0
        )
        packageInfo.versionName
    }

    val tipState = mutableStateOf<String?>(null)

}
