package com.ahu.ahutong.data.gray

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GrayRolloutTest {
    @Test
    fun bucketIsStableForSameFeatureAndSubject() {
        val first = GrayRollout.bucket("feature_a", "user:1001")
        val second = GrayRollout.bucket("feature_a", "user:1001")

        assertEquals(first, second)
    }

    @Test
    fun bucketFallsInsideHundredBuckets() {
        val bucket = GrayRollout.bucket("feature_a", "user:1001")

        assertTrue(bucket in 0..99)
    }

    @Test
    fun rolloutPercentageUsesBucketBoundary() {
        assertFalse(GrayRollout.isEnabled(0, 0))
        assertTrue(GrayRollout.isEnabled(1, 0))
        assertTrue(GrayRollout.isEnabled(100, 99))
        assertFalse(GrayRollout.isEnabled(50, 50))
    }
}
