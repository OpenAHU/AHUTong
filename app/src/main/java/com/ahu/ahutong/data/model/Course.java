package com.ahu.ahutong.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

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
    @Deprecated
    private String singleDouble;
    @SerializedName("courseId")
    private String courseId;


    @SerializedName("weekIndexes")
    private List<Integer> weekIndexes;

    public Integer getWeekday() {
        return weekday == null || weekday.isEmpty() ? 0 : Integer.parseInt(weekday);
    }

    public void setWeekday(String weekday) {
        this.weekday = weekday;
    }

    public Integer getStartWeek() {
        return startWeek == null || startWeek.isEmpty() ? 0 : Integer.parseInt(startWeek);
    }

    public void setStartWeek(String startWeek) {
        this.startWeek = startWeek;
    }

    public Integer getEndWeek() {
        return endWeek == null || endWeek.isEmpty() ? 0 : Integer.parseInt(endWeek);
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
        return length == null || length.isEmpty() ? 0 : Integer.parseInt(length);
    }

    public void setLength(String length) {
        this.length = length;
    }

    public Integer getStartTime() {
        return startTime == null || startTime.isEmpty() ? 0 : Integer.parseInt(startTime);
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

    public List<Integer> getWeekIndexes() {
        return weekIndexes;
    }

    public void setWeekIndexes(List<Integer> weekIndexes) {
        this.weekIndexes = weekIndexes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Course course = (Course) o;

        if (weekday != null ? !weekday.equals(course.weekday) : course.weekday != null)
            return false;
        if (startWeek != null ? !startWeek.equals(course.startWeek) : course.startWeek != null)
            return false;
        if (endWeek != null ? !endWeek.equals(course.endWeek) : course.endWeek != null)
            return false;
        if (extra != null ? !extra.equals(course.extra) : course.extra != null) return false;
        if (location != null ? !location.equals(course.location) : course.location != null)
            return false;
        if (name != null ? !name.equals(course.name) : course.name != null) return false;
        if (teacher != null ? !teacher.equals(course.teacher) : course.teacher != null)
            return false;
        if (length != null ? !length.equals(course.length) : course.length != null) return false;
        if (startTime != null ? !startTime.equals(course.startTime) : course.startTime != null)
            return false;
        if (singleDouble != null ? !singleDouble.equals(course.singleDouble) : course.singleDouble != null)
            return false;
        if (courseId != null ? !courseId.equals(course.courseId) : course.courseId != null)
            return false;
        return weekIndexes != null ? weekIndexes.equals(course.weekIndexes) : course.weekIndexes != null;
    }

    @Override
    public int hashCode() {
        int result = weekday != null ? weekday.hashCode() : 0;
        result = 31 * result + (startWeek != null ? startWeek.hashCode() : 0);
        result = 31 * result + (endWeek != null ? endWeek.hashCode() : 0);
        result = 31 * result + (extra != null ? extra.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (teacher != null ? teacher.hashCode() : 0);
        result = 31 * result + (length != null ? length.hashCode() : 0);
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (singleDouble != null ? singleDouble.hashCode() : 0);
        result = 31 * result + (courseId != null ? courseId.hashCode() : 0);
        result = 31 * result + (weekIndexes != null ? weekIndexes.hashCode() : 0);
        return result;
    }
}

