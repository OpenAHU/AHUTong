package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.Grade
import com.ahu.ahutong.ext.getSchoolYears
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import java.util.*

class GradeViewModel : ViewModel() {
    val totalGradePointAverage = MutableLiveData<String>()
    val termGradePointAverage = MutableLiveData<String>()
    val yearGradePointAverage = MutableLiveData<String>()
    val result = MutableLiveData<Result<Grade>>()
    var schoolYear = MutableLiveData(schoolYears[0])
    var schoolTerm = MutableLiveData(terms.keys.toTypedArray()[0])
    var grade: Grade? = null

    fun getGarde(isRefresh: Boolean = false) = viewModelScope.launch {
        result.value = AHURepository.getGrade(isRefresh)
    }

    companion object {
        val schoolYears by lazy {
            (AHUCache.getCurrentUser()
                ?: throw IllegalStateException("未登录，无法打开成绩界面！"))
                .getSchoolYears()
        }
        val terms by lazy {
            val result = mutableMapOf("1" to "0", "2" to "1")
            result
        }
    }
}