package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ahu.ahutong.data.model.Banner
import com.ahu.ahutong.data.model.News
import com.ahu.ahutong.data.model.Sector
import com.ahu.ahutong.data.model.Tool


/**
 * @Author Simon
 * @Date 2021/8/3-22:12
 * @Email 330771794@qq.com
 */
class DiscoveryViewModel: ViewModel() {
    val bannerData: MutableLiveData<List<Banner>> by lazy {
        MutableLiveData<List<Banner>>()
    }
    val newData: MutableLiveData<MutableList<News>> by lazy {
        MutableLiveData<MutableList<News>>()
    }

    val tools by lazy {
        Tool.defaultTools
    }
    val sectors by lazy {
        Sector.defaultSectors
    }
}