package com.example.loginmodule.Course;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.loginmodule.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public abstract class CourseFetcher extends AppCompatActivity {
    protected String uid, accountType;
    protected ArrayList<Course> coursesArray = new ArrayList<>();
    protected String filter = "None";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        uid = sharedPreferences.getString(getString(R.string.prefKey_stdID), null);
        accountType = sharedPreferences.getString(getString(R.string.prefKey_accType), null);
    }
    protected void fetchCoursesINS(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Courses")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> courseData = document.getData();
                                if (courseData == null)
                                    continue;
                                if (!(courseData.get("creator").equals(uid)) && !((ArrayList<String>) courseData.get("instructors")).contains(uid))
                                    continue;
                                addCourse(courseData);
                            }
                        }
                        handlefetchCoursesINS();
                    } else {
                        Log.e("fetchCourses", "Error getting courses", task.getException());
                    }
                });
    }
    protected void fetchCoursesSTD(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("CourseGroups")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> courseGroupData = document.getData();
                                if (courseGroupData == null) {
                                    continue;
                                }
                                if (!((ArrayList<String>) courseGroupData.get("students")).contains(uid))
                                    continue;
                                String courseID = document.getId().toString();
                                courseID = courseID.substring(0, 7);
                                Log.d("fetchCourses", "Course ID: " + courseID);
                                db.collection("Courses").document(courseID)
                                        .get()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                if (task1.getResult() != null) {
                                                    Map<String, Object> courseData = task1.getResult().getData();
                                                    if (courseData != null) {
                                                        Log.d("fetchCourses", "Course data: " + courseData.get("coursename"));
                                                        addCourse(courseData);
                                                    } else {
                                                        Log.e("fetchCourses", "Course data is missing or incorrect");
                                                    }
                                                }
                                            } else {
                                                Log.e("fetchCourses", "Error getting courses", task1.getException());
                                            }
                                            handlefetchCoursesSTD();
                                        });
                            }
                        }
                    } else {
                        Log.e("fetchCourses", "Error getting courses", task.getException());
                    }
                });
    }
    private void addCourse(Map<String,Object> courseData) {
        if (courseData != null && courseData.containsKey("coursename") &&
                courseData.containsKey("courseid") &&
                courseData.containsKey("startdate") &&
                courseData.containsKey("enddate")) {

            Date startDate = ((Timestamp) courseData.get("startdate")).toDate();
            Date endDate = ((Timestamp) courseData.get("enddate")).toDate();
            ArrayList<String> instructors = (ArrayList<String>) courseData.get("instructors");
            Course course = new Course(
                    courseData.get("coursename").toString(),
                    courseData.get("courseid").toString(),
                    startDate,
                    endDate,
                    instructors,
                    courseData.get("creator").toString()
            );
            if (course.getTimeInfo().equals(filter))
                return;
            coursesArray.add(course);
        }
        else {
            Log.e("fetchCourses", "Course data is missing or incorrect");
        }
    }

    protected void handlefetchCoursesSTD(){};
    protected void handlefetchCoursesINS(){};
}
