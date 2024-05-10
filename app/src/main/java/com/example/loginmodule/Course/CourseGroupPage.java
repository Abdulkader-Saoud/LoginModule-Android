package com.example.loginmodule.Course;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.loginmodule.HomePage;
import com.example.loginmodule.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CourseGroupPage extends AppCompatActivity {
    private EditText studentIDET;
    private Button addstudentBTN,laodCSVBTN,saveBTN,deleteBTN;
    private ListView studentsLV;
    private ArrayList<String> studentsList = new ArrayList<>();
    private ArrayAdapter<String> studentAdapter;
    private String docID,uid;
    private TextView stdCountTV;
    private int stdCount = 0;

    private static final int PICK_CSV_FILE_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_course_group_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        uid = sharedPreferences.getString(getString(R.string.prefKey_stdID), null);

        studentIDET = findViewById(R.id.studentIDET);
        studentsLV = findViewById(R.id.studentsLV);
        addstudentBTN = findViewById(R.id.addstudentBTN);
        saveBTN = findViewById(R.id.saveBTN);
        deleteBTN = findViewById(R.id.deleteBTN);
        laodCSVBTN = findViewById(R.id.laodCSVBTN);
        stdCountTV = findViewById(R.id.studentsTV);

        laodCSVBTN.setOnClickListener(view -> loadCSVHandler());

        saveBTN.setOnClickListener(view -> saveData());
        deleteBTN.setOnClickListener(view -> deleteGroup());
        addstudentBTN.setOnClickListener(view -> addInstructor());
        Intent intent = getIntent();
        docID = intent.getStringExtra("docID");
        fetchData();
    }
    private void fetchData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("CourseGroups").document(docID).get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                studentsList = (ArrayList<String>) documentSnapshot.get("students");
                studentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, studentsList);
                studentsLV.setAdapter(studentAdapter);
                showStdCount();
                studentsLV.setOnItemClickListener((adapterView, view, i, l) -> {
                    studentsList.remove(i);
                    studentsLV.setAdapter(studentAdapter);
                    showStdCount();
                });}
        });
    }
    private void showStdCount(){
        stdCount = studentsList.size();
        stdCountTV.setText("Students: "+stdCount);
    }
    private void addInstructor() {
        String instructorID = studentIDET.getText().toString();
        if(instructorID.isEmpty()){
            Toast toast = Toast.makeText(this, "Please enter instructor ID", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        studentsList.add(instructorID);
        studentAdapter.notifyDataSetChanged();
        studentIDET.setText("");
    }

    private void loadCSVHandler() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Select CSV File"), PICK_CSV_FILE_REQUEST_CODE);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CSV_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    if (inputStream != null) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        studentsList.clear();
                        while ((line = reader.readLine()) != null) {
                            String[] headers = line.split(",");
                            if (headers.length >= 2) {
                                String std = headers[0].trim();
                                studentsList.add(std);
                            }
                        }
                        inputStream.close();
                        studentAdapter.notifyDataSetChanged();
                        showStdCount();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void saveData() {
        Map<String, Object> data = new HashMap<>();
        data.put("students", studentsList);
        data.put("count", stdCount);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("CourseGroups").document(docID).update(data).addOnSuccessListener(aVoid -> {
            Toast toast = Toast.makeText(this, "Students added successfully", Toast.LENGTH_SHORT);
            toast.show();
        });
        Intent intent = new Intent(this, HomePage.class);
        startActivity(intent);
    }
    private void deleteGroup() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("CourseGroups").document(docID).delete().addOnSuccessListener(aVoid -> {
            Toast toast = Toast.makeText(this, "Group deleted successfully", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        });
        docID = docID.replace(uid, "");
        db.collection("Courses").document(docID).get().addOnSuccessListener(documentSnapshot -> {
            if(documentSnapshot.exists()){
                ArrayList<String> instructors;
                instructors = (ArrayList<String>) documentSnapshot.get("instructors");
                instructors.remove(uid);
                db.collection("Courses").document(docID).update("instructors", instructors);
            }
        });
        Intent intent = new Intent(this, HomePage.class);
        startActivity(intent);
    }
}