package com.ahu.ahutong.data.crawler.model.jwxt

data class GetFreeRoomsResponse(
    val roomList: List<FreeRoom>
)

data class FreeRoom(
    val building: FreeBuilding,
    val code: String,
    val date: Any,
    val floor: Int,
    val id: Int,
    val mngtDepartAssoc: Int,
    val nameEn: Any,
    val nameZh: String,
    val remark: String?,
    val roomType: FreeRoomType,
    val seats: Int,
    val seatsForLesson: Int,
    val units: Any,
    val virtual: Boolean,
    val week: Any,
    val weekNum: Any,
    val weekday: Any
)

data class FreeBuilding(
    val campus: FreeCampus,
    val code: String,
    val id: Int,
    val nameEn: Any,
    val nameZh: String
)

data class FreeRoomType(
    val code: String,
    val id: Int,
    val nameEn: Any,
    val nameZh: String
)

data class FreeCampus(
    val code: String,
    val id: Int,
    val nameEn: Any,
    val nameZh: String
)