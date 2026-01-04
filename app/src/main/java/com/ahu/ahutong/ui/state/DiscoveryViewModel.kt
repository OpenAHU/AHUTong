package com.ahu.ahutong.ui.state

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.crawler.api.adwmh.AdwmhApi
import com.ahu.ahutong.ext.launchSafe
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.journeyapps.barcodescanner.BarcodeEncoder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

/**
 * @Author Simon
 * @Date 2021/8/3-22:12
 * @Email 330771794@qq.com
 */


@HiltViewModel
class DiscoveryViewModel @Inject constructor() : ViewModel() {

    val TAG = DiscoveryViewModel::class.java.simpleName

    val bathroom = mutableStateMapOf<String, String>()
    var balance by mutableStateOf(0.0)
    var transitionBalance by mutableStateOf(0.0)

    val visibilities = mutableStateListOf<Int>()


    var qrcode = MutableStateFlow<Bitmap?>(null)
    var state = MutableStateFlow<Boolean>(false);

    fun loadActivityBean() {
        viewModelScope.launchSafe {

            AHURepository.getCardMoney().onSuccess {
                balance = it.balance ?: 0.0
//                transitionBalance = it.transitionBalance
            }

            AHURepository.getBathRooms().onSuccess {
                it.stream().forEach {
                    bathroom += it.bathroom to it.openStatus
                }
            }


        }
    }

    fun loadQrCode() {
        viewModelScope.launchSafe {
            withContext(Dispatchers.IO){
                state.value = false
                try {
                    val response = withContext(Dispatchers.IO) {
                        AdwmhApi.API.getQrcode()
                    }
                    if (response.code == 10000) {
                        val hints = HashMap<EncodeHintType, Any>()

                        hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.L
                        hints[EncodeHintType.MARGIN] = 1
                        val encoder = BarcodeEncoder()
                        qrcode.value = encoder.encodeBitmap(
                            response.`object`,
                            BarcodeFormat.QR_CODE,
                            400,
                            400,
                            hints
                        )
                    } else {
                        Log.e("QR", "接口返回错误: ${response.msg}")
                    }
                } catch (e: IOException) {
                    Log.e("QR", "网络异常", e)
                } catch (e: Exception) {
                    Log.e("QR", "未知异常", e)
                }
                state.value = true
            }
        }

    }

}
