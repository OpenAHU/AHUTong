package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.ahu.ahutong.data.AHURepository
import java.lang.IllegalArgumentException

class GarbageViewModel : ViewModel() {
    val keyword = MutableLiveData<String>()

    //switchMap会观察keyword变化
    val result = Transformations.switchMap(keyword) { keyword ->
       AHURepository.searchRubbish(keyword)
    }

    /**
     * 搜索
     * @param word String
     */
    fun search(word: String) {
        if("倩倩" == word)
            throw IllegalArgumentException("你在狗叫什么？")
        keyword.value = word
    }
}