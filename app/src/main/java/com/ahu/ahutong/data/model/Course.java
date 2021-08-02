package com.ahu.ahutong.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @Author SinkDev
 * @Date 2021/7/27-21:30
 * @Email 468766131@qq.com
 */
public class Course implements Serializable {

    /**
     * weekday : 周几
     * startWeek : 开始的周
     * endWeek : 结束的周
     * extra : 附加信息默认为空
     * location : 上课地点
     * name : 课程名称
     * teacher : 老师姓名
     * length : 课程长度（几节课）
     * startTime : 开始时间（第几节开始）
     * singleDouble : 是否单双周
     * courseId : 课程代码
     */

    @SerializedName("weekday")
    private String weekday;
    @SerializedName("startWeek")
    private String startWeek;
    @SerializedName("endWeek")
    private String endWeek;
    @SerializedName("extra")
    private String extra;
    @SerializedName("location")
    private String location;
    @SerializedName("name")
    private String name;
    @SerializedName("teacher")
    private String teacher;
    @SerializedName("length")
    private String length;
    @SerializedName("startTime")
    private String startTime;
    @SerializedName("singleDouble")
    private String singleDouble;
    @SerializedName("courseId")
    private String courseId;

    public Integer getWeekday() {
        return Integer.parseInt(weekday);
    }

    public void setWeekday(String weekday) {
        this.weekday = weekday;
    }

    public Integer getStartWeek() {
        return Integer.parseInt(startWeek);
    }

    public void setStartWeek(String startWeek) {
        this.startWeek = startWeek;
    }

    public Integer getEndWeek() {
        return Integer.parseInt(endWeek);
    }

    public void setEndWeek(String endWeek) {
        this.endWeek = endWeek;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public Integer getLength() {
        return Integer.parseInt(length);
    }

    public void setLength(String length) {
        this.length = length;
    }

    public Integer getStartTime() {
        return Integer.parseInt(startTime);
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getSingleDouble() {
        return singleDouble;
    }

    public void setSingleDouble(String singleDouble) {
        this.singleDouble = singleDouble;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

}

