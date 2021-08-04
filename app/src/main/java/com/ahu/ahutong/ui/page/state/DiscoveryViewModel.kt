package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.Banner
import com.ahu.ahutong.data.model.Developer
import com.ahu.ahutong.data.model.GridItem


/**
 * @Author Simon
 * @Date 2021/8/3-22:12
 * @Email 330771794@qq.com
 */
class DiscoveryViewModel: ViewModel() {
    private var bannerData: MutableLiveData<ArrayList<Banner>>? = null

    public fun getBannerData(): MutableLiveData<ArrayList<Banner>>? {
        if (bannerData == null) {
            bannerData = MutableLiveData()
        }
        return bannerData
    }

    override fun onCleared() {
        bannerData=null
        super.onCleared()
    }
    val gridItems by lazy {
        arrayListOf(
            GridItem(R.mipmap.feedback, "宣讲会"),
            GridItem(R.mipmap.library, "图书馆"),
            GridItem(R.mipmap.classroom, "空教室"),
            GridItem(R.mipmap.library, "图书馆"),
            GridItem(R.mipmap.score, "成绩单"),
            GridItem(R.mipmap.examination_room, "考场查询"),
            GridItem(R.mipmap.cengke, "共享课表"),
            GridItem(R.mipmap.pingjiao, "校园卡"),
            GridItem(R.mipmap.bathroom, "浴室开放"),
            GridItem(R.mipmap.rubbish, "垃圾分类"),
            GridItem(R.mipmap.more, "更多"),
        )
    }
}