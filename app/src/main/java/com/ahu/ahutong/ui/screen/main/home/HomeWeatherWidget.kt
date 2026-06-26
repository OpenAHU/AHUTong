package com.ahu.ahutong.ui.screen.main.home

import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.weather.WeatherApi
import com.ahu.ahutong.data.weather.WeatherResponse
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun HomeWeatherWidget(onClick: () -> Unit) {
    val showOnHome = remember { AHUCache.getWeatherShowOnHome() }
    if (!showOnHome) return

    val context = LocalContext.current
    var weather by remember { mutableStateOf<WeatherResponse?>(null) }
    var hasError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val city = withContext(Dispatchers.IO) { getCityFromLocation(context) }
                weather = WeatherApi.API.getWeather(city = city)
            } catch (_: Exception) {
                hasError = true
            }
        }
    }

    AnimatedVisibility(
        visible = weather != null && !hasError,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val w = weather!!
        val rainKeywords = listOf("雨", "雪", "雹")
        val containsRain = { s: String? -> s != null && rainKeywords.any { s.contains(it) } }
        val rainPossible = containsRain(w.weather) ||
            w.forecast?.firstOrNull()?.let { containsRain(it.weatherDay) || containsRain(it.weatherNight) } == true ||
            w.hourlyForecast?.take(6)?.any { containsRain(it.weather) } == true

        val isSunny = w.weather?.let { it.contains("晴") && !it.contains("多云") } == true
        val highUv = (w.uv ?: 0.0) >= 5.0

        // Wind arrow: points in the direction wind is GOING
        val windArrow = when (w.windDirection) {
            "北风" -> "\u2193"
            "南风" -> "\u2191"
            "东风" -> "\u2190"
            "西风" -> "\u2192"
            "东北风" -> "\u2199"
            "西北风" -> "\u2198"
            "东南风" -> "\u2196"
            "西南风" -> "\u2197"
            else -> when {
                (w.windDirection ?: "").contains("北") && (w.windDirection ?: "").contains("东") -> "\u2199"
                (w.windDirection ?: "").contains("北") && (w.windDirection ?: "").contains("西") -> "\u2198"
                (w.windDirection ?: "").contains("南") && (w.windDirection ?: "").contains("东") -> "\u2196"
                (w.windDirection ?: "").contains("南") && (w.windDirection ?: "").contains("西") -> "\u2197"
                (w.windDirection ?: "").contains("北") -> "\u2193"
                (w.windDirection ?: "").contains("南") -> "\u2191"
                (w.windDirection ?: "").contains("东") -> "\u2190"
                (w.windDirection ?: "").contains("西") -> "\u2192"
                else -> "\u2198"
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = 90.a1 withNight 30.a1)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: main weather info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = listOfNotNull(w.district, w.city).firstOrNull() ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        color = 50.n1 withNight 80.n1
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${w.temperature?.toInt() ?: "--"}°",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Light,
                            color = 0.n1 withNight 100.n1
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = w.weather ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = 0.n1 withNight 100.n1
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    if (w.windDirection != null || w.windPower != null) {
                        Text(
                            text = "$windArrow ${w.windDirection ?: ""} ${w.windPower ?: ""}".trim(),
                            style = MaterialTheme.typography.bodySmall,
                            color = 50.n1 withNight 80.n1
                        )
                    }
                    if (w.aqi != null) {
                        val aqiEmoji = when (w.aqiLevel) {
                            1 -> "\uD83D\uDFE2"
                            2 -> "\uD83D\uDFE1"
                            3 -> "\uD83D\uDFE0"
                            4 -> "\uD83D\uDD34"
                            5 -> "\uD83D\uDFE3"
                            else -> "\u26AA"
                        }
                        Text(
                            text = "$aqiEmoji 空气${w.aqi} ${w.aqiCategory ?: ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = 50.n1 withNight 80.n1
                        )
                    }
                }

                // Right: tips
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    if (rainPossible) {
                        val pops = w.hourlyForecast?.take(6)?.mapNotNull { it.pop } ?: emptyList()
                        val maxPop = if (pops.isNotEmpty()) pops.max() else 0
                        val label = when {
                            maxPop >= 60 -> "务必带伞"
                            maxPop >= 40 -> "建议带伞"
                            maxPop > 0 -> "可能降雨"
                            else -> "带伞"
                        }
                        Text("\u2614", fontSize = 24.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (maxPop > 0) "${maxPop}%" else "--",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = 80.a1 withNight 90.a1
                        )
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            color = 50.n1 withNight 80.n1
                        )
                    } else if (isSunny && highUv) {
                        Text("\u2600\uFE0F", fontSize = 24.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "UV${w.uv?.toInt() ?: "--"}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = 80.a1 withNight 90.a1
                        )
                        Text(
                            text = "防晒",
                            fontSize = 12.sp,
                            color = 50.n1 withNight 80.n1
                        )
                    }
                }
            }
        }
    }
}

private fun getCityFromLocation(context: Context): String? {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null

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
    return address.locality ?: address.subAdminArea
}
