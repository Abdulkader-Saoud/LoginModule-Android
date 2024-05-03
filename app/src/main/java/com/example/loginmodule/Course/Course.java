package com.example.loginmodule.Course;

import java.util.ArrayList;

public class Course {
    private String courseName;
    private String courseCode;
    private String courseContent;
    private Integer stdNum;
    private ArrayList<String> courseInstructors;

    public Course(String courseName, String courseCode, String courseContent, Integer stdNum, ArrayList<String> courseInstructors) {
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.courseContent = courseContent;
        this.stdNum = stdNum;
        this.courseInstructors = courseInstructors;
    }
    public Course(String courseName, String courseContent, Integer stdNum) {
        this.courseName = courseName;
        this.courseContent = courseContent;
        this.stdNum = stdNum;
        this.courseInstructors = new ArrayList<>();
    }
    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseContent() {
        return courseContent;
    }

    public void setCourseContent(String courseContent) {
        this.courseContent = courseContent;
    }

    public Integer getStdNum() {
        return stdNum;
    }

    public void setStdNum(Integer stdNum) {
        this.stdNum = stdNum;
    }

    public ArrayList<String> getCourseInstructors() {
        return courseInstructors;
    }
    public void setCourseInstructors(ArrayList<String> courseInstructors) {
        this.courseInstructors = courseInstructors;
    }
    public void addInstructor(String instructor){
        courseInstructors.add(instructor);
    }
    public void removeInstructor(String instructor){
        courseInstructors.remove(instructor);
    }
}
