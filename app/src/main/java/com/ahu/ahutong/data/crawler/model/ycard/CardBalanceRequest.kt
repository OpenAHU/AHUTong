package com.ahu.ahutong.data.crawler.model.ycard

import com.ahu.ahutong.data.crawler.utils.generateNonce
import com.ahu.ahutong.data.crawler.utils.getTimestamp
import com.ahu.ahutong.data.crawler.utils.sha256

class CardBalanceRequest(
    tranamt: String,
    yktcard: String
) : Request() {

    init {
        val appId = "56321"
        val time = getTimestamp()
        val nonce = generateNonce()
        val signType = "SHA256"
        val feeitemid = "401"
        val source = "app"
        val synAccessSource = "h5"

        addParam("feeitemid", feeitemid)
        addParam("appid", appId)
        addParam("tranamt", tranamt)
        addParam("source", source)
        addParam("yktcard", yktcard)
        addParam("synAccessSource", synAccessSource)
        addParam("APP_ID", appId)
        addParam("TIMESTAMP", time)
        addParam("SIGN_TYPE", signType)
        addParam("NONCE", nonce)

        addParam("SIGN",
            sha256("APP_ID=$appId&NONCE=$nonce&SIGN_TYPE=$signType&TIMESTAMP=$time&appid=$appId&feeitemid=$feeitemid&source=$source&synAccessSource=$synAccessSource&tranamt=$tranamt&yktcard=$yktcard&SECRET_KEY=0osTIhce7uPvDKHz6aa67bhCukaKoYl4").uppercase(),
        )
    }

    }