package com.ahu.ahutong.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.crawler.model.ycard.CardBalanceRequest
import com.ahu.ahutong.data.crawler.model.ycard.CardInfo
import com.ahu.ahutong.data.crawler.model.ycard.CardPayRequest
import com.ahu.ahutong.data.crawler.model.ycard.PayResponse
import com.ahu.ahutong.ext.launchSafe
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class CardBalanceDepositViewModel : ViewModel() {

    val TAG = "CardBalanceDepositViewModel"

    private val _cardInfo = MutableStateFlow<CardInfo?>(null)

    val cardInfo: StateFlow<CardInfo?> = _cardInfo

    private var token = ""


    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState

    fun load() = viewModelScope.launchSafe {
        _cardInfo.value = AHURepository.getCardInfo().data
    }


    fun charge(value: String) = viewModelScope.launchSafe {

        withContext(Dispatchers.IO) {

            _paymentState.value = PaymentState.Loading

            val accountInfo = cardInfo.value?.data?.card?.getOrNull(0)?.accinfo?.getOrNull(0)

            accountInfo?.let {

                val request = CardBalanceRequest(value,it.type)

                val response = AHURepository.getOrderThirdData(request)

                val regex = Regex("[?]orderid=([^&]+)")
                val match = regex.find(response.data.raw().request.url.toString())
                val target = match?.groupValues?.get(1)

                target?.let {

                    val request = CardPayRequest(it)

                    try {
                        val response = AHURepository.pay(request).data
                        val payResponse: PayResponse? = response.body()?.let { body ->
                            val jsonString = body.string()
                            Gson().fromJson(jsonString, PayResponse::class.java)
                        }

                        payResponse?.let {
                            if (it.code == 200) {
                                _paymentState.value = PaymentState.Success(it.data)
                                load()
                                return@withContext
                            } else {
                                _paymentState.value = PaymentState.Error(it.msg)
                                return@withContext
                            }
                        }


                    } catch (e: Exception) {
                        _paymentState.value = PaymentState.Error("异常: ${e.message}")
                        return@withContext
                    }

                }
                _paymentState.value = PaymentState.Error("异常: 未获取到订单号")
                return@withContext
            }
            _paymentState.value = PaymentState.Error("异常: 未获取到用户信息")
            return@withContext
        }


    }



    fun resetPaymentState() {
        _paymentState.value = PaymentState.Idle
    }


}


sealed class PaymentState {
    object Idle : PaymentState()
    object Loading : PaymentState()
    data class Success(val orderId: String) : PaymentState()
    data class Error(val message: String) : PaymentState()
}