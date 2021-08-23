package com.ahu.ahutong.ui.page.state

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.Exam
import kotlinx.coroutines.launch

class ExamViewModel:ViewModel() {
    val data  = MutableLiveData<Result<List<Exam>>>()
    val size = MutableLiveData(0)
    fun loadExam(isRefresh: Boolean = false) = viewModelScope.launch{
        val schoolTerm = AHUCache.getSchoolTerm()
        val schoolYear = AHUCache.getSchoolYear()
        if (schoolYear == null || schoolTerm == null){
            data.value = Result.failure(Throwable("未填写当前学年，学期。"))
            return@launch
        }
        data.value = AHURepository.getExamInfo(schoolYear, schoolTerm, isRefresh)
    }
}