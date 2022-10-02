package com.ahu.ahutong.ui.state

import android.webkit.CookieManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.data.api.AHUService
import com.ahu.ahutong.data.dao.AHUCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @Author: SinkDev
 * @Date: 2021/8/9-上午9:34
 * @Email: 468766131@qq.com
 */
class MainViewModel : ViewModel() {

    /**
     * 日活统计
     * @return Job
     */
    fun addAppAccess() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            try {
                AHUService.API.addAppAccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun logout() {
        AHUCache.logout()
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
    }
}
