package com.ahu.ahutong.data.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * @Author SinkDev
 * @Date 2021/7/27-21:25
 * @Email 468766131@qq.com
 */

public class Grade implements Serializable {

    /**
     * totalGradePoint : 总学分绩点
     * totalCredit : 总学分
     * totalGradePointAverage : 总平均学分绩点
     * termGradeList : [{"schoolYear":"学年","term":"学期","termGradePoint":"学期总绩点","termTotalCredit":"学期总学分","termGradePointAverage":"学期总平均学分绩点","gradeList":[{"courseNum":"课程代号","course":"课程名称","credit":"学分","gradePoint":"绩点","grade":"成绩","course_nature":"课程类型"},"..."]},"..."]
     */

    @SerializedName("totalGradePoint")
    private String totalGradePoint;
    @SerializedName("totalCredit")
    private String totalCredit;
    @SerializedName("totalGradePointAverage")
    private String totalGradePointAverage;
    @SerializedName("termGradeList")
    private List<TermGradeListBean> termGradeList;

    public String getTotalGradePoint() {
        return totalGradePoint;
    }

    public void setTotalGradePoint(String totalGradePoint) {
        this.totalGradePoint = totalGradePoint;
    }

    public String getTotalCredit() {
        return totalCredit;
    }

    public void setTotalCredit(String totalCredit) {
        this.totalCredit = totalCredit;
    }

    public String getTotalGradePointAverage() {
        return totalGradePointAverage;
    }

    public void setTotalGradePointAverage(String totalGradePointAverage) {
        this.totalGradePointAverage = totalGradePointAverage;
    }

    public List<TermGradeListBean> getTermGradeList() {
        return termGradeList;
    }

    public void setTermGradeList(List<TermGradeListBean> termGradeList) {
        this.termGradeList = termGradeList;
    }

    public static class TermGradeListBean implements Serializable {
        /**
         * schoolYear : 学年
         * term : 学期
         * termGradePoint : 学期总绩点
         * termTotalCredit : 学期总学分
         * termGradePointAverage : 学期总平均学分绩点
         * gradeList : [{"courseNum":"课程代号","course":"课程名称","credit":"学分","gradePoint":"绩点","grade":"成绩","course_nature":"课程类型"},"..."]
         */

        @SerializedName("schoolYear")
        private String schoolYear;
        @SerializedName("schoolTerm")
        private String term;
        @SerializedName("termGradePoint")
        private String termGradePoint;
        @SerializedName("termTotalCredit")
        private String termTotalCredit;
        @SerializedName("termGradePointAverage")
        private String termGradePointAverage;
        @SerializedName("termGradeList")
        private List<GradeListBean> gradeList;

        public String getSchoolYear() {
            return schoolYear;
        }

        public void setSchoolYear(String schoolYear) {
            this.schoolYear = schoolYear;
        }

        public String getTerm() {
            return term;
        }

        public void setTerm(String term) {
            this.term = term;
        }

        public String getTermGradePoint() {
            return termGradePoint;
        }

        public void setTermGradePoint(String termGradePoint) {
            this.termGradePoint = termGradePoint;
        }

        public String getTermTotalCredit() {
            return termTotalCredit;
        }

        public void setTermTotalCredit(String termTotalCredit) {
            this.termTotalCredit = termTotalCredit;
        }

        public String getTermGradePointAverage() {
            return termGradePointAverage;
        }

        public void setTermGradePointAverage(String termGradePointAverage) {
            this.termGradePointAverage = termGradePointAverage;
        }

        public List<GradeListBean> getGradeList() {
            return gradeList;
        }

        public void setGradeList(List<GradeListBean> gradeList) {
            this.gradeList = gradeList;
        }

        public static class GradeListBean implements Serializable {
            /**
             * courseNum : 课程代号
             * course : 课程名称
             * credit : 学分
             * gradePoint : 绩点
             * grade : 成绩
             * courseType : 课程类型
             */
            @SerializedName("courseNum")
            private String courseNum;
            @SerializedName("course")
            private String course;
            @SerializedName("credit")
            private String credit;
            @SerializedName("gradePoint")
            private String gradePoint;
            @SerializedName("grade")
            private String grade;
            @SerializedName("courseType")
            private String courseNature;

            public String getCourseNum() {
                return courseNum;
            }

            public void setCourseNum(String courseNum) {
                this.courseNum = courseNum;
            }

            public String getCourse() {
                return course;
            }

            public void setCourse(String course) {
                this.course = course;
            }

            public String getCredit() {
                return credit;
            }

            public void setCredit(String credit) {
                this.credit = credit;
            }

            public String getGradePoint() {
                return gradePoint;
            }

            public void setGradePoint(String gradePoint) {
                this.gradePoint = gradePoint;
            }

            public String getGrade() {
                return grade;
            }

            public void setGrade(String grade) {
                this.grade = grade;
            }

            public String getCourseNature() {
                return courseNature;
            }

            public void setCourseNature(String courseNature) {
                this.courseNature = courseNature;
            }

            @NonNull
            @Override
            public String toString() {
                return "GradeListBean{" +
                        "courseNum='" + courseNum + '\'' +
                        ", course='" + course + '\'' +
                        ", credit='" + credit + '\'' +
                        ", gradePoint='" + gradePoint + '\'' +
                        ", grade='" + grade + '\'' +
                        ", courseNature='" + courseNature + '\'' +
                        '}';
            }
        }

        @NonNull
        @Override
        public String toString() {
            return "TermGradeListBean{" +
                    "schoolYear='" + schoolYear + '\'' +
                    ", term='" + term + '\'' +
                    ", termGradePoint='" + termGradePoint + '\'' +
                    ", termTotalCredit='" + termTotalCredit + '\'' +
                    ", termGradePointAverage='" + termGradePointAverage + '\'' +
                    ", gradeList=" + gradeList +
                    '}';
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "Grade{" +
                "totalGradePoint='" + totalGradePoint + '\'' +
                ", totalCredit='" + totalCredit + '\'' +
                ", totalGradePointAverage='" + totalGradePointAverage + '\'' +
                ", termGradeList=" + termGradeList +
                '}';
    }
}
