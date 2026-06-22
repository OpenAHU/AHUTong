package com.ahu.ahutong.ui.state

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.weather.WeatherApi
import com.ahu.ahutong.data.weather.WeatherResponse
import kotlinx.coroutines.launch

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
        fetchWeather(lastCity)
    }

    fun updateHomeConfig(config: WeatherHomeConfig) {
        homeConfig = config
        AHUCache.saveWeatherShowOnHome(config.showOnHome)
    }

    val locationName: String
        get() {
            val w = weather ?: return ""
            return listOfNotNull(w.district, w.city, w.province)
                .firstOrNull { it.isNotBlank() } ?: ""
        }
}
