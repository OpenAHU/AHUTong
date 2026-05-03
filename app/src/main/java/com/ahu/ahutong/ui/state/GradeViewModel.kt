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
import com.google.gson.Gson
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.log

class GradeViewModel : ViewModel() {
    var totalGradePointAverage by mutableStateOf("暂无")
    var termGradePointAverage by mutableStateOf("暂无")
    var yearGradePointAverage by mutableStateOf("暂无")
    var grade by mutableStateOf<Grade?>(null)
    var schoolYear by mutableStateOf(schoolYears.firstOrNull())
    var schoolTerm by mutableStateOf(terms.keys.firstOrNull())
    var errorMessage by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)
    // ===================== 【GPA 排名数据】 =====================
    var gpaRankInfo by mutableStateOf<GpaRankInfo?>(null)
    var rankLoading by mutableStateOf(false)

    // ===================== 【加载 GPA 排名】 =====================
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
        val schoolYears by lazy {
            (AHUCache.getCurrentUser()
                ?: throw IllegalStateException("未登录，无法打开成绩界面！")).getSchoolYears()
        }
        val terms = mutableMapOf("1" to "0", "2" to "1")
    }

    init {
        snapshotFlow { grade }
            .onEach { grade1 ->
                totalGradePointAverage = grade1?.totalGradePointAverage ?: "暂无"
                grade1?.let {
                    calGPA(schoolYear!!, schoolTerm!!, it)
                }
            }
            .launchIn(viewModelScope)

        snapshotFlow { schoolYear to schoolTerm }
            .onEach { (schoolYear, schoolTerm) ->
                grade?.let {
                    calGPA(schoolYear!!, schoolTerm!!, it)
                }
            }
            .launchIn(viewModelScope)
        gpaRankInfo = AHUCache.getGpaRankInfo()
    }

    private fun calGPA(schoolYear: String, schoolTerm: String, grade: Grade) {
        var yearGrade = 0f
        var termTotal = 0f
        termGradePointAverage = "暂无"
        yearGradePointAverage = "暂无"
        for (termGrade in grade.termGradeList ?: emptyList()) {
            if (termGrade.schoolYear == schoolYear) {
                yearGrade += (termGrade.termGradePointAverage.toFloat() * termGrade.termGradePoint.toFloat())
                termTotal += termGrade.termGradePoint.toFloat()
                if (termGrade.term == schoolTerm) {
                    termGradePointAverage = termGrade.termGradePointAverage
                }
            }
        }
        if (termTotal != 0f) {
            val gradePointAverage = yearGrade / termTotal
            val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.HALF_UP
            yearGradePointAverage = df.format(gradePointAverage)
        }
    }
}
