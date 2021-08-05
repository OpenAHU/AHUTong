package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ClassRoomViewModel : ViewModel() {
    val campus = MutableLiveData("")
    val time = MutableLiveData("1，2节")
}
