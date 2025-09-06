package com.ahu.ahutong.data.crawler.model.ycard

class BathroomPayRequest (
    orderId: String,
    plaintext: String,
) : Request() {

    val uuid = "da07e4442e4841cca1655cb29653a023"
    val mapString = "1690457382"
    val plainDigits = "0123456789"


    val keymap = mapString.mapIndexed { index, c ->
        c.toString() to plainDigits[index].toString()
    }.toMap()

    val cipherText = plaintext.map { ch ->
        keymap[ch.toString()] ?: ch.toString()
    }.joinToString("")


    init {
        addParams(
            mapOf(
                "orderid" to orderId,
                "paystep" to "2",
                "paytype" to "ACCOUNTTSM",
                "paytypeid" to "64",
                "userAgent" to "h5",
                "ccctype" to "000",
                "password" to cipherText,
                "uuid" to uuid,
                "isWX" to "0"
            )
        )
    }
}