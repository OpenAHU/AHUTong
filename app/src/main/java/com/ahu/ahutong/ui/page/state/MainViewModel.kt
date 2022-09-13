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

    fun logout() {
        AHUCache.logout()
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
    }


    val latestVersions: MutableLiveData<Result<AHUResponse<AppVersion>>> = MutableLiveData()

    val retryLoginResult = SingleLiveEvent<Result<User>>()

    fun retryLogin() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            val result = try {
                val password = AHUCache.getWisdomPassword()!!
                val response = AHUService.API.login(AHUCache.getCurrentUser()!!.name,
                        password, User.UserType.AHU_Wisdom)
                if (!response.isSuccessful) {
                    Result.failure(Throwable(response.msg))
                } else {
                    Result.success(response.data)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
            retryLoginResult.postValue(result)
        }
    }

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

}