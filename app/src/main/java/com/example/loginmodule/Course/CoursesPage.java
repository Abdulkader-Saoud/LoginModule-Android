package com.example.loginmodule.Course;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private FirebaseAuth mAuth;
    private ArrayList<Course> courseModelArrayList = new ArrayList<Course>();
    private String uid,accountType;
    private RecyclerView courseRV;
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
        Log.d("fetchCourses", accountType +  "  TPYEEE");
        mAuth = FirebaseAuth.getInstance();

        courseRV = findViewById(R.id.coursesRV);
        courseRV.setEnabled(false);
        fetchCourses();

    }
    private void setupLV() {
        if (courseModelArrayList.isEmpty()) {
            Course course = new Course("No courses found", "No courses found", new Date(), new Date(), null, "");
            courseModelArrayList.add(course);
        }
        CourseAdapter courseAdapter = new CourseAdapter(this, courseModelArrayList);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        courseRV.setLayoutManager(linearLayoutManager);
        courseRV.setAdapter(courseAdapter);
        courseRV.setEnabled(true);
    }
    private void fetchCourses() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d("fetchCourses", "fetching courses");
        if (accountType.equals("Instructor")) {
            Log.d("fetchCourses", "fetching courses");
            db.collection("Courses")
                    .whereEqualTo("creator", uid)
                    .get()
                    .addOnCompleteListener(task -> {
                        Log.d("fetchCourses", "fetching courses");
                        if (task.isSuccessful()) {
                            Log.d("fetchCourses", "fetching courses successful");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> courseData = document.getData();
                                Log.d("fetchCourses", "fetching courses found one");
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
                                    courseModelArrayList.add(course);
                                } else {
                                    Log.e("fetchCourses", "Course data is missing or incorrect");
                                }
                            }
                            setupLV();
                        } else {
                            Log.e("fetchCourses", "Error getting courses", task.getException());
                        }
                    });
        } else {
            Toast.makeText(this, "Student", Toast.LENGTH_SHORT).show();
        }
    }

}