package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ahu.ahutong.data.model.Rubbish

class GarbageViewModel :ViewModel() {
    val liveData=MutableLiveData<Result<List<Rubbish>>>()
    val random = listOf(
        "纸巾",
        "垃圾袋",
        "瓜子",
        "方便面",
        "苹果",
        "包装纸",
        "小龙虾",
        "电池",
        "橡皮泥",
        "猫砂",
        "西瓜",
        "卫生纸",
        "灰土",
        "男朋友",//对此疑惑
        "电灯泡"
    )
}