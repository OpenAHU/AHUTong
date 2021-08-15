package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.ViewModel
import com.ahu.ahutong.data.model.Developer
import com.ahu.ahutong.data.model.Tel

class TelDirectoryViewModel : ViewModel() {
    val tels by lazy {
        listOf(
            Tel(Tel.Type.常用, "网络服务中心", "055163861118"),
            Tel(Tel.Type.常用, "一卡通办公室", "055163861077"),
            Tel(Tel.Type.常用, "报警电话", "0555163861110"),
            Tel(Tel.Type.常用, "校医院值班电话", "0555163861120"),
            Tel(Tel.Type.常用, "物业办公室", "0555163861044"),
            Tel(Tel.Type.常用, "物业修缮服务室", "0555163861114"),
            Tel(Tel.Type.常用, "考试考务中心", "0555163861053"),
            Tel(Tel.Type.常用, "学生资助管理中心", "0555163861008"),
            Tel(Tel.Type.常用, "大学生勤俭中心", "0555163861181"),
            Tel(Tel.Type.常用, "图书馆", "0555163861109"),

            Tel(Tel.Type.教务处,"综合办公室","0555163861055")
        )
    }
}
