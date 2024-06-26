package com.example.loginmodule.Course;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.loginmodule.HomePage;
import com.example.loginmodule.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CourseCreatePage extends AppCompatActivity implements DatePickerFragment.DateSetListener {
    private FirebaseAuth mAuth;
    private Button editStartdateBTN,editEnddateBTN, saveBTN, deleteBTN, addgroupBTN;
    private TextView sedateTV;
    private EditText coursenameET, courseidET, instructorET;
    private String seChoice, docID;
    private int startYear, startMonth, startDay , endYear, endMonth, endDay;
    private String uid;
    private ArrayList<String> instructors;
    private ArrayAdapter<String> instructorsAdapter;
    private ListView instructorsLV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_course_create_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        uid = sharedPreferences.getString(getString(R.string.prefKey_stdID), null);

        Intent thisintent = getIntent();
        docID = thisintent.getStringExtra("docID");
        mAuth = FirebaseAuth.getInstance();
        setupUI();

        deleteBTN.setOnClickListener(v -> handleDeleteCourse());
        if (docID == null) {
            instructors = new ArrayList<>();
        }
        else {
            fetchUI();
        }

        updateDate(true);

        editStartdateBTN.setOnClickListener(v -> {
            seChoice = "start";
            DatePickerFragment datePickerFragment = new DatePickerFragment();
            datePickerFragment.setDateSetListener(this);
            datePickerFragment.show(getSupportFragmentManager(), "date picker");
        });
        editEnddateBTN.setOnClickListener(v -> {
            seChoice = "end";
            DatePickerFragment datePickerFragment = new DatePickerFragment();
            datePickerFragment.setDateSetListener(this);
            datePickerFragment.show(getSupportFragmentManager(), "date picker");
        });

    }

    @Override
    public void onDateSet(int year, int month, int day) {
        Log.d("CC DatePicker", "onDateSet: " + year + "-" + month + "-" + day);
        if (seChoice.equals("start")) {
            startYear = year;
            startMonth = month;
            startDay = day;
        }
        if (seChoice.equals("end")) {
            endYear = year;
            endMonth = month;
            endDay = day;
        }
        updateDate(false);
    }
    private void saveCourse() {
        String coursename = coursenameET.getText().toString();
        String courseid = courseidET.getText().toString();
        Date startDate = new Date(startYear -1900, startMonth, startDay);
        Date endDate = new Date(endYear -1900, endMonth, endDay);

        Log.d("CC SaveCourse", "saveCourse: " + startDate + " " + endDate);
        if (coursename.isEmpty() || courseid.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> data = new HashMap<>();
            data.put("coursename", coursename);
            data.put("courseid", courseid);
            data.put("startdate", startDate);
            data.put("enddate", endDate);
            data.put("creator", uid);
            data.put("instructors", instructors);

            db.collection("Courses").document(courseid)
                    .set(data)
                    .addOnSuccessListener(aVoid -> {
                        docID = courseid;
                        Toast.makeText(this, "Course saved", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error saving course", Toast.LENGTH_SHORT).show();
                    });
        }
        else {
            Toast toast = Toast.makeText(this, "User is null", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void updateDate(Boolean today) {
        String date;
        if (today) {
            final java.util.Calendar c = java.util.Calendar.getInstance();
            endYear = startYear = c.get(java.util.Calendar.YEAR);
            endMonth = startMonth = c.get(java.util.Calendar.MONTH);
            endDay = startDay = c.get(java.util.Calendar.DAY_OF_MONTH);
        }
        date = startYear + "/" + startMonth + "/" + startDay;
        date = date + " - " + endYear + "/" + endMonth + "/" + endDay;
        sedateTV.setText(date);
    }

    private void setupUI() {
        sedateTV = findViewById(R.id.sedateTV);
        editStartdateBTN = findViewById(R.id.editstartdateBTN);
        editEnddateBTN = findViewById(R.id.editenddateBTN);
        addgroupBTN = findViewById(R.id.addgroupBTN);
        saveBTN = findViewById(R.id.saveBTN);
        deleteBTN = findViewById(R.id.deleteBTN);
        coursenameET = findViewById(R.id.cnameET);
        courseidET = findViewById(R.id.cidET);
        instructorET = findViewById(R.id.instructorIDET);

        saveBTN.setOnClickListener(v -> saveCourse());
        addgroupBTN.setOnClickListener(v -> handleAddGroup());

    }
    private void handleDeleteCourse() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (String instructor : instructors) {
            db.collection("CourseGroups").document(docID + instructor)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("CC DeleteCourse", "Group deleted");
                    })
                    .addOnFailureListener(e -> {
                        Log.d("CC DeleteCourse", "Error deleting group");
                    });
        }
        db.collection("Courses").document(docID)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("CC DeleteCourse", "Course deleted");
                    Toast.makeText(this, "Course deleted", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, HomePage.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Log.d("CC DeleteCourse", "Error deleting course");
                    Toast.makeText(this, "Error deleting course", Toast.LENGTH_SHORT).show();
                });
    }
    private void handleAddGroup() {
        String instructorID = instructorET.getText().toString();
        if (docID == null) {
            Toast.makeText(this, "Please save course first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (instructorID.isEmpty()) {
            Toast.makeText(this, "Please fill in instructor ID", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(instructorID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if (Objects.equals(documentSnapshot.getString("accountType"), "Instructor")) {
                            HashMap<String, Object> data = new HashMap<>();
                            data.put("instructorID", instructorID);
                            data.put("students", new ArrayList<String>());
                            data.put("count", 0);
                            instructors.add(instructorID);
                            if (instructorsAdapter == null){
                                instructorsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, instructors);
                            }
                            instructorsAdapter.notifyDataSetChanged();
                            db.collection("CourseGroups").document(docID + instructorID)
                                    .set(data)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Group added", Toast.LENGTH_SHORT).show();
                                        db.collection("Courses").document(docID)
                                                .update("instructors", instructors)
                                                .addOnSuccessListener(aVoid1 -> {
                                                    Log.d("CC AddGroup", "Instructor added to course");
                                                    Toast.makeText(this, "Instructor added to course", Toast.LENGTH_SHORT).show();
                                                    makeGeneralPost(docID + instructorID);
                                                })
                                                .addOnFailureListener(e -> Log.d("CC AddGroup", "Error adding instructor to course"));
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error adding group", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(this, "User is not an instructor", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Instructor not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking instructor", Toast.LENGTH_SHORT).show();
                });
    }
    private void makeGeneralPost(String id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("title", "General Post");
        data.put("content", "This is a general post");
        data.put("date", new Date());
        data.put("commentsCount", 0);
        db.collection("CourseGroups").document(id).collection("Posts")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    Log.d("CC MakeGeneralPost", "General post added");
                })
                .addOnFailureListener(e -> {
                    Log.d("CC MakeGeneralPost", "Error adding general post");
                });
    }
    private void fetchUI(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Courses").document(docID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> data = documentSnapshot.getData();
                        coursenameET.setText(documentSnapshot.getString("coursename"));
                        courseidET.setText(documentSnapshot.getString("courseid"));
                        Date startDate = documentSnapshot.getDate("startdate");
                        Date endDate = documentSnapshot.getDate("enddate");
                        startYear = startDate.getYear() + 1900;
                        startMonth = startDate.getMonth();
                        startDay = startDate.getDay();
                        endYear = endDate.getYear() + 1900;
                        endMonth = endDate.getMonth();
                        endDay = endDate.getDay();
                        updateDate(false);
                        instructors = (ArrayList<String>) data.get("instructors");
                        instructorsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, instructors);
                        instructorsLV = findViewById(R.id.instructorsLV);
                        instructorsLV.setAdapter(instructorsAdapter);
                        instructorsLV.setOnItemClickListener((adapterView, view, i, l) -> {
                            if (uid.equals(instructors.get(i))){
                                Intent intent = new Intent(this, CourseGroupPage.class);
                                intent.putExtra("docID", docID + instructors.get(i));
                                startActivity(intent);
                            }else {
                                Toast.makeText(this, "You are not the Instructor of this course", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else {
                        Toast.makeText(this, "Course not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching course", Toast.LENGTH_SHORT).show();
                });
    }
}