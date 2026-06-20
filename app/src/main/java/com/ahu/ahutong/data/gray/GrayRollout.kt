package com.ahu.ahutong.data.gray

import java.security.MessageDigest

object GrayRollout {
    fun isEnabled(rolloutPercentage: Int, bucket: Int): Boolean {
        val normalizedPercentage = rolloutPercentage.coerceIn(0, 100)
        val normalizedBucket = bucket.coerceIn(0, 99)
        return normalizedBucket < normalizedPercentage
    }

    fun bucket(featureKey: String, subjectKey: String): Int {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("$featureKey:$subjectKey".toByteArray(Charsets.UTF_8))
        val value = digest.take(4).fold(0) { acc, byte ->
            (acc shl 8) or (byte.toInt() and 0xff)
        }
        return Math.floorMod(value, 100)
    }
}
