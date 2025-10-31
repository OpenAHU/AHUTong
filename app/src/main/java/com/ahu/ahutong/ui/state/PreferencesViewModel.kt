package com.ahu.ahutong.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.data.dao.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreferencesViewModel @Inject constructor(private val preferencesManager: PreferencesManager): ViewModel() {

    private val _showQRCode = MutableStateFlow(false)
    val showQRCode: StateFlow<Boolean> = _showQRCode.asStateFlow()

    private val _isShowAllCourse = MutableStateFlow(false)
    val isShowAllCourse: StateFlow<Boolean> = _isShowAllCourse.asStateFlow()


    init {
        viewModelScope.launch {
            preferencesManager.showQRCode.collect {
                _showQRCode.value = it
            }


        }
        viewModelScope.launch {
            preferencesManager.isShowAllCourse.collect {
                _isShowAllCourse.value = it
            }
        }
    }

    fun setShowQRCode(value : Boolean){
        viewModelScope.launch {
            preferencesManager.setShowQRCode(value)
        }
    }

    fun setIsShowAllCourse(value: Boolean){
        viewModelScope.launch {
            preferencesManager.setIsShowAllCourse(value)
        }
    }

}