package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.Exam
import kotlinx.coroutines.launch

class ExamViewModel : ViewModel() {
    val data = MutableLiveData<Result<List<Exam>>>()
    fun loadExam(isRefresh: Boolean = true) = viewModelScope.launch {
        val user = AHUCache.getCurrentUser()
        if (user == null) {
            data.value = Result.failure(Throwable("账户未登录"))
            return@launch
        }
        data.value = AHURepository.getExamInfo(isRefresh, user.xh, user.name)
    }
}
