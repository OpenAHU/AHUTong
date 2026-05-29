package com.ahu.ahutong.data.server

import com.ahu.ahutong.data.server.model.ApkUpdateInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApkUpdatePolicyTest {
    private val sha256 = "ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789"

    @Test
    fun validateAcceptsTrustedHttpsUpdateWithSha256() {
        val result = ApkUpdatePolicy.validate(updateInfo(), currentVersionCode = 1)

        assertTrue(result.isSuccess)
        assertEquals(sha256.lowercase(), result.getOrThrow().sha256)
        assertEquals("https://openahu.org/download/app.apk", result.getOrThrow().downloadUrl)
    }

    @Test
    fun validateRejectsMissingSha256() {
        val result = ApkUpdatePolicy.validate(updateInfo(sha256 = null), currentVersionCode = 1)

        assertTrue(result.isFailure)
    }

    @Test
    fun validateAcceptsMissingUpdateFlagWhenVersionIsNewer() {
        val result = ApkUpdatePolicy.validate(updateInfo(update = null), currentVersionCode = 1)

        assertTrue(result.isSuccess)
    }

    @Test
    fun validateRejectsExplicitDisabledUpdate() {
        val result = ApkUpdatePolicy.validate(updateInfo(update = false), currentVersionCode = 1)

        assertTrue(result.isFailure)
    }

    @Test
    fun validateResolvesTrustedRelativeDownloadUrl() {
        val result = ApkUpdatePolicy.validate(
            updateInfo(url = "/download/app.apk"),
            currentVersionCode = 1
        )

        assertTrue(result.isSuccess)
        assertEquals("https://openahu.org/download/app.apk", result.getOrThrow().downloadUrl)
    }

    @Test
    fun validateRejectsHttpDownloadUrl() {
        val result = ApkUpdatePolicy.validate(
            updateInfo(url = "http://openahu.org/download/app.apk"),
            currentVersionCode = 1
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun validateRejectsUntrustedDownloadHost() {
        val result = ApkUpdatePolicy.validate(
            updateInfo(url = "https://example.com/download/app.apk"),
            currentVersionCode = 1
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun validateRejectsNonNewerVersion() {
        val result = ApkUpdatePolicy.validate(updateInfo(versionCode = 1), currentVersionCode = 1)

        assertTrue(result.isFailure)
    }

    private fun updateInfo(
        update: Boolean? = true,
        versionCode: Int = 2,
        url: String? = "https://openahu.org/download/app.apk",
        sha256: String? = this.sha256
    ) = ApkUpdateInfo(
        update = update,
        force = false,
        versionCode = versionCode,
        versionName = "2.0.0",
        changelog = "Fixes",
        url = url,
        sha256 = sha256,
        signature = null,
        alg = null,
        note = null
    )
}
