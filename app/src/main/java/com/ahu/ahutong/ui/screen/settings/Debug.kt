package com.ahu.ahutong.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ahu.ahutong.data.crawler.manager.CookieManager
import com.ahu.ahutong.data.crawler.manager.TokenManager
import com.ahu.ahutong.data.crawler.model.ycard.Token
import com.ahu.ahutong.data.dao.AHUCache
import com.tencent.bugly.crashreport.CrashReport


@Composable
fun Debug() {

    Column(modifier = Modifier.padding(30.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

        Button(onClick = {
            CookieManager.cookieJar.clearCookiesForUrl("https://adwmh.ahu.edu.cn/")
        }) {
            Text(text = "清除Adwmh cookie")
        }

        Button(onClick = {
            CookieManager.cookieJar.clear()
            TokenManager.clear()
        }) {
            Text(text = "清除所有cookie & token")
        }

        Button(onClick = {
            AHUCache.clearAll()
        }) {
            Text(text = "清除缓存")
        }

        Divider()


        Button(onClick = {
            CrashReport.testJavaCrash();
        }) {
            Text(text = "测试Bugly")
        }
    }
}