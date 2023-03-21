package com.ahu.ahutong.data.model;

import java.util.Date;

public class ScheduleConfigBean {
    private Date startTime; // 开学时间
    private int week; // 当前周
    private int weekDay; // 周几
    private boolean isShowAll; // 是否显示非本周

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public int getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(int weekDay) {
        this.weekDay = weekDay;
    }

    public boolean isShowAll() {
        return isShowAll;
    }

    public void setShowAll(boolean showAll) {
        isShowAll = showAll;
    }
}
