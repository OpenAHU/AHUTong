package com.ahu.ahutong.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.User
import com.ahu.ahutong.ext.launchSafe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @Author: SinkDev
 * @Date: 2021/8/14-上午8:58
 * @Email: 468766131@qq.com
 */
class LoginViewModel : ViewModel() {
    var state by mutableStateOf(LoginState.Idle)
    var failureMessage by mutableStateOf("")
    var succeedMessage by mutableStateOf("")

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
