package com.ahu.ahutong.data.crawler.model.ycard

import com.ahu.ahutong.data.crawler.utils.generateNonce
import com.ahu.ahutong.data.crawler.utils.getTimestamp
import com.ahu.ahutong.data.crawler.utils.sha256

class CardPayRequest(orderId: String) : Request() {

    init {
        val time = getTimestamp()
        val nonce = generateNonce()
        val appId = "56321"
        val payStep = "2"
        val payType = "BANKCARD"
        val payTypeId = "63"
        val redirectUrl = "https://ycard.ahu.edu.cn/payment/?name=result"
        val userAgent = "h5"
        val synAccessSource = "h5"

        addParams(
            mapOf(
                "paytypeid" to payTypeId,
                "paytype" to payType,
                "paystep" to payStep,
                "orderid" to orderId,
                "redirect_url" to redirectUrl,
                "userAgent" to userAgent,
                "APP_ID" to appId,
                "TIMESTAMP" to time,
                "SIGN_TYPE" to "SHA256",
                "NONCE" to nonce,
                "SIGN" to sha256("APP_ID=56321&NONCE=$nonce&SIGN_TYPE=SHA256&TIMESTAMP=$time&orderid=$orderId&paystep=2&paytype=BANKCARD&paytypeid=63&redirect_url=https://ycard.ahu.edu.cn/payment/?name=result&userAgent=h5&SECRET_KEY=0osTIhce7uPvDKHz6aa67bhCukaKoYl4").uppercase(),
                "synAccessSource" to synAccessSource
            )
        )
    }

}
