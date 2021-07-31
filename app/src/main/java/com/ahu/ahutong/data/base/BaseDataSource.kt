package com.ahu.ahutong.data.base

import com.ahu.ahutong.data.AHUResponse
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.data.model.Exam
import com.ahu.ahutong.data.model.Grade
import com.ahu.ahutong.data.model.Room

/**
 * @Author: Sink
 * @Date: 2021/7/31-下午8:40
 * @Email: 468766131@qq.com
 */
interface BaseDataSource {

    /**
     * getSchedule
     * @param schoolYear String 2020-2021
     * @param schoolTerm String 1,2
     * @return AHUResponse<List<Course>>
     */
    suspend fun getSchedule(schoolYear: String, schoolTerm: String): AHUResponse<List<Course>>

    /**
     * getExamInfo
     * @param schoolYear String 2020-2021
     * @param schoolTerm String 1,2
     * @return AHUResponse<List<Exam>>
     */

    suspend fun getExamInfo(schoolYear: String,schoolTerm: String): AHUResponse<List<Exam>>

    /**
     * 获取空教室API
     * @param campus 1为新区，2为老区
     * @param weekday 星期几
     * @param weekNum 第几周
     * @param time 1为1，2节；2为3，4节；3为5，6节；4为7，8节；5为9，10，11节；6为上午；7为下午；8为晚上；9为白天；10为整天
     * @return AHUResponse<List<Room>>
     */

    suspend fun getEmptyRoom(campus: String, weekday: String, weekNum: String, time: String): AHUResponse<List<Room>>



    suspend fun getGrade(): AHUResponse<Grade>
}