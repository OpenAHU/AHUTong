package com.ahu.ahutong.data.crawler.model.jwxt

data class CurrentSemester(
    val approvedYear: String,
    val calendarAssoc: CalendarAssoc,
    val code: String,
    val countInTerm: Boolean,
    val enabled: Boolean,
    val endDate: EndDate,
    val fileInfoAssoc: Any,
    val id: Int,
    val includeMonths: List<Int>,
    val name: String,
    val nameEn: String,
    val nameZh: String,
    val schoolYear: String,
    val season: Season,
    val startDate: StartDate,
    val transient: Boolean,
    val weekStartOnSunday: Boolean
)

data class CalendarAssoc(
    val id: Int
)

data class EndDate(
    val centuryOfEra: Int,
    val chronology: Chronology,
    val dayOfMonth: Int,
    val dayOfWeek: Int,
    val dayOfYear: Int,
    val era: Int,
    val fieldTypes: List<FieldType>,
    val fields: List<Field>,
    val monthOfYear: Int,
    val values: List<Int>,
    val weekOfWeekyear: Int,
    val weekyear: Int,
    val year: Int,
    val yearOfCentury: Int,
    val yearOfEra: Int
)

data class Season(
    val `$name`: String,
    val `$type`: String
)

data class StartDate(
    val centuryOfEra: Int,
    val chronology: Chronology,
    val dayOfMonth: Int,
    val dayOfWeek: Int,
    val dayOfYear: Int,
    val era: Int,
    val fieldTypes: List<FieldType>,
    val fields: List<Field>,
    val monthOfYear: Int,
    val values: List<Int>,
    val weekOfWeekyear: Int,
    val weekyear: Int,
    val year: Int,
    val yearOfCentury: Int,
    val yearOfEra: Int
)

data class Chronology(
    val zone: Zone
)

data class FieldType(
    val durationType: DurationType,
    val name: String,
    val rangeDurationType: RangeDurationType
)

data class Field(
    val durationField: DurationField,
    val leapDurationField: LeapDurationField,
    val lenient: Boolean,
    val maximumValue: Int,
    val minimumValue: Int,
    val name: String,
    val rangeDurationField: RangeDurationField,
    val supported: Boolean,
    val type: TypeXXX,
    val unitMillis: Int
)

data class Zone(
    val ID: String,
    val fixed: Boolean
)

data class DurationType(
    val name: String
)

data class RangeDurationType(
    val name: String
)

data class DurationField(
    val name: String,
    val precise: Boolean,
    val supported: Boolean,
    val type: Type,
    val unitMillis: Long
)

data class LeapDurationField(
    val name: String,
    val precise: Boolean,
    val supported: Boolean,
    val type: Type,
    val unitMillis: Int
)

data class RangeDurationField(
    val name: String,
    val precise: Boolean,
    val supported: Boolean,
    val type: Type,
    val unitMillis: Long
)

data class TypeXXX(
    val durationType: DurationType,
    val name: String,
    val rangeDurationType: RangeDurationType
)

data class Type(
    val name: String
)