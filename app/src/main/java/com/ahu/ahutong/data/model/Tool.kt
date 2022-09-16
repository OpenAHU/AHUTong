package com.ahu.ahutong.data.model

import com.ahu.ahutong.R

data class Tool(val title: String, val resID: Int, val action: Int) {
    companion object {
        val defaultTools = listOf(
            Tool("成绩单", R.mipmap.score, R.id.gradle_fragment),
            Tool("考场查询", R.mipmap.examination_room, R.id.exam_fragment),
            Tool("电话簿", R.mipmap.telephone_directory, R.id.teldirectory_fragment)
        )
    }
}
