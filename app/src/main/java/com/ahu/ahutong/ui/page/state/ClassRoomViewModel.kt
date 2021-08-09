package com.ahu.ahutong.ui.page.state

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ClassRoomViewModel : ViewModel() {
    val campus = MutableLiveData<String>("磬苑校区")
    val time = MutableLiveData<String>("1，2节")
    companion object {
        val campus = listOf(
            "磬苑校区",
            "龙河校区"
        )
        val times = listOf(
            "1,2节",
            "3,4节",
            "5,6节",
            "7,8节",
            "9,10,11节",
            "上午",
            "下午",
            "晚上",
            "白天",
            "整天",
        )
    }
}
