package com.example.loginmodule.Course;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

public class Course {
    private String courseName,courseCode;
    private Integer stdNum;
    private ArrayList<String> courseInstructors;
    private String creator;
    private Date startDate, endDate;
    private String timeInfo;// Attending - Complete


    public Course(String courseName,String courseCode, Date startDate, Date endDate, ArrayList<String> courseInstructors, String creator) {
        this.courseName = courseName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.courseCode = courseCode;
        this.courseInstructors = courseInstructors;
        this.creator = creator;
        Log.d("Course", "Course: "+courseName+" " );
        for (String instructor: courseInstructors){
            Log.d("Course", "Course: "+instructor+" " );
        }
        this.timeInfo = "Attending";
        Date currentDate = new Date();
        if (endDate.after(currentDate)){
            this.timeInfo = "Complete";
        }

    }

    public String getStartandEndDate() {
        // only day month and year
        return startDate.toString().substring(0,10) + " - " + endDate.toString().substring(0,10);
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
    public String getCurrentTimeInfo(){
        return timeInfo;
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
    public String getTimeInfo(){
        return timeInfo;
    }

    public Boolean hasInstructor(String instructor){
        return courseInstructors.contains(instructor);
    }
    public Boolean isCreator(String uid){
        return creator.equals(uid);
    }

}
