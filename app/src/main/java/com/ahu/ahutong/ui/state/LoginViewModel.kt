package com.ahu.ahutong.ui.state

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arch.sink.utils.Utils
import com.ahu.ahutong.AHUApplication
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.api.AHUCookieJar
import com.ahu.ahutong.data.api.AHUService
import com.ahu.ahutong.data.crawler.api.adwmh.AdwmhApi
import com.ahu.ahutong.data.crawler.api.jwxt.JwxtApi
import com.ahu.ahutong.data.crawler.configs.Constants
import com.ahu.ahutong.data.crawler.manager.CookieManager
import com.ahu.ahutong.data.crawler.manager.TokenManager
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.User
import com.ahu.ahutong.data.reptile.utils.DES
import com.ahu.ahutong.ext.launchSafe
import com.ahu.ahutong.utils.RSA
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup

/**
 * @Author: SinkDev
 * @Date: 2021/8/14-上午8:58
 * @Email: 468766131@qq.com
 */
class LoginViewModel : ViewModel() {
    var state by mutableStateOf(LoginState.Idle)
    var failureMessage by mutableStateOf("")
    var succeedMessage by mutableStateOf("")
    val serverLoginResult = MutableLiveData<Result<User>>()

    fun loginWithServer(userID: String, wisdomPassword: String) =
        viewModelScope.launch {
            val result: Result<User> = try {
                state = LoginState.InProgress
                // 智慧安大登录
                val wisdomResponse = withContext(Dispatchers.IO) {
                    val encryptedPassword =
                        RSA.encryptByPublicKey(wisdomPassword.toByteArray(Charsets.UTF_8))
                    AHUCache.saveWisdomPassword(encryptedPassword)
                    AHUService.API.login(userID, encryptedPassword, User.UserType.AHU_Wisdom)
                }
                // 登录必须全部成功
                if (wisdomResponse.isSuccessful) {
                    wisdomResponse.data.xh = userID
                    AHUCache.saveCurrentUser(wisdomResponse.data)
                    state = LoginState.Succeeded
                    succeedMessage = "欢迎，${wisdomResponse.data.name}！"
                    Result.success(wisdomResponse.data)
                } else {
                    state = LoginState.Failed
                    failureMessage = wisdomResponse.msg
                    Result.failure(IllegalArgumentException(wisdomResponse.msg))
                }
            } catch (e: Throwable) {
                CrashReport.postCatchedException(e) // 上报异常
                Result.failure(e)
            }
            serverLoginResult.value = result
        }


    /**
     * 爬虫登录
     */

    fun loginWithCrawler(userID: String, password: String) = viewModelScope.launchSafe {

        val result: Result<User> = try {
            state = LoginState.InProgress
            val response = withContext(Dispatchers.IO) {
                AHURepository.loginWithCrawler(userID, password)
            }

            if(response.isSuccessful){
                state = LoginState.Succeeded
                succeedMessage = "欢迎，${response.data.name}！"
                AHUCache.saveCurrentUser(response.data)
                AHUCache.saveWisdomPassword(password)
                Result.success(response.data)
            }else{
                state = LoginState.Failed
                failureMessage = response.msg
                Result.failure(IllegalArgumentException(response.msg))
            }


        }catch (e: Throwable) {
            Result.failure(e)
        }


    }
}

enum class LoginState {
    Idle, InProgress, Failed, Succeeded
}
