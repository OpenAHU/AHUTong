package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.AHUApplication
import com.ahu.ahutong.data.api.AHUService
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.User
import com.ahu.ahutong.utils.RSA
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.net.SocketTimeoutException

/**
 * @Author: SinkDev
 * @Date: 2021/8/14-上午8:58
 * @Email: 468766131@qq.com
 */
class LoginViewModel : ViewModel() {
    val serverLoginResult = MutableLiveData<Result<User>>()

    fun loginWithServer(username: String, wisdomPassword: String) =
        viewModelScope.launch {
            val user = User()
            user.name = username
            val result: Result<User> = try {
                //智慧安大登录
                val wisdomResponse = withContext(Dispatchers.IO) {
                    val encryptedPassword =
                        RSA.encryptByPublicKey(wisdomPassword.toByteArray(Charsets.UTF_8))
                    AHUService.API.login(username, encryptedPassword, User.UserType.AHU_Wisdom)
                }
                // 登录必须全部成功
                if (wisdomResponse.isSuccessful) {
                    AHUCache.saveCurrentUser(user)
                    // 保存智慧安大密码
                    AHUCache.saveWisdomPassword(wisdomPassword)
                    AHUApplication.loginType.setValue(User.UserType.AHU_Wisdom)
                    Result.success(user)
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