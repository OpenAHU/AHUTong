package com.ahu.ahutong.ui.state

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.crawler.PayState
import com.ahu.ahutong.data.crawler.api.ycard.YcardApi
import com.ahu.ahutong.data.crawler.model.ycard.BathroomPayRequest
import com.ahu.ahutong.data.crawler.model.ycard.BathroomRequest
import com.ahu.ahutong.data.crawler.model.ycard.PayResponse
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.BathroomTelInfo
import com.ahu.ahutong.ext.launchSafe
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.FormBody

class BathroomDepositViewModel: ViewModel() {

    val TAG = "BathroomDepositViewModel"

    private  val _info = MutableStateFlow<AHUResponse<BathroomTelInfo>?>(null)

    val info: StateFlow<AHUResponse<BathroomTelInfo>?> = _info

    var _payState = MutableStateFlow<PayState>(PayState.Idle)

    val payState : StateFlow<PayState> = _payState

    fun resetPaymentState() {
        _payState.value = PayState.Idle
    }

    fun getBathroomInfo(bathroom:String,tel: String){
        viewModelScope.launchSafe {
            withContext(Dispatchers.IO){
                _info.value = AHURepository.getBathroomInfo(bathroom = bathroom,tel = tel)
            }
        }
    }



    val paymentSuccessEvent = MutableLiveData<Unit>()
    fun pay(bathroom:String,amount: String,password: String){

        _payState.value = PayState.InProgress
        paymentSuccessEvent.value = Unit

        if(info.value == null)
            return

        viewModelScope.launchSafe {
            withContext(Dispatchers.Default){
                info.value!!.data.map!!.data?.let{ //????
                    val data = it
                    data.myCustomInfo = "手机号：${data.telPhone}"

                    val thirdPartyJson = Gson().toJson(data)

                    val request = BathroomRequest(bathroom,amount,thirdPartyJson)


                    var res = AHURepository.pay(request).data
                    val jsonString = res.body()!!.string()

                    val regex = """"orderid"\s*:\s*"([^"]+)"""".toRegex()
                    val match = regex.find(jsonString)
                    val orderId = match?.groups?.get(1)?.value


                    orderId?.let{ orderId ->
                        val payRequest = BathroomPayRequest(orderId,password)
                        val res = AHURepository.pay(payRequest)


                        val payResponse: PayResponse? = res.data.body()?.string()?.let {
                            Gson().fromJson(it, PayResponse::class.java)
                        }

                        if(payResponse?.code == 200){
                            _info.value = AHURepository.getBathroomInfo(bathroom = bathroom,tel = data.telPhone)
                            AHUCache.savePhone(it.telPhone)
                            _payState.value = PayState.Succeeded(message = payResponse.data)
                        }else{
                            _payState.value = PayState.Failed(message = payResponse?.msg?:"未知错误")
                        }

                    }



                }
            }

        }
    }

}


