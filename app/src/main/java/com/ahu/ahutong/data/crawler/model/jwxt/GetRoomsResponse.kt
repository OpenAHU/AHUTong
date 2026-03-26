package com.ahu.ahutong.data.crawler.model.jwxt

class GetRoomsResponse : ArrayList<GetRoomsResponseItem>()

data class GetRoomsResponseItem(
    val code: String,
    val enabled: Boolean,
    val experiment: Boolean,
    val floor: Int,
    val id: Int,
    val nameEn: Any,
    val nameZh: String,
    val seatsForLesson: Int,
    val virtual: Boolean
)