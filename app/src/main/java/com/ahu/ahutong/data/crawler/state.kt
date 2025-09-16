package com.ahu.ahutong.data.crawler

sealed class PayState {
    object Idle : PayState()
    object InProgress : PayState()
    data class Failed(val message: String) : PayState()
    data class Succeeded(val message: String) : PayState()
}