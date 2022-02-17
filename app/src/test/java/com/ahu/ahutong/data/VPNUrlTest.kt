package com.ahu.ahutong.data

import com.ahu.ahutong.data.reptile.Constants
import com.ahu.ahutong.data.reptile.utils.VpnURL
import org.junit.Test

class VPNUrlTest {
    @Test
    fun testGetPlaintUrl() {
        val plaintUrl = VpnURL.getPlaintUrl(Constants.URL_TEACH_EXAM)
        println(plaintUrl)
    }
}