package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.ViewModel
import com.ahu.ahutong.data.model.Developer

/**
 *
 * @Author: Sink
 * @Date: 2021/7/31-下午8:40
 * @Email: 468766131@qq.com
 */
class DeveloperViewModel: ViewModel() {
    val developers by lazy {
        listOf(
            Developer("吴振龙", "爬虫&后端开发者", "1298749337"),
            Developer("高玉灿", "Android 客户端开发者", "468766131"),
            Developer("谭哲昊", "Android 客户端开发者","330771794")
        )
    }
}