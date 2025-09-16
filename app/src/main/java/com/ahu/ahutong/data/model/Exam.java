package com.ahu.ahutong.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * @Author SinkDev
 * @Date 2021/7/27-21:29
 * @Email 468766131@qq.com
 */
public class Exam implements Serializable {
    /**
     * course : 课程名
     * location : 考试地点
     * time : 考试时间
     * seatNum : 座位号
     * finished : 考试是否结束
     */
    @SerializedName("course")
    private String course;
    @SerializedName("location")
    private String location;
    @SerializedName("time")
    private String time;
    @SerializedName("seatNum")
    private String seatNum;



    @SerializedName("finished")
    private Boolean finished;

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSeatNum() {
        return seatNum;
    }

    public void setSeatNum(String seatNum) {
        this.seatNum = seatNum;
    }


    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }
}
