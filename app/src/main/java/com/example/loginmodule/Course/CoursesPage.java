package com.example.loginmodule.Course;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginmodule.HomePage;
import com.example.loginmodule.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CoursesPage extends AppCompatActivity {
    private ArrayList<Course> courseModelArrayList = new ArrayList<Course>();
    private String uid,accountType;
    private RecyclerView courseRV;
    private Button AddCourseButtonBTN;
    private RadioGroup radioFilterGroup;
    CourseAdapter courseAdapter;
    private String filter = "None";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_courses_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        uid = sharedPreferences.getString(getString(R.string.prefKey_stdID), null);
        accountType = sharedPreferences.getString(getString(R.string.prefKey_accType), null);

        courseRV = findViewById(R.id.coursesRV);
        AddCourseButtonBTN = findViewById(R.id.AddCourseButton);
        radioFilterGroup = findViewById(R.id.radioFilterGroup);
        radioFilterGroup.check(R.id.radio_none);
        radioFilterGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_none)
                filter = "None";
            else if (checkedId == R.id.radio_attending)
                filter = "Attending";
            else if (checkedId == R.id.radio_complete)
                filter = "Complete";

            courseModelArrayList.clear();
            courseAdapter.notifyDataSetChanged();
            fetchCourses();
        });

        AddCourseButtonBTN.setOnClickListener(e -> {
            Intent intent = new Intent(this , CourseCreatePage.class);
            startActivity(intent);
        });
        courseRV.setEnabled(false);
        courseRV.setVisibility(View.GONE);
        fetchCourses();
    }
    private void setupLV() {
        if (courseModelArrayList.isEmpty()) {
            Toast.makeText(this, "No courses found", Toast.LENGTH_SHORT).show();
            return;
        }
        courseAdapter = new CourseAdapter(this, courseModelArrayList);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        courseRV.setLayoutManager(linearLayoutManager);
        courseRV.setAdapter(courseAdapter);
        courseRV.setEnabled(true);
        courseRV.setVisibility(View.VISIBLE);
    }
    private void fetchCourses() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d("fetchCourses", "Fetching courses");
        Log.d("fetchCourses", "Account type: " + accountType);

        if (accountType.equals("Instructor")) {
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
                            setupLV();
                        } else {
                            Log.e("fetchCourses", "Error getting courses", task.getException());
                        }
                    });
          AddCourseButtonBTN.setVisibility(View.VISIBLE);
        }
        else {
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
                                                            addCourse(courseData);
                                                        } else {
                                                            Log.e("fetchCourses", "Course data is missing or incorrect");
                                                        }
                                                    }
                                                } else {
                                                    Log.e("fetchCourses", "Error getting courses", task1.getException());
                                                }
                                                setupLV();
                                            });
                                }
                            }

                        } else {
                            Log.e("fetchCourses", "Error getting courses", task.getException());
                        }
                    });
        }
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
            courseModelArrayList.add(course);
        }
        else {
            Log.e("fetchCourses", "Course data is missing or incorrect");
        }
    }
}