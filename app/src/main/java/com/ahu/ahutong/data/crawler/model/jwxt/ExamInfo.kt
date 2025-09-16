package com.ahu.ahutong.data.crawler.model.jwxt

class ExamInfo : ArrayList<ExamInfoItem>()

data class ExamInfoItem(
    val course: CourseInExam,
    val examStatus: Any,
    val examStatusPublished: Boolean,
    val examStatusStr: Any,
    val examTime: String,
    val examType: ExamType,
    val examViolateStatus: Any,
    val finished: Boolean,
    val id: Int,
    val requiredCampus: RequiredCampus,
    val room: String,
    val seatNo: Int,
    val violation: Any
)


data class CourseInExam(
    val id: Int,
    val nameEn: String,
    val nameZh: String,
)


data class ExamBatchAssoc(
    val id: Int
)


data class ExamType(
    //val bizTypeAssocs: List<BizTypeAssoc>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: String,
    val nameZh: String,
    val transient: Boolean
)

data class RequiredCampus(
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val nameEn: Any,
    val nameZh: String
)

data class Student(
    val aboardCultivateMethod: Any,
    val aboardScholarshipCode: Any,
    val aboardStdType: Any,
    val aboardStudentRegisterCode: Any,
    val adminclass: Adminclass,
    val attackStudyType: Any,
    val attendTag: Any,
    val bizType: BizType,
    val campus: Campus,
    val code: String,
    val cultivateType: CultivateType,
    val department: DepartmentInExam,
    val disciplineCategory: Any,
    val education: Education,
    val enterSchoolDate: EnterSchoolDate,
    val enterSchoolGrade: Int,
    val feeType: Any,
    val firstDiscipline: Any,
    val firstTutor: Any,
    val grade: String,
    val graduate: Graduate,
    val hasXueJi: Boolean,
    val id: Int,
    val inSchool: Boolean,
    val instructors: List<Instructor>,
    val lastStdAlterType: Any,
    val lastStdAlterTypeId: Any,
    val major: Major,
    val majorDirection: Any,
    val mngtDepartment: MngtDepartment,
    val needRegister: Boolean,
    val person: PersonXXXXXXXXX,
    val plaType: Any,
    val portraitFileInfo: Any,
    val predictEnterDate: Any,
    val program: Program,
    val regPortraitFileInfo: Any,
    val researchDirection: Any,
    val retakeTag: Any,
    val retirement: Boolean,
    val secondDiscipline: Any,
    val secondTutor: Any,
    val stdStatus: StdStatus,
    val stdType: StdType,
    val studentRecordType: Any,
    val studyForm: StudyForm,
    val studyMethod: Any,
    val studyYears: Double,
    val tags: Any,
    val zaiJi: Boolean
)

data class BelongBizType(
    val id: Int,
    val nameEn: String,
    val nameZh: String
)

data class CourseManager(
    val createdBy: Any,
    val createdDateTime: Any,
    val teacher: TeacherInExam,
    val updatedBy: Any,
    val updatedDateTime: Any
)

data class CourseProperty(
    //val bizTypeAssocs: List<BizTypeAssoc>,
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

data class CourseTaxonInExam(
    //val bizTypeAssocs: List<BizTypeAssoc>,
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


data class DefaultExamMode(
    //val bizTypeAssocs: List<BizTypeAssoc>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: String,
    val nameZh: String,
    val transient: Boolean
)

data class DefaultOpenDepart(
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
    val recruitTypeSet: List<RecruitTypeSet>,
    val recruitTypes: String,
    val research: Boolean,
    val telephone: Any
)

data class Education(
    //val bizTypeAssocs: List<BizTypeAssoc>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: Any,
    val nameZh: String,
    val transient: Boolean
)

data class MngtDepartment(
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
    val recruitTypeSet: List<RecruitTypeSet>,
    val recruitTypes: String,
    val research: Boolean,
    val telephone: Any
)


data class StageInfoInExam(
    val stage: Boolean,
    val stageGrantNum: Any,
    val stageGrantWeight: Any,
    val stageGrantWeightList: List<Any>,
    val stageNum: Any
)

data class SuitableBizType(
    val id: Int,
    val nameEn: String,
    val nameZh: String
)

data class TeachLang(
    //val bizTypeAssocs: List<BizTypeAssoc>,
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

data class TeacherInExam(
    val belongSeries: Any,
    val code: String,
    val country: CountryInExam,
    val degreeType: DegreeTypeInExam,
    val department: DepartmentInExam,
    val empTerm: Any,
    val hireType: HireType,
    val id: Int,
    val major: Any,
    val nameZh: Any,
    val periodInfo: PeriodInfo,
    val person: Person,
    val personnelDepartment: String,
    val preEducation: PreEducation,
    val researchDepartment: Any,
    val researchDirection: String,
    val rsZaiZhi: String,
    val source: Any,
    val standing: Any,
    val teacherCert: Any,
    val teacherCertNo: Any,
    val teaching: Boolean,
    val thesisDepartments: Any,
    val title: Title,
    val titleDate: TitleDate,
    val type: TypeXXXXXXXX,
    val undertake: Any,
    val undertakeType: Any,
    val vehicleInfo: Any,
    val workUnit: Any,
    val workUnitType: Any,
    val zaiZhi: Boolean
)

data class CountryInExam(
    //val bizTypeAssocs: List<BizTypeAssoc>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: Any,
    val nameZh: String,
    val transient: Boolean
)

data class DegreeTypeInExam(
    //val bizTypeAssocs: List<BizTypeAssoc>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: Any,
    val nameZh: String,
    val transient: Boolean
)

data class DepartmentInExam(
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
    val recruitTypeSet: List<RecruitTypeSet>,
    val recruitTypes: String,
    val research: Boolean,
    val telephone: Any
)

data class HireType(
    val `$name`: String,
    val `$type`: String
)

data class PersonInExam(
    val eleSignature: String,
    val id: Int,
    val nameEn: String,
    val nameZh: String
)

data class PreEducationInExam(
    //val bizTypeAssocs: List<BizTypeAssoc>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: Any,
    val nameZh: String,
    val transient: Boolean
)

data class TitleInExam(
    //val bizTypeAssocs: List<BizTypeAssoc>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: Any,
    val nameZh: String,
    val specialistPositionLevelAssoc: SpecialistPositionLevelAssoc,
    val transient: Boolean
)

data class TitleDate(
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

data class TypeXXXXXXXX(
    //val bizTypeAssocs: List<BizTypeAssoc>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: Any,
    val nameZh: String,
    val transient: Boolean
)

data class RecruitTypeSet(
    val `$name`: String,
    val `$type`: String
)

data class EnrollDate(
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

data class TeacherX(
    val id: Int
)
data class SpecialistPositionLevelAssoc(
    val id: Int
)

data class BizType(
    val id: Int,
    val nameEn: String,
    val nameZh: String
)

data class ExamBatchType(
    val `$name`: String,
    val `$type`: String
)

data class Semester(
    val calendar: Calendar,
    val code: String,
    val countInTerm: Boolean,
    val enabled: Boolean,
    val endDate: EndDate,
    val fileInfo: Any,
    val id: Int,
    val nameEn: String,
    val nameZh: String,
    val schoolYear: String,
    val season: Season,
    val startDate: StartDate,
    val weekStartOnSunday: Boolean
)

data class Calendar(
    val id: Int,
    val nameEn: Any,
    val nameZh: String
)


data class ExamTime(
    val buildingOrderType: Any,
    val dateTimeString: String,
    val endTime: Int,
    val endTimeString: String,
    val examBatchAssoc: ExamBatchAssoc,
    val examDate: ExamDate,
    val examDateString: String,
    val examTakeCount: Int,
    val examWeek: Int,
    val floorOrderType: Any,
    val id: Int,
    val name: String,
    val startTime: Int,
    val startTimeString: String,
    val timeString: String,
    val weekday: Int
)

data class Lesson(
    val allowDelay: Boolean,
    val allowMakeup: Boolean,
    val arrangeExamType: String,
    val campus: Campus,
    val code: String,
//    val course: CourseX,
//    val courseProperty: CourseProperty,
    val courseType: CourseType,
    val examDuration: Any,
    val examMethod: ExamMethod,
    val examMode: ExamMode,
    val id: Int,
    val nameEn: Any,
    val nameZh: String,
    val openDepartment: OpenDepartment,
    val planExamWeek: Any,
    val scheduleEndWeek: Int,
    val semesterAssoc: SemesterAssoc,
    val teacherAssignmentList: List<TeacherAssignmentInExam>
)

data class ExamDate(
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

data class Campus(
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val nameEn: Any,
    val nameZh: String
)

data class CourseX(
    val allowDelay: Boolean,
    val allowExempt: Any,
    val allowMakeUp: Boolean,
    val calculateGp: Boolean,
    val claim: Any,
    val code: String,
    val credits: Int,
    val design: Boolean,
    val enabled: Boolean,
    val exemptRemark: Any,
    val experiment: Boolean,
    val extra: Boolean,
    val extraCredits: Any,
    val flag: Any,
    val flags: List<Any?>,
    val id: Int,
    val machine: Boolean,
    val nameEn: String,
    val nameZh: String,
    val periodInfo: PeriodInfoX,
    val platformLink: Any,
    val practice: Boolean,
    val remark: Any,
    val stageInfo: StageInfoInExam,
    val test: Boolean,
    val theory: Boolean
)

data class ExamMethod(
    //val bizTypeAssocs: List<BizTypeAssoc>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: String,
    val nameZh: String,
    val transient: Boolean
)


data class SemesterAssoc(
    val id: Int
)

data class TeacherAssignmentInExam(
    val age: Int,
    val indexNo: Int,
    val person: Person,
    val role: Role,
    val teacher: TeacherInExam
)

data class Role(
    val `$name`: String,
    val `$type`: String
)

class GroupAndArrangeGroup2ExamTakeCount

data class MajorMonitor(
    val major: Boolean,
    val staff: Staff
)

data class MinorMonitor(
    val major: Boolean,
    val staff: Staff
)

data class Room(
    val bizTypes: List<BizType>,
    val building: Building,
    val code: String,
    val enabled: Boolean,
    val experiment: Boolean,
    val floor: Int,
    val id: Int,
    val mngtDepart: MngtDepartX,
    val nameEn: Any,
    val nameZh: String,
    val remark: Any,
    val roomType: RoomType,
    val seats: Int,
    val seatsForLesson: Int,
    val usableDeparts: List<UsableDepart>,
    val virtual: Boolean
)

data class SeatMap(
    val columns: Int,
    val map: String,
    val rows: Int
)

data class Staff(
    val belongSeries: Any,
    val campusIdSet: List<Any?>,
    val campuses: List<Any?>,
    val canBeMonitor: Boolean,
    val canBeSupervisor: Boolean,
    val code: String,
    val department: DepartmentInExam,
    val examStaffType: ExamStaffType,
    val freeStaff: Boolean,
    val id: Int,
    val major: Any,
    val person: PersonXXX,
    val teacherDepartment: TeacherDepartment,
    val teaching: Boolean,
    val title: TitleXX,
    val type: TypeXXXXXXXX,
    val zaiZhi: Boolean
)

data class ExamStaffType(
    val `$name`: String,
    val `$type`: String
)

data class PersonXXX(
    val country: CountryInExam,
    val gender: Gender,
    val id: Int,
    val idCardNumber: String,
    val idCardType: IdCardType,
    val nameEn: String,
    val nameZh: String,
    val nation: Nation,
    val nativePlace: Any,
    val politicalVisage: PoliticalVisage
)

data class TeacherDepartment(
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
    val recruitTypeSet: List<RecruitTypeSet>,
    val recruitTypes: String,
    val research: Boolean,
    val telephone: Any
)

data class TitleXX(
    val bizTypes: List<BizType>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val indexNo: Any,
    val nameEn: Any,
    val nameZh: String,
    val specialistPositionLevel: SpecialistPositionLevel
)

data class Gender(
    //val bizTypeAssocs: List<BizTypeAssoc>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: String,
    val nameZh: String,
    val transient: Boolean
)

data class IdCardType(
    //val bizTypeAssocs: List<BizTypeAssoc>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: Any,
    val nameZh: String,
    val transient: Boolean
)

data class Nation(
    //val bizTypeAssocs: List<BizTypeAssoc>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: Any,
    val nameZh: String,
    val transient: Boolean
)

data class PoliticalVisage(
    //val bizTypeAssocs: List<BizTypeAssoc>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: String,
    val nameZh: String,
    val transient: Boolean
)

data class SpecialistPositionLevel(
    //val bizTypeAssocs: List<BizTypeAssoc>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: Any,
    val nameZh: String,
    val specLevel: Int,
    val transient: Boolean
)

data class Building(
    val bizTypes: List<BizType>,
    val campus: Campus,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val mngtDepart: MngtDepart,
    val nameEn: Any,
    val nameZh: String,
    val remark: Any
)

data class MngtDepartX(
    val abbrEn: Any,
    val abbrZh: String,
    val address: Any,
    val code: String,
    val college: Boolean,
    val collegeAbbr: String,
    val experiment: Boolean,
    val id: Int,
    val indexNo: Int,
    val nameEn: Any,
    val nameZh: String,
    val openCourse: Boolean,
    val recruitTypeSet: List<RecruitTypeSet>,
    val recruitTypes: String,
    val research: Boolean,
    val telephone: Any
)

data class RoomType(
    //val bizTypeAssocs: List<BizTypeAssoc>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: Any,
    val nameZh: String,
    val transient: Boolean
)

data class UsableDepart(
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
    val recruitTypeSet: List<RecruitTypeSet>,
    val recruitTypes: String,
    val research: Boolean,
    val telephone: Any
)

data class MngtDepart(
    val abbrEn: Any,
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
    val recruitTypeSet: List<RecruitTypeSet>,
    val recruitTypes: String,
    val research: Boolean,
    val telephone: Any
)

data class FieldXXXXXXXX(
    val durationField: DurationField,
    val leapDurationField: LeapDurationField,
    val lenient: Boolean,
    val maximumValue: Int,
    val minimumValue: Int,
    val name: String,
    val range: Int,
    val rangeDurationField: RangeDurationField,
    val supported: Boolean,
    val type: TypeXXX,
    val unitMillis: Int
)


data class Adminclass(
    val abbrEn: Any,
    val abbrZh: Any,
    val code: String,
    val enabled: Boolean,
    val grade: String,
    val id: Int,
    val nameEn: Any,
    val nameZh: String,
    val planCount: Int,
    val remark: Any,
    val stdCount: Int
)

data class CultivateType(
    //val bizTypeAssocs: List<BizTypeAssoc>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: String,
    val nameZh: String,
    val transient: Boolean
)

data class EnterSchoolDate(
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

data class Graduate(
    val afterGraduateDir: Any,
    val certificateCode: Any,
    val college: Any,
    val collegeEn: Any,
    val confirmCompanyName: Any,
    val deadlineDate: Any,
    val deliveryDate: Any,
    val expectedGraduateDate: ExpectedGraduateDate,
    val graduateDate: Any,
    val id: Int,
    val major: Any,
    val majorEn: Any,
    val needAudit: Boolean,
    val portraitFileInfo: Any,
    val principalName: Any,
    val principalNameEn: Any,
    val remark: Any,
    val result: Any,
    val school: Any,
    val schoolCode: Any,
    val schoolEn: Any,
    val student: StudentX
)

data class Instructor(
    val id: Any,
    val instructorType: InstructorType,
    val instructors: List<InstructorX>,
    val nameEn: Any,
    val nameZh: Any,
    val studentId: Any
)

data class Major(
    val abbrEn: Any,
    val abbrZh: String,
    val chargeTeacherCodes: String,
    val chargeTeacherNames: String,
    val childMajorAssocs: List<Any>,
    val code: String,
    val establishYear: Any,
    val id: Int,
    val majorCertificate: Any,
    val nameEn: String,
    val nameZh: String,
    val parent: Boolean,
    val parentMajorAssocs: List<Any>,
    val relatedMajors: String,
    val reportCode: String,
    val schoolingYear: Any,
    val shuntTime: Any,
    val studyYears: Double
)

data class PersonXXXXXXXXX(
    val activeSoldier: Any,
    val age: Int,
    val ancestralHome: Any,
    val arrivalTime: Any,
    val backHomeNumber: Any,
    val bank: Any,
    val bankAccount: Any,
    val birthPlace: Any,
    val birthday: Birthday,
    val character: Any,
    val chinaPrefectureCity: Any,
    val contactInfo: ContactInfo,
    val country: Any,
    val countryType: Any,
    val eleSignature: Any,
    val family: Family,
    val formerName: Any,
    val gender: Gender,
    val graduatedSchool: Any,
    val healthStatus: Any,
    val hobby: Any,
    val id: Int,
    val idCardNumber: String,
    val idCardType: IdCardType,
    val joinLeagueDatePlace: Any,
    val joinPartyDatePlace: Any,
    val leaderPositionLevel: Any,
    val maritalStatus: Any,
    val nameEn: String,
    val namePinyin: String,
    val nameZh: String,
    val nation: Nation,
    val nativePlace: Any,
    val permanentResident: Boolean,
    val politicalVisage: PoliticalVisage,
    val portrait: Any,
    val religiousBelief: Any,
    val remark: String,
    val residence: Any,
    val residenceNature: Any,
    val specialistPositionLevel: Any,
    val specialty: Any,
    val stdLeaders: Boolean,
    val studyExperiences: List<Any?>,
    val trainStation: Any,
    val trainStationChinaPlace: Any,
    val volunteerExperience: Any,
    val workCompany: Any,
    val workExperience: Any,
    val workExperiences: List<Any?>
)

data class Program(
    val auditState: AuditState,
    val currentNode: String,
    val enabled: Boolean,
    val grade: String,
    val id: Int,
    val nameEn: Any,
    val nameZh: String,
    val type: TypeXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
)

data class StdStatus(
    //val bizTypeAssocs: List<BizTypeAssoc>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: String,
    val nameZh: String,
    val transient: Boolean
)

data class StdType(
    //val bizTypeAssocs: List<BizTypeAssoc>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: String,
    val nameZh: String,
    val transient: Boolean
)

data class StudyForm(
    //val bizTypeAssocs: List<BizTypeAssoc>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: Any,
    val nameZh: String,
    val transient: Boolean
)

data class ExpectedGraduateDate(
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

data class StudentX(
    val aboardScholarshipCode: Any,
    val aboardStudentRegisterCode: Any,
    val attendTag: Any,
    val code: String,
    val enterSchoolDate: EnterSchoolDate,
    val enterSchoolGrade: Int,
    val grade: String,
    val hasXueJi: Boolean,
    val id: Int,
    val inSchool: Boolean,
    val lastStdAlterTypeId: Any,
    val needRegister: Boolean,
    val predictEnterDate: Any,
    val researchDirection: Any,
    val retakeTag: Any,
    val retirement: Boolean,
    val studentRecordType: Any,
    val studyYears: Double,
    val zaiJi: Boolean
)

data class InstructorType(
    //val bizTypeAssocs: List<BizTypeAssoc>,
    val bizTypeIds: List<Int>,
    val code: String,
    val enabled: Boolean,
    val id: Int,
    val name: String,
    val nameEn: String,
    val nameZh: String,
    val transient: Boolean
)

data class InstructorX(
    val instructor: InstructorXX,
    val orderNumber: Int
)

data class InstructorXX(
    val belongSeries: Any,
    val code: String,
    val department: DepartmentInExam,
    val empTerm: Any,
    val hireType: HireType,
    val id: Int,
    val nameZh: Any,
    val person: PersonXXXXXXXX,
    val personnelDepartment: String,
    val researchDirection: Any,
    val rsZaiZhi: String,
    val source: Any,
    val standing: Any,
    val teacherCert: Any,
    val teacherCertNo: Any,
    val teaching: Boolean,
    val title: Title,
    val titleDate: TitleDate,
    val type: TypeXXXXXXXX,
    val undertake: Any,
    val undertakeType: Any,
    val vehicleInfo: Any,
    val workUnit: Any,
    val workUnitType: Any,
    val zaiZhi: Boolean
)

data class PersonXXXXXXXX(
    val eleSignature: Any,
    val id: Int,
    val nameEn: String,
    val nameZh: String
)

data class Birthday(
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

data class ContactInfo(
    val address: Any,
    val dormAddress: Any,
    val dormPhone: Any,
    val email: String,
    val emergencyContact: Any,
    val emergencyPhone: Any,
    val mobile: String,
    val postcode: Any,
    val qq: Any,
    val telephone: String,
    val wechat: Any
)

data class Family(
    val contactInfo: ContactInfoX,
    val id: Any,
    val members: List<Any?>,
    val nameEn: Any,
    val nameZh: Any,
    val personAssoc: PersonAssoc
)

data class ContactInfoX(
    val address: Any,
    val dormAddress: Any,
    val dormPhone: Any,
    val email: Any,
    val emergencyContact: Any,
    val emergencyPhone: Any,
    val mobile: Any,
    val postcode: Any,
    val qq: Any,
    val telephone: Any,
    val wechat: Any
)

data class PersonAssoc(
    val id: Int
)

data class AuditState(
    val `$name`: String,
    val `$type`: String
)

data class TypeXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX(
    val `$name`: String,
    val `$type`: String
)


