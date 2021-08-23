package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.model.Grade
import kotlinx.coroutines.launch
import java.util.*

class GradeViewModel : ViewModel() {
    val totalGradePointAverage = MutableLiveData<String>()
    val termGradePointAverage = MutableLiveData<String>()
    val yearGradePointAverage = MutableLiveData<String>()
    val result = MutableLiveData<Result<Grade>>()
    var schoolYear = MutableLiveData<String>(schoolYears[0])
    var schoolTerm = MutableLiveData<String>(terms.keys.toTypedArray()[0])
    var grade: Grade? = null

    fun getGarde(isRefresh: Boolean = false) = viewModelScope.launch {
        result.value = AHURepository.getGrade(isRefresh)
    }

    companion object {
        val schoolYears by lazy {
            val result = mutableListOf<String>()
            val year = Calendar.getInstance().get(Calendar.YEAR)
            for (i in 0..6) {
                result.add("${year - i}-${year - i + 1}")
            }
            result.toTypedArray()
        }
        val terms by lazy {
            val result = mutableMapOf("1" to "0", "2" to "1")
            result
        }
    }
}