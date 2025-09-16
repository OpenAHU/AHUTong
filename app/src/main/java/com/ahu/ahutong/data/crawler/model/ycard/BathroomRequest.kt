package com.ahu.ahutong.data.crawler.model.ycard

class BathroomRequest(
    bathroom: String,
    amount: String,
    thirdPartyJson: String
) : Request() {

    init {
        var feeitemid :String? = null
        when(bathroom){
            "竹园/龙河"->{
                feeitemid = "409"
            }
            "桔园/蕙园"->{
                feeitemid = "430"
            }
            else -> {
                throw IllegalArgumentException("Unknown bathroom.")
            }
        }

        feeitemid.let{
            addParams(
                mapOf(
                    "feeitemid" to it,
                    "tranamt" to amount,
                    "flag" to "choose",
                    "source" to "app",
                    "paystep" to "0",
                    "abstracts" to "",
                    "third_party" to thirdPartyJson
                )
            )
        }


    }
}