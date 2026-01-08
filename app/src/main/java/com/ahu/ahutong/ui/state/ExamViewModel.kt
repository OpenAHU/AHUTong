package com.ahu.ahutong.ui.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.Exam
import com.ahu.ahutong.ext.launchSafe
import kotlinx.coroutines.flow.MutableStateFlow

class ExamViewModel : ViewModel() {
    val data = MutableLiveData<Result<List<Exam>>>()
    val isLoading = MutableStateFlow<Boolean?>(null)
    val errorMessage = MutableStateFlow<String?>(null)

    fun loadExam(isRefresh: Boolean = false) = viewModelScope.launchSafe {
        val user = AHUCache.getCurrentUser()
        if (user == null) {
            data.value = Result.failure(Throwable("账户未登录"))
            errorMessage.value = "账户未登录"
            return@launchSafe
        }
        isLoading.value = true
        errorMessage.value = null
        val result = AHURepository.getExamInfo(isRefresh, user.xh, user.name)
        data.value = result
        if (result.isFailure) {
            errorMessage.value = result.exceptionOrNull()?.message ?: "获取考试信息失败"
        }
        isLoading.value = false
    }
}
