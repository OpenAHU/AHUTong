package com.ahu.ahutong.ui.state

import androidx.lifecycle.ViewModel
import com.ahu.ahutong.data.model.License

/**
 * @Author: SinkDev
 * @Date: 2021/8/26-下午6:30
 * @Email: 468766131@qq.com
 */
class LicenseViewModel : ViewModel() {
    // TODO: complete
    val license by lazy {
        listOf(
            License(
                "AndroidX",
                "Google",
                "https://source.android.com",
                "Apache Software License 2.0"
            ),
            License(
                "Material",
                "Google",
                "https://source.android.com",
                "Apache Software License 2.0"
            ),
            License(
                "Gson",
                "Google",
                "https://github.com/google/gson",
                "Apache Software License 2.0"
            ),
            License(
                "Okhttp",
                "Square",
                "https://github.com/square/okhttp",
                "Apache Software License 2.0"
            ),
            License(
                "Retrofit",
                "Square",
                "https://github.com/square/retrofit",
                "Apache Software License 2.0"
            ),
            License(
                "Jsoup",
                "jsoup.org",
                "https://jsoup.org/",
                "MIT License."
            ),
            License(
                "MMKV",
                "Tencent",
                "https://github.com/Tencent/MMKV",
                "BSD 3-Clause License."
            ),
            License(
                "Coil",
                "Coil Contributors",
                "https://github.com/coil-kt/coil",
                "Apache Software License 2.0"
            ),
            License(
                "PersistentCookieJar",
                "Fran Montiel ",
                "https://github.com/franmontiel/PersistentCookieJar",
                "Apache Software License 2.0"
            ),
            License(
                "Accompanist",
                "Google",
                "https://github.com/google/accompanist",
                "Apache Software License 2.0"
            ),
            License(
                "ZXing Android Embedded",
                "JourneyApps",
                "https://github.com/journeyapps/zxing-android-embedded",
                "Apache Software License 2.0"
            ),
            License(
                "Monet",
                "Kyant0",
                "https://github.com/Kyant0/Monet",
                "Apache Software License 2.0"
            )

        )
    }
}
