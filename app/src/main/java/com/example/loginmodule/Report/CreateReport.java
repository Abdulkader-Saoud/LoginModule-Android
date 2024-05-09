package com.example.loginmodule.Report;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.loginmodule.Course.Course;
import com.example.loginmodule.Course.CourseFetcher;
import com.example.loginmodule.HomePage;
import com.example.loginmodule.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;


public class CreateReport extends CourseFetcher {
    AutoCompleteTextView scopeTV,courseTV, instructorTV;
    ArrayAdapter<String> scopeArrayAdapter, coursesArrayAdapter, instructorsArrayAdapter;
    ArrayList<String> instructorsArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_report);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String[] scopeStrings = getResources().getStringArray(R.array.scopes_array);
        scopeArrayAdapter = new ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, scopeStrings);
        scopeTV = findViewById(R.id.scopeTV);
        scopeTV.setAdapter(scopeArrayAdapter);

        courseTV = findViewById(R.id.courseTV);
        courseTV.setVisibility(View.GONE);

        instructorTV = findViewById(R.id.instructorTV);
        instructorTV.setVisibility(View.GONE);

        fetchCoursesSTD();
    }

    @Override
    protected void handlefetchCoursesSTD() {
        if (super.coursesArray == null || coursesArray.isEmpty()){
            Toast.makeText(this, "No courses found", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, HomePage.class);
            startActivity(intent);
            return;
        }
        ArrayList<String> courseNames = new ArrayList<>();
        for (Course course : coursesArray){
            courseNames.add(course.getCourseName());
        }
        coursesArrayAdapter = new ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, courseNames);
        courseTV.setAdapter(coursesArrayAdapter);

        AutoCompleteTextView courseTV = findViewById(R.id.courseTV);

        courseTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fetchInstructors(coursesArray.get(position).getCourseCode());
            }
        });

        courseTV.setVisibility(View.VISIBLE);
    }

    private void fetchInstructors(String courseID){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d("fetchInstructors", "Fetching instructors for course: " + courseID);
        db.collection("Courses")
                .document(courseID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            Map<String, Object> courseData = task.getResult().getData();
                            if (courseData == null)
                                return;
                            instructorsArray = (ArrayList<String>) courseData.get("instructors");
                            setupInstructorsAdapter();
                        }
                    } else {
                        Log.e("fetchInstructors", "Error getting instructors", task.getException());
                    }
                });
    }
    private void setupInstructorsAdapter(){
        if (instructorsArray == null || instructorsArray.isEmpty()){
            Toast.makeText(this, "No instructors found", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, HomePage.class);
            startActivity(intent);
            return;
        }
        ArrayAdapter<String> instructorsArrayAdapter = new ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, instructorsArray);
        instructorTV.setAdapter(instructorsArrayAdapter);
        instructorTV.setVisibility(View.VISIBLE);
    }

}