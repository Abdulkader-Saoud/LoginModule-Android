package com.example.loginmodule.AttendanceModule;

public class AttendanceItem {
    private String courseName;
    private String courseCode;
    private String attendanceDate;

    public AttendanceItem(String courseName, String courseCode, String attendanceDate) {
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.attendanceDate = attendanceDate;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getAttendanceDate() {
        return attendanceDate;
    }
}
