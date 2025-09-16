package com.ahu.ahutong.data.model

import java.io.Serializable

data class ElectricityChargeInfo(
    val totalAmount: Double,
    val firstChargeDate: String
) : Serializable