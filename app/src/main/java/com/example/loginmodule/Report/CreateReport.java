package com.example.loginmodule.Report;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.loginmodule.Course.Course;
import com.example.loginmodule.Course.CourseFetcher;
import com.example.loginmodule.HomePage;
import com.example.loginmodule.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class CreateReport extends CourseFetcher {
    private AutoCompleteTextView scopeTV,courseTV, recipientTV;
    private ArrayAdapter<String> scopeArrayAdapter, coursesArrayAdapter, recipientArrayAdapter;
    private ArrayList<String> recipientsArray = new ArrayList<>();
    private ProgressBar progressBar;
    private ConstraintLayout dataLayout;
    private EditText subjectET, bodyET;
    private Button submitButton;
    private String courseName;

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
        dataLayout = findViewById(R.id.CL);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        dataLayout.setVisibility(View.GONE);

        subjectET = findViewById(R.id.subjectET);
        bodyET = findViewById(R.id.replyET);
        submitButton = findViewById(R.id.postReplyButton);
        Intent intent = getIntent();
        courseName = intent.getStringExtra("courseName");

        String[] scopeStrings = getResources().getStringArray(R.array.scopes_array);
        scopeArrayAdapter = new ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, scopeStrings);
        scopeTV = findViewById(R.id.scopeTV);
        scopeTV.setAdapter(scopeArrayAdapter);
        scopeTV.setText(scopeStrings[0], false);

        scopeTV.setOnItemClickListener((parent, view, position, id) -> {
            if (courseName != null && !courseName.isEmpty()) {
                if (scopeTV.getText().toString().contains("Course")) {
                    recipientsArray.clear();
                    recipientsArray.add("Instructors");
                } else {
                    recipientsArray.clear();
                    recipientsArray.add("Admins");
                }
                recipientTV.setText(recipientsArray.get(0), false);
                recipientArrayAdapter.notifyDataSetChanged();
            }
        });
        courseTV = findViewById(R.id.courseTV);
        recipientTV = findViewById(R.id.recipientTV);

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
        if (courseName != null && !courseName.isEmpty()){
            courseNames.add(courseName);
        }
        else {
            for (Course course : coursesArray){
                courseNames.add(course.getCourseName());
            }
        }

        coursesArrayAdapter = new ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, courseNames);
        courseTV.setAdapter(coursesArrayAdapter);
        courseTV.setText(courseNames.get(0), false);

        if (courseName != null && !courseName.isEmpty()){
            recipientsArray.clear();
            if (scopeTV.getText().toString().contains("Course")){
                recipientsArray.add("Instructors");
            }
            else {
                recipientsArray.add("Admins");
            }
        }
        else {
            recipientsArray.clear();
            recipientsArray.add("Instructors");
            recipientsArray.add("Admins");
        }

        recipientArrayAdapter = new ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, recipientsArray);
        recipientTV.setAdapter(recipientArrayAdapter);
        recipientTV.setText(recipientsArray.get(0), false);

        progressBar.setVisibility(View.GONE);
        dataLayout.setVisibility(View.VISIBLE);
        submitButton.setOnClickListener(e -> handleSubmit());

        Log.d("handlefetchCoursesSTD", "Courses fetched successfully");
        Log.d("courseName", "cnam :" + courseName);
        Log.d("Recipients","cnam :" +  recipientsArray.toString());
    }

    private void handleSubmit(){
        String scope = scopeTV.getText().toString();
        String course = courseTV.getText().toString();
        String recipient = recipientTV.getText().toString();
        String subject = subjectET.getText().toString();
        String body = bodyET.getText().toString();

        dataLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        if (scope.isEmpty() || course.isEmpty() || recipient.isEmpty() || subject.isEmpty() || body.isEmpty()){
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            dataLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            return;
        }
        if (recipient.equals("Instructors")){
            fetchInstructors(coursesArray.get(coursesArrayAdapter.getPosition(course)).getCourseCode());
        }
        else {
            fetchAdmin();
        }
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
                            ArrayList<String> instructorsArray = (ArrayList<String>) courseData.get("instructors");
                            if (instructorsArray == null)
                                return;
                            createReport(instructorsArray);
                        }
                    } else {
                        Log.e("fetchInstructors", "Error getting instructors", task.getException());
                    }
                });
    }
    private void fetchAdmin(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            ArrayList<String> adminsArray = new ArrayList<>();
                            for (int i = 0; i < task.getResult().size(); i++) {
                                adminsArray.add(task.getResult().getDocuments().get(i).getId());
                            }
                            createReport(adminsArray);
                        }
                    } else {
                        Log.e("fetchAdmin", "Error getting admins", task.getException());
                    }
                });
    }
    private void createReport(ArrayList<String> recipients){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String,Object> reportData = new HashMap<String,Object>(){};
        reportData.put("scope", scopeTV.getText().toString());
        reportData.put("course", courseTV.getText().toString());
        reportData.put("recipient", recipientTV.getText().toString());
        reportData.put("recipients",  recipients);
        reportData.put("body", bodyET.getText().toString());
        reportData.put("subject", subjectET.getText().toString());
        Date date = new Date();
        date = new Date(date.getTime() - date.getTime() % 1000);
        reportData.put("Date", date);

        db.collection("Reports")
                .add(reportData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Report sent", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, HomePage.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error Sending The Report.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, HomePage.class);
                    startActivity(intent);
                });
    }

}