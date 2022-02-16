package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.R
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.api.AHUService
import com.ahu.ahutong.data.api.APIDataSource
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.User
import com.ahu.ahutong.utils.RSA
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * @Author: SinkDev
 * @Date: 2021/8/14-上午8:58
 * @Email: 468766131@qq.com
 */
class LoginViewModel : ViewModel() {
    var loginType = AHUCache.getLoginType()
    val serverLoginResult = MutableLiveData<Result<User>>()

    fun loginWithServer(username: String, password: String) = viewModelScope.launch {
        val user = User()
        user.name = username
        val result: Result<User> = try {
            //普通登录
            val response = AHUService.API.login(
                username,
                RSA.encryptByPublicKey(
                    password.toByteArray(Charsets.UTF_8)
                ),
                loginType
            )
            if (response.isSuccessful) {
                AHUCache.saveCurrentUser(user)
                //切换数据源
                AHUCache.saveLoginType(loginType)
                AHURepository.dataSource = APIDataSource()
                Result.success(response.data)
            } else {
                Result.failure(Throwable(response.msg))
            }
        } catch (e: Exception) {
            Result.failure(Throwable("登录失败，系统异常。"))
        }
        serverLoginResult.value = result
    }

    companion object {
        val type = mapOf(
            R.id.rd_wisdom_local to User.UserType.AHU_LOCAL,
            R.id.rd_wisdom to User.UserType.AHU_Wisdom,
            R.id.rd_teach to User.UserType.AHU_Teach
        )
    }

}