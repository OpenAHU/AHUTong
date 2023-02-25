package com.ahu.ahutong.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.Grade
import com.ahu.ahutong.ext.getSchoolYears
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class GradeViewModel : ViewModel() {
    var totalGradePointAverage by mutableStateOf("暂无")
    var termGradePointAverage by mutableStateOf("暂无")
    var yearGradePointAverage by mutableStateOf("暂无")
    var grade by mutableStateOf<Grade?>(null)
    var schoolYear by mutableStateOf(schoolYears.firstOrNull())
    var schoolTerm by mutableStateOf(terms.keys.firstOrNull())

    fun getGarde(isRefresh: Boolean = false) = viewModelScope.launch {
        grade = AHURepository.getGrade(isRefresh).getOrNull()
    }

    companion object {
        val schoolYears by lazy {
            (AHUCache.getCurrentUser() ?: throw IllegalStateException("未登录，无法打开成绩界面！")).getSchoolYears()
        }
        val terms = mutableMapOf("1" to "0", "2" to "1")
    }

    init {
        snapshotFlow { grade }
            .onEach {
                totalGradePointAverage = it?.totalGradePointAverage?.takeUnless {
                    it == "NaN"
                } ?: "暂无"
            }
            .launchIn(viewModelScope)

        snapshotFlow { schoolYear to schoolTerm }
            .onEach { (schoolYear, schoolTerm) ->
                var yearGrade = 0f
                var termTotal = 0f
                termGradePointAverage = "暂无"
                yearGradePointAverage = "暂无"
                for (termGrade in grade?.termGradeList ?: emptyList()) {
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
            .launchIn(viewModelScope)
    }
}
