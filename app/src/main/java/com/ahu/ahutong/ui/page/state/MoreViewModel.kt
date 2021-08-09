package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.ViewModel
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.Developer
import com.ahu.ahutong.data.model.Tool

class MoreViewModel : ViewModel() {
    val study by lazy {
        listOf(
            Tool("宣讲会", R.mipmap.preach, R.id.developer_fragment),
            Tool("图书馆", R.mipmap.library, R.id.developer_fragment),
            Tool("空教室", R.mipmap.classroom, R.id.classroom_fragment),
            Tool("成绩单", R.mipmap.score, R.id.developer_fragment),
            Tool("考场查询", R.mipmap.examination_room, R.id.developer_fragment),
            Tool("共享课表", R.mipmap.cengke, R.id.developer_fragment),
            Tool("挂科率", R.mipmap.guakelv, R.id.developer_fragment),
            Tool("校园卡",R.mipmap.pingjiao,R.id.developer_fragment)
        )
    }
    val life by lazy {
        listOf(
            Tool("浴室开放", R.mipmap.bathroom, R.id.bathroom_fragment),
            Tool("电话簿", R.mipmap.telephone_directory, R.id.teldirectory_fragment),
            Tool("垃圾分类", R.mipmap.rubbish, R.id.developer_fragment),
            Tool("校园生活",R.mipmap.school_life,R.id.developer_fragment),
            Tool("校长信封",R.mipmap.letter,R.id.developer_fragment),
            Tool("校历",R.mipmap.calendar,R.id.developer_fragment),
            Tool("校车时间",R.mipmap.school_bus,R.id.developer_fragment),
            Tool("校园地图",0,R.id.developer_fragment)
        )
    }
}