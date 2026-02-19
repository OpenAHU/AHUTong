package com.ahu.ahutong.data.model

import java.io.Serializable

data class ElectricityDepositHistoryItem(
    val selection: RoomSelectionInfo,
    val label: String,
    val updatedAt: Long
) : Serializable
