package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.ViewModel
import com.ahu.ahutong.data.model.Developer

/**
 *
 * @Author: Sink
 * @Date: 2021/7/31-下午8:40
 * @Email: 468766131@qq.com
 */
class DeveloperViewModel : ViewModel() {
    val developers by lazy {
        listOf(
            Developer("高玉灿", "Android 客户端 & 划水 开发者", "468766131"),
            Developer("谭哲昊", "Android 客户端 & 摸鱼 开发者", "330771794"),
            Developer("王学雷", "Android 客户端 & 美化 开发者", "257314409"),
            Developer("吴振龙", "爬虫 & 后端 开发者", "1298749337"),
            Developer("徐 海", "后端 & 卷王 开发者", "1479356730"),
            Developer("王岳赣", "后端 & 芭比Q 开发者", "1491605298"),
            Developer("王壮壮", "后端 & 摸鱼 开发者", "2539907983")
        )
    }
}
