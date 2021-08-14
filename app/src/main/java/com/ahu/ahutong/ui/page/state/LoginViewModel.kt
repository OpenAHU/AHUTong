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
import com.ahu.ahutong.data.reptile.Reptile
import com.ahu.ahutong.data.reptile.ReptileDataSource
import com.ahu.ahutong.data.reptile.ReptileManager
import com.ahu.ahutong.data.reptile.ReptileUser
import com.ahu.ahutong.data.reptile.store.DefaultCookieStore
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * @Author: SinkDev
 * @Date: 2021/8/14-上午8:58
 * @Email: 468766131@qq.com
 */
class LoginViewModel : ViewModel() {
    var loginType = AHUCache.getLoginType()
    val loginResult = MutableLiveData<Result<User>>()

    fun login(username: String, password: String) = viewModelScope.launch {
        val result: Result<User> = if (loginType == User.UserType.AHU_LOCAL) {
            //爬虫登录
            ReptileManager.getInstance().cookieStore = DefaultCookieStore()
            val result = Reptile.login(ReptileUser(username, password))
            if (result.isSuccess) {
                val user = User()
                user.name = username
                AHUCache.saveCurrentUser(user)
                AHUCache.saveCurrentPassword(password)
                //切换数据源
                AHURepository.dataSource = ReptileDataSource(ReptileUser(username, password))
                Result.success(user)
            } else {
                Result.failure(result.exceptionOrNull() ?: Throwable("登录失败，请检查账号密码后重试！"))
            }
        } else {
            try {
                //普通登录
                val response = AHUService.API.login(username, password, loginType)
                if (response.isSuccessful) {
                    AHUCache.saveCurrentUser(response.data)
                    //切换数据源
                    AHURepository.dataSource = APIDataSource()
                    Result.success(response.data)
                } else {
                    Result.failure(Throwable(response.msg))
                }
            }catch (e: Exception){
                Result.failure(Throwable("登录失败，系统异常。"))
            }

        }
        loginResult.value = result
    }

    companion object {
        val type = mapOf(
            R.id.rd_wisdom_local to User.UserType.AHU_LOCAL,
            R.id.rd_wisdom to User.UserType.AHU_Wisdom,
            R.id.rd_teach to User.UserType.AHU_Teach
        )
    }

}