package com.ahu.ahutong.ui.widget.schedule.bean;

import androidx.annotation.NonNull;

import com.ahu.ahutong.data.model.Course;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: SinkDev
 * @Date: 2021/8/1-下午2:13
 * @Email: 468766131@qq.com
 */
public class ScheduleCourse {
    @NonNull
    private final List<Course> courses = new ArrayList<>();
    private Integer startTime;

    public ScheduleCourse(Integer startTime) {
        this.startTime = startTime;
    }

    @NonNull
    public List<Course> getCourses() {
        return courses;
    }

    public void addCourse(@NonNull Course course) {
        this.courses.add(course);
    }

    public Integer getStartTime() {
        return startTime;
    }

    public void setStartTime(Integer startTime) {
        this.startTime = startTime;
    }

    public Integer getLength(int week, boolean showAll) {
        int length = 1;
        Course course = getCourse(week, showAll);
        if (course != null) {
            length = course.getLength();
        }
        return length;
    }

    public Course getCourse(int week, boolean isShowAll) {
        for (Course course : courses) {
            if (course.getStartWeek() <= week && course.getEndWeek() >= week) {
                return course;
            }
        }
        if (isShowAll && courses.size() > 0) {
            return courses.get(0);
        }
        return null;
    }
}
