package com.ahu.ahutong.data.crawler.model.jwxt

data class GetFreeRoomsRequest(
    val buildingId: String = "",
    val campusId: String,
    val dateTimeSegmentCmd: DateTimeSegmentCmd,
    val hasDataPermission: Boolean = false,
    val roomId: String = "",
    val seatsForLessonGte: String = ""
)

data class DateTimeSegmentCmd(
    val endDateTime: String,
    val endTime: String = "",
    val startDateTime: String,
    val startTime: String = "",
    val units: List<String>,
    val weekdays: List<String> = emptyList()
)
