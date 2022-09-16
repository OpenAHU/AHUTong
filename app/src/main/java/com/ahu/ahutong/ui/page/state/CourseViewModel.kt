package com.ahu.ahutong.ui.page.state

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import arch.sink.utils.Utils
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.data.model.Course

/**
 * @Author: SinkDev
 * @Date: 2021/8/26-下午3:02
 * @Email: 468766131@qq.com
 */
class CourseViewModel : ViewModel() {
    val course = MutableLiveData<Course>()

    fun addCourse(course: Course) {
        val schoolTerm = AHUCache.getSchoolTerm()
        val schoolYear = AHUCache.getSchoolYear()
        if (schoolYear == null || schoolTerm == null) {
            Toast.makeText(Utils.getApp(), "当前未填写当前日期,请填写后重试", Toast.LENGTH_SHORT).show()
            return
        }
        val newSchedule = mutableListOf<Course>()
        AHUCache.getSchedule(schoolYear, schoolTerm)?.forEach {
            // courseId 作为课程的标识
            if (!it.courseId.equals(course.courseId)) {
                newSchedule.add(it)
            }
        }
        newSchedule.add(course)
        AHUCache.saveSchedule(schoolYear, schoolTerm, newSchedule)
    }

    fun removeCourse(course: Course) {
        val schoolTerm = AHUCache.getSchoolTerm()
        val schoolYear = AHUCache.getSchoolYear()
        if (schoolYear == null || schoolTerm == null) {
            Toast.makeText(Utils.getApp(), "当前未填写当前日期,请填写后重试", Toast.LENGTH_SHORT).show()
            return
        }
        val schedule = AHUCache.getSchedule(schoolYear, schoolTerm)?.filter {
            it != course
        }
        AHUCache.saveSchedule(schoolYear, schoolTerm, schedule ?: emptyList())
    }
}
