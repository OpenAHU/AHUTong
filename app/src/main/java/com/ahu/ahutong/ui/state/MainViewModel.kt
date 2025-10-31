package com.ahu.ahutong.ui.state

import android.webkit.CookieManager
import androidx.lifecycle.ViewModel
import com.ahu.ahutong.data.dao.AHUCache

/**
 * @Author: SinkDev
 * @Date: 2021/8/9-上午9:34
 * @Email: 468766131@qq.com
 */
class MainViewModel : ViewModel() {

    fun logout() {
        AHUCache.logout()
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
    }
}
