package com.ahu.ahutong.data.crawler.model.jwxt

data class CourseTable(
    val studentTableVms: List<StudentTableVm>
)

data class StudentTableVm(
    val activities: List<Activity>,
    val adminclass: String,
    val arrangedLessonSearchVms: List<ArrangedLessonSearchVm>,
    val code: String,
    val courseTablePrintConfigs: List<CourseTablePrintConfig>,
    val credits: Int,
    val department: String,
    val grade: String,
    val id: Int,
    val lessonNamePrint: Boolean,
    val lessonSearchVms: List<Any?>,
    val major: String,
    val name: String,
    val practiceWeekScheduleTexts: List<Any?>,
    val stdCountPrint: Boolean,
    val teacherCodePrint: Boolean,
    val teacherTitlePrint: Boolean,
    val timePrint: Boolean,
    val timeTableLayout: TimeTableLayout,
    val totalRetakeCredits: Int
)

data class Activity(
    val building: String,
    val campus: String,
    val courseCode: String,
    val courseName: String,
    val courseType: CourseType,
    val credits: Int,
    val endTime: String,
    val endUnit: Int,
    val endUnitName: Int,
    val groupNum: Any,
    val lessonBizType: String,
    val lessonCode: String,
    val lessonId: Int,
    val lessonName: String,
    val lessonRemark: Any,
    val limitCount: Int,
    val periodInfo: PeriodInfo,
    val room: String,
    val semesterId: Any,
    val startTime: String,
    val startUnit: Int,
    val startUnitName: Int,
    val stdCount: Int,
    val taskPeopleNum: Any,
    val teacherCodes: List<String>,
    val teacherNames: List<String>,
    val teacherTitles: List<String>,
    val teachers: List<String>,
    val weekIndexes: List<Int>,
    val weekday: Int,
    val weeksStr: String
)

data class ArrangedLessonSearchVm(
    val actualDesignPeriod: Int,
    val actualExperimentPeriod: Int,
    val actualExtraPeriod: Int,
    val actualMachinePeriod: Int,
    val actualPeriods: Int,
    val actualPracticePeriod: Int,
    val actualTestPeriod: Int,
    val actualTheoryPeriod: Int,
    val adminClasses: Any,
    val adminclassIds: List<Any?>,
    val adminclassSetup: Boolean,
    val adminclassVms: List<Any?>,
    val allowDelay: Boolean,
    val allowMakeup: Boolean,
    val arrangeExamType: String,
    val arrangeExamTypeZh: String,
    val auditLogVms: Any,
    val auditState: String,
    val auto: Boolean,
    val bizType: Any,
    val calcRelatedAdminclasses: Boolean,
    val campus: Any,
    val code: String,
    val compulsorys: List<String>,
    val compulsorysStr: String,
    val course: Course,
    val courseLevelRequireList: List<Any?>,
    val courseProperty: CoursePropertyX,
    val courseStdCount: Any,
    val courseType: CourseType,
    val crossBizTypes: List<Any?>,
    val currentNode: Any,
    val enforce: Boolean,
    val examDuration: Any,
    val examMethod: Any,
    val examMode: ExamMode,
    val expActualPeriods: Int,
    val generateSeatNumber: Boolean,
    val hasSchedule: Any,
    val id: Int,
    val jointlyCourse: Any,
    val lessonKind: String,
    val lessonKindEn: String,
    val lessonKindText: String,
    val lessonKindZh: String,
    val limitCount: Int,
    val midtermRetake: Boolean,
    val minorCourse: Any,
    val nameEn: Any,
    val nameZh: String,
    val needAssign: Boolean,
    val noAttendCount: Int,
    val openDepartment: OpenDepartment,
    val passed: Any,
    val planExamWeek: Any,
    val practiceWeekScheduleText: Any,
    val practiceWeekStr: Any,
    val practiceWeeks: List<Any?>,
    val preCourses: List<Any?>,
    val remark: Any,
    val remarkForPrint: Any,
    val requiredPeriodInfo: RequiredPeriodInfo,
    val reserveCount: Int,
    val retakeCount: Int,
    val roomType: Any,
    val scheduleAssignDepartment: ScheduleAssignDepartment,
    val scheduleChangeWeeks: List<Any?>,
    val scheduleCurrentWeek: Any,
    val scheduleEndWeek: Any,
    val scheduleRemark: Any,
    val scheduleStartWeek: Any,
    val scheduleState: String,
    val scheduleText: ScheduleText,
    val scheduleWeeksInfo: Any,
    val selectionRemark: Any,
    val semester: Any,
    val stdCount: Int,
    val submitDate: Any,
    val suggestScheduleWeeks: List<Int>,
    val suggestScheduleWeeksInfo: String,
    val tags: Any,
    val teachLang: TeachLangX,
    val teacherAssignmentList: List<TeacherAssignment>,
    val teacherAssignmentStr: String,
    val teacherAssignmentString: String,
    val teacherPeriodInfo: TeacherPeriodInfo,
    val teacherScheduleTextVms: List<Any?>,
    val timeTableLayout: Any
)

data class CourseTablePrintConfig(
    val nameEn: String,
    val nameZh: String,
    val unitGroup: List<List<Int>>
)

data class TimeTableLayout(
    val changeDayOfMonth: Any,
    val changeMonth: Any,
    val courseUnitList: List<CourseUnit>,
    val enabled: Boolean,
    val id: Int,
    val maxEndTime: Int,
    val maxIndexNo: Int,
    val minIndexNo: Int,
    val minStartTime: Int,
    val name: String,
    val nameEn: String,
    val nameZh: String,
    val transient: Boolean
)

data class CourseType(
    val bizTypeAssocs: List<Int>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: Any,
    val nameZh: String,
    val transient: Boolean
)

data class PeriodInfo(
    val design: Any,
    val designUnit: Any,
    val dispersedPractice: Any,
    val dispersedPracticeUnit: Any,
    val experiment: Int,
    val experimentUnit: String,
    val extra: Any,
    val extraUnit: Any,
    val focusPractice: Int,
    val focusPracticeUnit: String,
    val machine: Any,
    val machineUnit: Any,
    val periodsPerWeek: Int,
    val practice: Any,
    val practiceUnit: Any,
    val requireDesign: Any,
    val requireExperiment: Int,
    val requireExtra: Any,
    val requireMachine: Any,
    val requirePractice: Int,
    val requireTest: Any,
    val requireTheory: Int,
    val test: Any,
    val testUnit: Any,
    val theory: Int,
    val theoryUnit: String,
    val total: Int,
    val weeks: Double
)

data class Course(
    val allowDelay: Boolean,
    val allowExempt: Any,
    val allowMakeUp: Boolean,
    val belongBizType: Any,
    val calculateGp: Boolean,
    val claim: Any,
    val code: String,
    val courseLevelRequireList: List<Any?>,
    val courseManager: Any,
    val courseOwnership: CourseOwnership,
    val courseProperty: CoursePropertyX,
    val courseSpec: Any,
    val courseTaxon: CourseTaxon,
    val courseType: CourseType,
    val credits: Int,
    val defaultExamMode: Any,
    val defaultOpenDepart: Any,
    val defaultOpenMajor: Any,
    val defaultPreCourseList: List<Any?>,
    val defaultPreCourses: List<Any?>,
    val design: Boolean,
    val education: Any,
    val enabled: Boolean,
    val exemptRemark: Any,
    val experiment: Boolean,
    val extra: Boolean,
    val extraCredits: Any,
    val flag: String,
    val flags: List<String>,
    val grantCourseLevel: Any,
    val id: Int,
    val lessonType: String,
    val machine: Boolean,
    val minorCourses: List<Any?>,
    val mngtDepartment: Any,
    val nameEn: String,
    val nameZh: String,
    val openResearchDepartment: Any,
    val periodInfo: PeriodInfoX,
    val platformLink: Any,
    val practice: Boolean,
    val professor: Any,
    val remark: Any,
    val seasons: List<Any?>,
    val stageInfo: StageInfo,
    val suitMajors: List<Any?>,
    val suitableBizTypes: List<Any?>,
    val tags: Any,
    val teachLang: TeachLangX,
    val teachType: Any,
    val test: Boolean,
    val textbooks: List<Any?>,
    val theory: Boolean
)

data class CoursePropertyX(
    val bizTypeAssocs: List<Int>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: Any,
    val nameZh: String,
    val shortName: String,
    val transient: Boolean
)

data class ExamMode(
    val bizTypeAssocs: List<Int>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: String,
    val nameZh: String,
    val transient: Boolean
)

data class OpenDepartment(
    val abbrEn: String,
    val abbrZh: String,
    val address: Any,
    val code: String,
    val college: Boolean,
    val collegeAbbr: String,
    val experiment: Boolean,
    val id: Int,
    val indexNo: Int,
    val nameEn: String,
    val nameZh: String,
    val openCourse: Boolean,
    val recruitTypeSet: List<String>,
    val recruitTypes: String,
    val research: Boolean,
    val telephone: Any
)

data class RequiredPeriodInfo(
    val design: Any,
    val designUnit: Any,
    val dispersedPractice: Any,
    val dispersedPracticeUnit: Any,
    val experiment: Int,
    val experimentUnit: String,
    val extra: Any,
    val extraUnit: Any,
    val focusPractice: Int,
    val focusPracticeUnit: String,
    val machine: Any,
    val machineUnit: Any,
    val periodsPerWeek: Int,
    val practice: Any,
    val practiceUnit: Any,
    val requireDesign: Any,
    val requireExperiment: Int,
    val requireExtra: Any,
    val requireMachine: Any,
    val requirePractice: Int,
    val requireTest: Any,
    val requireTheory: Int,
    val test: Any,
    val testUnit: Any,
    val theory: Int,
    val theoryUnit: String,
    val total: Int,
    val weeks: Double
)

data class ScheduleAssignDepartment(
    val abbrEn: String,
    val abbrZh: String,
    val address: Any,
    val code: String,
    val college: Boolean,
    val collegeAbbr: String,
    val experiment: Boolean,
    val id: Int,
    val indexNo: Int,
    val nameEn: String,
    val nameZh: String,
    val openCourse: Boolean,
    val recruitTypeSet: List<String>,
    val recruitTypes: String,
    val research: Boolean,
    val telephone: Any
)

data class ScheduleText(
    val dateTimePlacePersonText: DateTimePlacePersonText,
    val dateTimePlaceText: DateTimePlaceText,
    val dateTimeText: DateTimeText,
    val roomSeatText: RoomSeatText
)

data class TeachLangX(
    val bizTypeAssocs: List<Int>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val indexNo: Int,
    val name: String,
    val nameEn: Any,
    val nameZh: String,
    val transient: Boolean
)

data class TeacherAssignment(
    val age: Int,
    val indexNo: Int,
    val person: Person,
    val role: String,
    val teacher: Teacher
)

data class TeacherPeriodInfo(
    val design: Any,
    val designUnit: Any,
    val dispersedPractice: Any,
    val dispersedPracticeUnit: Any,
    val experiment: Any,
    val experimentUnit: Any,
    val extra: Any,
    val extraUnit: Any,
    val focusPractice: Any,
    val focusPracticeUnit: Any,
    val machine: Any,
    val machineUnit: Any,
    val periodsPerWeek: Any,
    val practice: Any,
    val practiceUnit: Any,
    val requireDesign: Any,
    val requireExperiment: Any,
    val requireExtra: Any,
    val requireMachine: Any,
    val requirePractice: Any,
    val requireTest: Any,
    val requireTheory: Any,
    val test: Any,
    val testUnit: Any,
    val theory: Any,
    val theoryUnit: Any,
    val total: Any,
    val weeks: Any
)

data class CourseOwnership(
    val bizTypeAssocs: List<Int>,
    val bizTypeIds: List<Int>,
    val code: String,
    val courseTaxonAssoc: Int,
    val enabled: Boolean,
    val id: Int,
    val indexNo: Int,
    val name: String,
    val nameEn: String,
    val nameZh: String,
    val transient: Boolean
)

data class CourseTaxon(
    val bizTypeAssocs: List<Int>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val indexNo: Int,
    val name: String,
    val nameEn: String,
    val nameZh: String,
    val transient: Boolean
)

data class PeriodInfoX(
    val design: Any,
    val designUnit: Any,
    val dispersedPractice: Any,
    val dispersedPracticeUnit: String,
    val experiment: Int,
    val experimentUnit: String,
    val extra: Any,
    val extraUnit: Any,
    val focusPractice: Int,
    val focusPracticeUnit: String,
    val machine: Any,
    val machineUnit: Any,
    val periodsPerWeek: Int,
    val practice: Any,
    val practiceUnit: Any,
    val requireDesign: Any,
    val requireExperiment: Int,
    val requireExtra: Any,
    val requireMachine: Any,
    val requirePractice: Int,
    val requireTest: Any,
    val requireTheory: Int,
    val test: Any,
    val testUnit: Any,
    val theory: Int,
    val theoryUnit: String,
    val total: Int,
    val weeks: Double
)

data class StageInfo(
    val stage: Boolean,
    val stageGrantNum: Any,
    val stageGrantWeight: Any,
    val stageGrantWeightList: List<Any>,
    val stageNum: Any
)

data class DateTimePlacePersonText(
    val text: String,
    val textEn: String,
    val textZh: String
)

data class DateTimePlaceText(
    val text: String,
    val textEn: String,
    val textZh: String
)

data class DateTimeText(
    val text: String,
    val textEn: String,
    val textZh: String
)

data class RoomSeatText(
    val text: String,
    val textEn: String,
    val textZh: String
)

data class Person(
    val eleSignature: String,
    val id: Int,
    val nameEn: String,
    val nameZh: String
)

data class Teacher(
    val belongSeries: Any,
    val code: String,
    val country: Country,
    val degreeType: DegreeType,
    val department: Department,
    val empTerm: String,
    val hireType: String,
    val id: Int,
    val major: Any,
    val nameZh: Any,
    val periodInfo: PeriodInfoXX,
    val person: Person,
    val personnelDepartment: String,
    val preEducation: PreEducation,
    val researchDepartment: Any,
    val researchDirection: String,
    val rsZaiZhi: String,
    val source: String,
    val standing: String,
    val teacherCert: Boolean,
    val teacherCertNo: Any,
    val teaching: Boolean,
    val thesisDepartments: List<ThesisDepartment>,
    val title: Title,
    val titleDate: String,
    val type: CourseTableType,
    val undertake: String,
    val undertakeType: String,
    val vehicleInfo: Any,
    val workUnit: String,
    val workUnitType: String,
    val zaiZhi: Boolean
)

data class Country(
    val bizTypeAssocs: List<Int>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: Any,
    val nameZh: String,
    val transient: Boolean
)

data class DegreeType(
    val bizTypeAssocs: List<Int>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: Any,
    val nameZh: String,
    val transient: Boolean
)

data class Department(
    val abbrEn: String,
    val abbrZh: String,
    val address: Any,
    val code: String,
    val college: Boolean,
    val collegeAbbr: String,
    val experiment: Boolean,
    val id: Int,
    val indexNo: Int,
    val nameEn: String,
    val nameZh: String,
    val openCourse: Boolean,
    val recruitTypeSet: List<String>,
    val recruitTypes: String,
    val research: Boolean,
    val telephone: Any
)

data class PeriodInfoXX(
    val enrollDate: String,
    val leaveDate: Any,
    val officePhone: Any,
    val officePlace: String,
    val teacher: Int
)

data class PreEducation(
    val bizTypeAssocs: List<Int>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: Any,
    val nameZh: String,
    val transient: Boolean
)

data class ThesisDepartment(
    val abbrEn: String,
    val abbrZh: String,
    val address: Any,
    val code: String,
    val college: Boolean,
    val collegeAbbr: String,
    val experiment: Boolean,
    val id: Int,
    val indexNo: Int,
    val nameEn: String,
    val nameZh: String,
    val openCourse: Boolean,
    val recruitTypeSet: List<String>,
    val recruitTypes: String,
    val research: Boolean,
    val telephone: Any
)

data class Title(
    val bizTypeAssocs: List<Int>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: Any,
    val nameZh: String,
    val specialistPositionLevelAssoc: Int,
    val transient: Boolean
)

data class CourseTableType(
    val bizTypeAssocs: List<Int>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: Any,
    val nameZh: String,
    val transient: Boolean
)


data class CourseUnit(
    val changeEndTime: Int,
    val changeStartTime: Int,
    val color: String,
    val dayPart: String,
    val endTime: Int,
    val indexNo: Int,
    val name: String,
    val nameEn: String,
    val nameZh: String,
    val segmentIndex: Int,
    val startTime: Int,
    val timeTableLayoutAssoc: Int
)