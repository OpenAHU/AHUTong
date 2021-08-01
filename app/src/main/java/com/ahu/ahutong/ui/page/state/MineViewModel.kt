package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ahu.ahutong.data.AHUCache
import com.ahu.ahutong.data.model.User

/**
 * @Author SinkDev
 * @Date 2021/7/27-15:48
 * @Email 468766131@qq.com
 */
class MineViewModel : ViewModel() {
    val user = MutableLiveData<User>()


    fun isLogin() = AHUCache.isLogin()

    fun logout(){
        AHUCache.clearCurrentUser()
    }
}