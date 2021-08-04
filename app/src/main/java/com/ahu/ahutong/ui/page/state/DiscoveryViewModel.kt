package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ahu.ahutong.data.model.Banner
import com.ahu.ahutong.data.model.Tool


/**
 * @Author Simon
 * @Date 2021/8/3-22:12
 * @Email 330771794@qq.com
 */
class DiscoveryViewModel: ViewModel() {
    val bannerData by lazy {
        listOf(Banner(), Banner(), Banner())
    }


    val tools by lazy {
        Tool.defaultTools
    }
}