package com.ahu.ahutong.data.model

import android.graphics.Color

class Tel(type: Type?, var name: String, var tel: String) {
    var color = 0

    enum class Type {//我觉得中文阅读起来比英文轻松，故这样写
        常用, 教务处, 团委, 学生处, 财务处, 保卫处, 宿舍, 物业, 校医院
    }

    init {
        color = when (type) {
            Type.常用 -> Color.BLUE
            Type.教务处 -> Color.RED
            Type.团委 -> Color.WHITE
            Type.学生处 -> Color.GREEN
            Type.财务处 -> Color.YELLOW
            Type.保卫处 -> Color.BLACK
            Type.宿舍 -> Color.CYAN
            Type.物业 -> Color.GRAY
            Type.校医院 -> Color.MAGENTA
            null -> TODO()
        }
    }
}