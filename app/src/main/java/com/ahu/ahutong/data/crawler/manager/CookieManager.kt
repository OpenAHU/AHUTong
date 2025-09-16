package com.ahu.ahutong.data.crawler.manager

import arch.sink.utils.Utils
import com.ahu.ahutong.data.api.AHUCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor

object CookieManager {


    val cookieJar = AHUCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(Utils.getApp()))

}