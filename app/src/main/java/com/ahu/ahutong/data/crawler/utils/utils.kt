package com.ahu.ahutong.data.crawler.utils

import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getTimestamp(): String {
    val formatter = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
    return formatter.format(Date())
}


fun generateNonce(length: Int = 11): String {
    val charset = "0123456789abcdefghijklmnopqrstuvwxyz"
    val secureRandom = SecureRandom()
    return buildString {
        repeat(length) {
            val index = secureRandom.nextInt(charset.length)
            append(charset[index])
        }
    }
}


fun sha256(input: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}
