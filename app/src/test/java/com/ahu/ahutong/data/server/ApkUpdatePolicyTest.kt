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
    fun validateNormalizesMissingDisplayFields() {
        val result = ApkUpdatePolicy.validate(
            updateInfo(versionName = null, changelog = null),
            currentVersionCode = 1
        )

        assertTrue(result.isSuccess)
        assertEquals("2", result.getOrThrow().info.versionName)
        assertEquals("", result.getOrThrow().info.changelog)
    }

    @Test
    fun validateRejectsExplicitDisabledUpdate() {
        val result = ApkUpdatePolicy.validate(updateInfo(update = false), currentVersionCode = 1)

        assertTrue(result.isFailure)
    }

    @Test
    fun validateRejectsInvalidRemoteVersion() {
        val result = ApkUpdatePolicy.validate(updateInfo(versionCode = 0), currentVersionCode = 1)

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
    fun validateRejectsMirrorDownloadHostByDefault() {
        val result = ApkUpdatePolicy.validate(
            updateInfo(url = "https://openahu-ahutong-cn.muxyang.com/download/app.apk"),
            currentVersionCode = 1
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun validateAcceptsMirrorDownloadHostOnlyWhenExplicitlyAllowed() {
        val result = ApkUpdatePolicy.validateDownloadUrl(
            rawUrl = "https://openahu-ahutong-cn.muxyang.com/download/app.apk",
            allowMirrorHost = true
        )

        assertTrue(result.isSuccess)
        assertEquals("https://openahu-ahutong-cn.muxyang.com/download/app.apk", result.getOrThrow())
    }

    @Test
    fun mirrorDownloadUrlReplacesOnlyHost() {
        val result = ApkUpdatePolicy.mirrorDownloadUrl(
            "https://openahu.org/download/app.apk?channel=stable"
        )

        assertTrue(result.isSuccess)
        assertEquals(
            "https://openahu-ahutong-cn.muxyang.com/download/app.apk?channel=stable",
            result.getOrThrow()
        )
    }

    @Test
    fun validateResolvesRedirectLocationAgainstCurrentDownloadUrl() {
        val result = ApkUpdatePolicy.validateDownloadUrl(
            rawUrl = "next.apk",
            baseUrl = "https://openahu.org/download/ahutong.apk"
        )

        assertTrue(result.isSuccess)
        assertEquals("https://openahu.org/download/next.apk", result.getOrThrow())
    }

    @Test
    fun validateRejectsRedirectLocationToUntrustedHost() {
        val result = ApkUpdatePolicy.validateDownloadUrl(
            rawUrl = "//example.com/download/app.apk",
            baseUrl = "https://openahu.org/download/ahutong.apk"
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
        versionName: String? = "2.0.0",
        changelog: String? = "Fixes",
        url: String? = "https://openahu.org/download/app.apk",
        sha256: String? = this.sha256
    ) = ApkUpdateInfo(
        update = update,
        force = false,
        versionCode = versionCode,
        versionName = versionName,
        changelog = changelog,
        url = url,
        sha256 = sha256,
        signature = null,
        alg = null,
        note = null
    )
}
