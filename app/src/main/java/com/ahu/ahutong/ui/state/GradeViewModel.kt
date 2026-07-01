package com.ahu.ahutong.ui.state

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.GpaRankInfo
import com.ahu.ahutong.data.model.Grade
import com.ahu.ahutong.ext.getSchoolYears
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class GradeViewModel : ViewModel() {
    var totalGradePointAverage by mutableStateOf("暂无")
    var termGradePointAverage by mutableStateOf("暂无")
    var grade by mutableStateOf<Grade?>(null)
    var schoolYear by mutableStateOf(schoolYears.firstOrNull())
    var schoolTerm by mutableStateOf(terms.keys.firstOrNull())
    var errorMessage by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)
    var gpaRankInfo by mutableStateOf<GpaRankInfo?>(null)
    var rankLoading by mutableStateOf(false)

    fun getGpaRank() = viewModelScope.launch {
        rankLoading = true
        try {
            val result = AHURepository.getGpaRankInfo()
            if (result.code == 0) {
                gpaRankInfo = result.data
                AHUCache.saveGpaRankInfo(result.data)
                errorMessage = null
            } else {
                Log.w("GradeViewModel", "getGpaRank failed: ${result.msg}")
            }
        } catch (t: Throwable) {
            Log.w("GradeViewModel", "getGpaRank failed", t)
        } finally {
            rankLoading = false
        }
    }

    fun getGarde(isRefresh: Boolean = false) = viewModelScope.launch {
        isLoading = true
        try {
            val result = AHURepository.getGrade(isRefresh)
            if (result.isSuccess) {
                grade = result.getOrNull()
                errorMessage = null
            } else {
                errorMessage = result.exceptionOrNull()?.message ?: "获取成绩失败"
            }
        } catch (t: Throwable) {
            errorMessage = t.message ?: "获取成绩失败"
        } finally {
            isLoading = false
        }
    }

    var isRefreshing by mutableStateOf(false)
        private set

    fun refreshGrade() {
        viewModelScope.launch {
            isRefreshing = true
            try {
                getGarde(true)
                getGpaRank()
            } finally {
                isRefreshing = false
            }
        }
    }

    companion object {
        val schoolYears: List<String> by lazy {
            AHUCache.getCurrentUser()?.getSchoolYears()?.toList()
                ?: if (AHUCache.getMockData()) {
                    listOf("2024-2025", "2023-2024", "2022-2023")
                } else {
                    throw IllegalStateException("未登录，无法打开成绩界面！")
                }
        }
        val terms = mutableMapOf("1" to "0", "2" to "1")
    }

    init {
        // 全程平均绩点：从 GPA 排名接口获取（getGpaRankFromHtml → GpaRankInfo.gpa）
        snapshotFlow { gpaRankInfo }
            .onEach { info ->
                totalGradePointAverage = info?.gpa?.let { "%.2f".format(it) } ?: "暂无"
                refreshTermAndYearGPA()
            }
            .launchIn(viewModelScope)

        // 学期 + 学年绩点：从 grade 学期列表中取
        snapshotFlow { grade }
            .onEach { refreshTermAndYearGPA() }
            .launchIn(viewModelScope)

        snapshotFlow { schoolYear to schoolTerm }
            .onEach { refreshTermAndYearGPA() }
            .launchIn(viewModelScope)

        gpaRankInfo = if (AHUCache.getMockData()) null else AHUCache.getGpaRankInfo()
    }

    private fun refreshTermAndYearGPA() {
        val g = grade ?: return
        if (schoolYear == null || schoolTerm == null) return
        termGradePointAverage = g.termGradeList
            ?.find { it.schoolYear == schoolYear && it.term == schoolTerm }
            ?.termGradePointAverage
            ?: "暂无"
    }
}
