package com.example.loginmodule.Course;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginmodule.HomePage;
import com.example.loginmodule.R;

public class CoursesPage extends CourseFetcher {
    private RecyclerView courseRV;
    private Button AddCourseButtonBTN;
    private RadioGroup radioFilterGroup;
    private CourseAdapter courseAdapter;
    private ProgressBar progressBar;
    private ConstraintLayout dataLayout;
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

        dataLayout = findViewById(R.id.CL);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        dataLayout.setVisibility(View.GONE);

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

            coursesArray.clear();
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
    private void setupRecyclerView() {
        if (coursesArray.isEmpty()) {
            if (accountType.equals("Student")){
                Toast.makeText(this, "No courses found", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, HomePage.class);
                startActivity(intent);
                return;
            }
            else {
                Toast.makeText(this, "No courses found Create One.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, CourseCreatePage.class);
                startActivity(intent);
                return;
            }
        }

        courseAdapter = new CourseAdapter(this, coursesArray);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        courseRV.setLayoutManager(linearLayoutManager);
        courseRV.setAdapter(courseAdapter);
        courseRV.setEnabled(true);
        courseRV.setVisibility(View.VISIBLE);

        progressBar.setVisibility(View.GONE);
        dataLayout.setVisibility(View.VISIBLE);
    }
    @Override
    protected void handlefetchCoursesINS() {setupRecyclerView();}
    @Override
    protected void handlefetchCoursesSTD() {setupRecyclerView();}

    private void fetchCourses() {
        if (accountType.equals("Instructor")) {
            fetchCoursesINS();
            AddCourseButtonBTN.setVisibility(View.VISIBLE);
        }
        else {
            fetchCoursesSTD();
            AddCourseButtonBTN.setVisibility(View.GONE);
        }
    }

}