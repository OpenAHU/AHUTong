package com.ahu.ahutong.ui.state

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
            Developer("高玉灿", "Android 客户端 & 划水", "468766131"),
            Developer("谭哲昊", "Android 客户端 & 摸鱼", "330771794"),
            Developer("王学雷", "Android 客户端 & 美化", "257314409"),
            Developer("吴振龙", "爬虫 & 后端", "1298749337"),
            Developer("徐    海", "后端 & 卷王", "1479356730"),
            Developer("王岳赣", "后端 & 芭比Q", "1491605298"),
            Developer("王壮壮", "后端 & 摸鱼", "2539907983")
        )
    }
}
