package com.ahu.ahutong.ui.page.state

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import arch.sink.utils.Utils
import com.sink.library.update.CookApkUpdate

/**
 * @Author: SinkDev
 * @Date: 2021/8/25-下午1:10
 * @Email: 468766131@qq.com
 */
class AboutViewModel : ViewModel() {
    val versionName by lazy {
        val packageInfo = Utils.getApp().packageManager.getPackageInfo(Utils.getApp().packageName, 0)
        packageInfo.versionName
    }
}