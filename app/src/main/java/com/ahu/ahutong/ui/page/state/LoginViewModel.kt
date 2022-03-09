package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.data.api.AHUService
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.User
import com.ahu.ahutong.utils.RSA
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * @Author: SinkDev
 * @Date: 2021/8/14-上午8:58
 * @Email: 468766131@qq.com
 */
class LoginViewModel : ViewModel() {
    val serverLoginResult = MutableLiveData<Result<User>>()

    fun loginWithServer(username: String, teachPassword: String, wisdomPassword: String) =
        viewModelScope.launch {
            val user = User()
            user.name = username
            val result: Result<User> = try {
                //智慧安大登录
                val wisdomResponse = async {
                    val encryptedPassword =
                        RSA.encryptByPublicKey(wisdomPassword.toByteArray(Charsets.UTF_8))
                    AHUService.API.login(username, encryptedPassword, User.UserType.AHU_Wisdom)
                }
                // 教务登录
                val teachResponse = async {
                    val encryptedPassword =
                        RSA.encryptByPublicKey(teachPassword.toByteArray(Charsets.UTF_8))
                    AHUService.API.login(username, encryptedPassword, User.UserType.AHU_Teach)
                }
                // 登录必须全部成功
                if (wisdomResponse.await().isSuccessful && teachResponse.await().isSuccessful) {
                    AHUCache.saveCurrentUser(user)
                    // 保存智慧安大密码
                    AHUCache.saveWisdomPassword(wisdomPassword)
                    Result.success(user)
                } else {
                    throw Throwable("登录认证失败，请查看密码是否正确。")
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
            serverLoginResult.value = result
        }


}