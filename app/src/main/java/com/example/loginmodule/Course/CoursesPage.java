package com.example.loginmodule.Course;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginmodule.R;

import java.util.ArrayList;

public class CoursesPage extends AppCompatActivity {


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

        RecyclerView courseRV = findViewById(R.id.coursesRV);

        ArrayList<Course> courseModelArrayList = new ArrayList<Course>();
        courseModelArrayList.add(new Course("DSA in Java", "DSA in Java umportant dsd as dasd sa",25));
        courseModelArrayList.add(new Course("Java Course", "DSA in Java umportant dsd as dasd sa",25));
        courseModelArrayList.add(new Course("C++ Course", "DSA in Java umportant dsd as dasd sa", 25));
        courseModelArrayList.add(new Course("DSA in C++", "DSA in Java umportant dsd as dasd sa", 25));
        courseModelArrayList.add(new Course("Kotlin for Android", "DSA in Java umportant dsd as dasd sa",25));
        courseModelArrayList.add(new Course("Java for Android", "DSA in Java umportant dsd as dasd sa", 25));
        courseModelArrayList.add(new Course("HTML and CSS", "DSA in Java umportant dsd as dasd sa", 25));

        CourseAdapter courseAdapter = new CourseAdapter(this, courseModelArrayList);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        courseRV.setLayoutManager(linearLayoutManager);
        courseRV.setAdapter(courseAdapter);
    }
}