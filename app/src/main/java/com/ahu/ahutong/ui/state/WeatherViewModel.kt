package com.ahu.ahutong.ui.state

import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.weather.WeatherApi
import com.ahu.ahutong.data.weather.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

data class WeatherHomeConfig(
    val showOnHome: Boolean = false,
    val showTemp: Boolean = true,
    val showWeather: Boolean = true,
    val showAqi: Boolean = true,
    val showLocation: Boolean = true,
)

class WeatherViewModel : ViewModel() {

    var weather by mutableStateOf<WeatherResponse?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var lastCity by mutableStateOf<String?>(null)
        private set

    var lastAdcode by mutableStateOf<String?>(null)
        private set

    var lastLocationName by mutableStateOf<String?>(null)
        private set

    var homeConfig by mutableStateOf(WeatherHomeConfig())
        private set

    init {
        homeConfig = WeatherHomeConfig(showOnHome = AHUCache.getWeatherShowOnHome())
    }

    fun fetchWeather(city: String? = null) {
        if (isLoading) return
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val result = WeatherApi.API.getWeather(city = city)
                weather = result
                lastCity = city
                lastAdcode = null
                Log.d("Weather", "Fetched weather for city=$city, temp=${result.temperature}")
            } catch (e: Exception) {
                Log.e("Weather", "Failed to fetch weather", e)
                errorMessage = e.message ?: "获取天气失败"
            } finally {
                isLoading = false
            }
        }
    }

    fun refresh() {
        if (lastAdcode != null) {
            fetchWeatherByAdcode(lastAdcode!!)
        } else {
            fetchWeather(lastCity)
        }
    }

    private fun fetchWeatherByAdcode(adcode: String) {
        if (isLoading) return
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val result = WeatherApi.API.getWeather(adcode = adcode)
                weather = result
                lastAdcode = adcode
                lastCity = null
                result.adcode?.let { AHUCache.saveWeatherAdcode(it) }
                Log.d("Weather", "Fetched weather for adcode=$adcode, temp=${result.temperature}")
            } catch (e: Exception) {
                Log.e("Weather", "Failed to fetch weather by adcode", e)
                errorMessage = e.message ?: "获取天气失败"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * 通过 GPS 定位获取天气（比 IP 定位精准）
     * 降级策略：GPS → 网络定位 → Geocoder 反查城市名 → 调用 API
     */
    fun fetchWeatherByLocation(context: Context) {
        if (isLoading) return
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val cachedAdcode = AHUCache.getWeatherAdcode()
                if (cachedAdcode != null) {
                    // 有缓存的区级 adcode，直接用
                    Log.d("Weather", "Using cached adcode: $cachedAdcode")
                    val result = WeatherApi.API.getWeather(adcode = cachedAdcode)
                    weather = result
                    lastAdcode = cachedAdcode
                    lastCity = null
                    // 刷新缓存
                    result.adcode?.let { AHUCache.saveWeatherAdcode(it) }
                    return@launch
                }

                // 无缓存，GPS 定位获取城市名
                val city = withContext(Dispatchers.IO) { getCityNameFromGps(context) }
                if (city != null) {
                    Log.d("Weather", "GPS located city: $city")
                    lastCity = city
                    lastAdcode = null
                    lastLocationName = city
                    val result = WeatherApi.API.getWeather(city = city)
                    weather = result
                    // 保存 adcode 供后续精准查询
                    result.adcode?.let { AHUCache.saveWeatherAdcode(it) }
                } else {
                    Log.w("Weather", "GPS failed, fallback to IP")
                    val result = WeatherApi.API.getWeather()
                    weather = result
                    result.adcode?.let { AHUCache.saveWeatherAdcode(it) }
                }
            } catch (e: Exception) {
                Log.e("Weather", "Failed to fetch weather by GPS", e)
                try {
                    val result = WeatherApi.API.getWeather()
                    weather = result
                    errorMessage = null
                } catch (e2: Exception) {
                    errorMessage = e2.message ?: "获取天气失败"
                }
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * GPS 定位 → Geocoder 反查城市名
     * 尝试获取区级名称（locality = 蜀山区），否则市（subAdminArea = 合肥市）
     */
    private fun getCityNameFromGps(context: Context): String? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = runCatching {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }.getOrNull() ?: runCatching {
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }.getOrNull() ?: return null

        val geocoder = Geocoder(context, Locale.CHINA)
        val addresses = runCatching {
            geocoder.getFromLocation(location.latitude, location.longitude, 1)
        }.getOrNull() ?: return null

        val address = addresses.firstOrNull() ?: return null
        // 优先市名（API 更稳定），区名做 fallback
        return address.subAdminArea ?: address.locality
    }

    fun updateHomeConfig(config: WeatherHomeConfig) {
        homeConfig = config
        AHUCache.saveWeatherShowOnHome(config.showOnHome)
    }

    val locationName: String
        get() {
            // 优先 GPS 反查的区位名
            lastLocationName?.let { return it }
            val w = weather ?: return ""
            return listOfNotNull(w.district, w.city, w.province)
                .firstOrNull { it.isNotBlank() } ?: ""
        }
}
