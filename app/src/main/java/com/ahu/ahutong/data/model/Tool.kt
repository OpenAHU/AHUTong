package com.ahu.ahutong.data.model

import com.ahu.ahutong.R


data class Tool(val title: String, val resID: Int, val des: Int){
    companion object{
        val defaultTools = listOf(
            Tool("宣讲会", R.mipmap.preach, R.id.developer_fragment),
            Tool("图书馆", R.mipmap.library, R.id.developer_fragment),
            Tool("空教室", R.mipmap.classroom, R.id.developer_fragment),
            Tool("成绩单", R.mipmap.score, R.id.developer_fragment),
            Tool("考场查询", R.mipmap.examination_room, R.id.developer_fragment),
            Tool("共享课表", R.mipmap.cengke, R.id.developer_fragment),
            Tool("浴室开放", R.mipmap.bathroom, R.id.developer_fragment),
            //Tool("挂科率", R.mipmap.guakelv, R.id.fragment_pass),
            Tool("电话簿", R.mipmap.telephone_directory, R.id.developer_fragment),
            Tool("垃圾分类", R.mipmap.rubbish, R.id.developer_fragment),
            Tool("更多", R.mipmap.more, R.id.developer_fragment),
        )

    }
}

