package com.ahu.ahutong.ui.page.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.data.api.AHUService
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.User
import com.ahu.ahutong.utils.RSA
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @Author: SinkDev
 * @Date: 2021/8/14-上午8:58
 * @Email: 468766131@qq.com
 */
class LoginViewModel : ViewModel() {
    var userID by mutableStateOf(TextFieldValue())
    var password by mutableStateOf(TextFieldValue())
    var isLoggingIn by mutableStateOf(false)
    val serverLoginResult = MutableLiveData<Result<User>>()

    fun loginWithServer(userID: String, wisdomPassword: String) =
        viewModelScope.launch {
            val result: Result<User> = try {
                isLoggingIn = true
                // 智慧安大登录
                val wisdomResponse = withContext(Dispatchers.IO) {
                    val encryptedPassword =
                        RSA.encryptByPublicKey(wisdomPassword.toByteArray(Charsets.UTF_8))
                    AHUCache.saveWisdomPassword(encryptedPassword)
                    AHUService.API.login(userID, encryptedPassword, User.UserType.AHU_Wisdom)
                }
                isLoggingIn = false
                // 登录必须全部成功
                if (wisdomResponse.isSuccessful) {
                    wisdomResponse.data.xh = userID
                    AHUCache.saveCurrentUser(wisdomResponse.data)
                    Result.success(wisdomResponse.data)
                } else {
                    Result.failure(IllegalArgumentException(wisdomResponse.msg))
                }
            } catch (e: Throwable) {
                CrashReport.postCatchedException(e) // 上报异常
                Result.failure(e)
            }
            serverLoginResult.value = result
        }
}
