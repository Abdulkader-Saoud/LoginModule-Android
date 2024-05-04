package com.example.loginmodule.Course;

import java.util.ArrayList;
import java.util.Date;

public class Course {
    private String courseName,courseCode;
    private Integer stdNum;
    private ArrayList<String> courseInstructors;
    private String creator;
    private Date startDate, endDate;

    public Course(String courseName,String courseCode, Date startDate, Date endDate, ArrayList<String> courseInstructors, String creator) {
        this.courseName = courseName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.courseCode = courseCode;
        this.courseInstructors = courseInstructors;
        this.creator = creator;
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



    public Integer getStdNum() {
        return stdNum;
    }

    public void setStdNum(Integer stdNum) {
        this.stdNum = stdNum;
    }

    public ArrayList<String> getCourseInstructors() {
        return courseInstructors;
    }

    public Boolean hasInstructor(String instructor){
        return courseInstructors.contains(instructor);
    }
    public Boolean isCreator(String uid){
        return creator.equals(uid);
    }

}
