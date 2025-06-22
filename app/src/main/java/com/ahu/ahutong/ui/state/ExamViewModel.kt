package com.ahu.ahutong.ui.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.Exam
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ExamViewModel : ViewModel() {
    val data = MutableLiveData<Result<List<Exam>>>()
    val isLoading = MutableStateFlow<Boolean?>(null)
    fun loadExam(isRefresh: Boolean = true) = viewModelScope.launch {
        val user = AHUCache.getCurrentUser()
        if (user == null) {
            data.value = Result.failure(Throwable("账户未登录"))
            return@launch
        }
        isLoading.value = true
        data.value = AHURepository.getExamInfo(isRefresh, user.xh, user.name)
        isLoading.value = false
    }
}
