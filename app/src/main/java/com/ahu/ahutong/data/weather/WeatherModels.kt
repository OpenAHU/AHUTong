package com.ahu.ahutong.data.weather

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val province: String?,
    val city: String?,
    val district: String?,
    val adcode: String?,
    val weather: String?,
    @SerializedName("weather_icon")
    val weatherIcon: String?,
    val temperature: Double?,
    @SerializedName("wind_direction")
    val windDirection: String?,
    @SerializedName("wind_power")
    val windPower: String?,
    val humidity: Int?,
    @SerializedName("report_time")
    val reportTime: String?,

    // extended=true
    @SerializedName("feels_like")
    val feelsLike: Double?,
    val visibility: Double?,
    val pressure: Double?,
    val uv: Double?,
    val precipitation: Double?,
    val cloud: Int?,
    val aqi: Int?,
    @SerializedName("aqi_level")
    val aqiLevel: Int?,
    @SerializedName("aqi_category")
    val aqiCategory: String?,
    @SerializedName("aqi_primary")
    val aqiPrimary: String?,
    @SerializedName("air_pollutants")
    val airPollutants: AirPollutants?,
    val alerts: List<WeatherAlert>?,

    // forecast=true
    @SerializedName("temp_max")
    val tempMax: Double?,
    @SerializedName("temp_min")
    val tempMin: Double?,
    val forecast: List<ForecastDay>?,

    // hourly=true
    @SerializedName("hourly_forecast")
    val hourlyForecast: List<HourlyForecast>?,

    // indices=true
    @SerializedName("life_indices")
    val lifeIndices: LifeIndices?
)

data class AirPollutants(
    val pm25: Double?,
    val pm10: Double?,
    val o3: Double?,
    val no2: Double?,
    val so2: Double?,
    val co: Double?
)

data class WeatherAlert(
    val title: String?,
    val type: String?,
    val level: String?,
    val text: String?,
    @SerializedName("publish_time")
    val publishTime: String?,
    val publisher: String?,
    val guidance: List<String>?
)

data class ForecastDay(
    val date: String?,
    val week: String?,
    @SerializedName("temp_max")
    val tempMax: Double?,
    @SerializedName("temp_min")
    val tempMin: Double?,
    @SerializedName("weather_day")
    val weatherDay: String?,
    @SerializedName("weather_night")
    val weatherNight: String?,
    @SerializedName("wind_dir_day")
    val windDirDay: String?,
    @SerializedName("wind_dir_night")
    val windDirNight: String?,
    @SerializedName("wind_scale_day")
    val windScaleDay: String?,
    @SerializedName("wind_scale_night")
    val windScaleNight: String?,
    @SerializedName("wind_speed_day")
    val windSpeedDay: Double?,
    val humidity: Int?,
    val precip: Double?,
    val visibility: Double?,
    @SerializedName("uv_index")
    val uvIndex: Int?,
    val sunrise: String?,
    val sunset: String?
)

data class HourlyForecast(
    val time: String?,
    val temperature: Double?,
    val weather: String?,
    @SerializedName("wind_direction")
    val windDirection: String?,
    @SerializedName("wind_speed")
    val windSpeed: Double?,
    @SerializedName("wind_scale")
    val windScale: String?,
    val humidity: Int?,
    val precip: Double?,
    @SerializedName("feels_like")
    val feelsLike: Double?,
    val visibility: Double?,
    val pop: Int?,
    @SerializedName("uv_index")
    val uvIndex: Int?
)

data class LifeIndices(
    val clothing: LifeIndexItem?,
    val uv: LifeIndexItem?,
    @SerializedName("car_wash")
    val carWash: LifeIndexItem?,
    val drying: LifeIndexItem?,
    @SerializedName("air_conditioner")
    val airConditioner: LifeIndexItem?,
    @SerializedName("cold_risk")
    val coldRisk: LifeIndexItem?,
    val exercise: LifeIndexItem?,
    val comfort: LifeIndexItem?,
    val travel: LifeIndexItem?,
    val fishing: LifeIndexItem?,
    val allergy: LifeIndexItem?,
    val sunscreen: LifeIndexItem?,
    val mood: LifeIndexItem?,
    val beer: LifeIndexItem?,
    val umbrella: LifeIndexItem?,
    val traffic: LifeIndexItem?,
    @SerializedName("air_purifier")
    val airPurifier: LifeIndexItem?,
    val pollen: LifeIndexItem?
)

data class LifeIndexItem(
    val level: String?,
    val brief: String?,
    val advice: String?
)
