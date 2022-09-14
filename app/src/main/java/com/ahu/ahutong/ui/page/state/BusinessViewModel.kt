package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.ViewModel
import arch.sink.utils.Utils
import com.ahu.ahutong.data.model.Developer


class BusinessViewModel : ViewModel() {
    val partner by lazy {
        listOf(
            Developer("安大通官Q", "小程序Zone投稿", "3414609310"),
            Developer("余晓波", "这个人有点懒，居然什么都不想说", "1363952921"),
            Developer("陈冠宇", "这个人也有点懒，也是什么都不想说","853134978")
        )
    }
}