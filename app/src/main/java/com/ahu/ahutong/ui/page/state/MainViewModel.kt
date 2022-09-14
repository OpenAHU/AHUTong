package com.ahu.ahutong.ui.page.state

import android.webkit.CookieManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.common.SingleLiveEvent
import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.api.AHUService
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.AppVersion
import com.ahu.ahutong.data.model.User
import com.ahu.ahutong.data.reptile.login.SinkWebViewClient
import com.ahu.ahutong.utils.RSA
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