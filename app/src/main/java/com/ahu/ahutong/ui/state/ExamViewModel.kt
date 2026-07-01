package com.ahu.ahutong.ui.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.Exam
import com.ahu.ahutong.ext.launchSafe
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class RefreshState { IDLE, LOADING, UPDATED }

class ExamViewModel : ViewModel() {
    val data = MutableLiveData<Result<List<Exam>>>()
    val isLoading = MutableStateFlow<Boolean?>(null)
    val errorMessage = MutableStateFlow<String?>(null)

    private val _refreshState = MutableStateFlow(RefreshState.IDLE)
    val refreshState = _refreshState.asStateFlow()

    // 防止重复刷新
    private var refreshJob: Job? = null

    fun loadExam(isRefresh: Boolean = false) {
        // 正在刷新中则忽略新请求
        if (_refreshState.value == RefreshState.LOADING) return
        // 首次自动后台加载也跳过重复
        if (!isRefresh && isLoading.value == true) return

        refreshJob?.cancel()
        refreshJob = viewModelScope.launchSafe {
            val user = AHUCache.getCurrentUser()
            if (user == null && !AHUCache.getMockData()) {
                data.value = Result.failure(Throwable("账户未登录"))
                errorMessage.value = "账户未登录"
                return@launchSafe
            }

            // 1. 优先展示缓存数据，首屏秒出
            val cached = AHUCache.getExamInfo().orEmpty()
            if (cached.isNotEmpty() && !isRefresh) {
                data.value = Result.success(cached)
            }

            // 手动刷新时：先显示 LOADING，保证最少 1 秒可见
            if (isRefresh) {
                _refreshState.value = RefreshState.LOADING
                // 最小加载时间 1 秒，避免一闪而过
                delay(800)
            }

            // 仅无缓存时显示全屏加载动画
            if (cached.isEmpty()) {
                isLoading.value = true
            }
            errorMessage.value = null

            val result = AHURepository.getExamInfo(
                isRefresh = true,
                studentID = user?.xh ?: "mock-student",
                studentName = user?.name ?: "Mock 用户"
            )

            if (result.isSuccess) {
                val newExams = result.getOrNull().orEmpty()

                // 与缓存比对，有差异才更新 UI
                val cachedJson = Gson().toJson(cached)
                val newJson = Gson().toJson(newExams)
                if (cachedJson != newJson) {
                    AHUCache.saveExamInfo(newExams)
                    data.value = Result.success(newExams)
                }

                // 手动刷新后显示"已更新"，最少 2 秒
                if (isRefresh) {
                    _refreshState.value = RefreshState.UPDATED
                    delay(2000)
                    _refreshState.value = RefreshState.IDLE
                }
            } else {
                // 网络失败：手动刷新时立即恢复 IDLE
                if (isRefresh) {
                    _refreshState.value = RefreshState.IDLE
                }
                if (cached.isEmpty()) {
                    data.value = result
                    errorMessage.value = result.exceptionOrNull()?.message ?: "获取考试信息失败"
                }
            }

            isLoading.value = false
        }
    }
}
