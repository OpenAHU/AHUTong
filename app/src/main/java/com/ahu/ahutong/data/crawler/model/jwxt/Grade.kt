package com.ahu.ahutong.data.crawler.model.jwxt


data class Grade(
    val courseGrade: CourseGrade
)

data class GradeResponse(
    val semesterId2studentGrades: Map<String, List<CourseGrade>>
)


data class CourseGrade(
    val compulsory: Boolean,
    val courseCode: String,
    val courseName: String,
    val courseNameEn: String,
    val courseProperty: String,
    val courseTaxon: String,
    val courseType: String,
    val credits: Double,
    val fillAGrace: Any,
    val gaGrade: String,
    val gp: Double,
    val gradeDetail: String,
    val id: Int,
    val lessonCode: String,
    val lessonName: String,
    val minorCourseCode: Any,
    val minorCourseCredits: Any,
    val minorCourseName: Any,
    val minorCourseNameEn: Any,
    val passed: Boolean,
    val published: Boolean,
    val semesterId: Int,
    val semesterName: String
)