package com.example.loginmodule.AttendanceModule;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginmodule.Course.Course;
import com.example.loginmodule.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PastAttendance extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PastAttendanceAdapter adapter;
    private ArrayList<AttendanceItem> attendanceList = new ArrayList<>();
    private String instructorID;
    private Course course;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_attendance);

        Intent intent = getIntent();
        instructorID = getIntent().getStringExtra("instructorID");
        course = (Course) intent.getSerializableExtra("course");

        recyclerView = findViewById(R.id.recyclerViewPastAttendance);
        adapter = new PastAttendanceAdapter(attendanceList);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        getPastAttendance();
    }

    private void getPastAttendance(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("CourseGroups")
                .document(course.getCourseCode() + instructorID)
                .collection("pastAttendance")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                String attendanceDate = documentSnapshot.getId();
                                attendanceList.add(new AttendanceItem(course.getCourseName(), course.getCourseCode(), attendanceDate));
                                Log.d("AttendancePage", "Attendance Date: " + attendanceDate);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.e("AttendancePage", "Dokümanlar alınırken hata oluştu", task.getException());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("AttendancePage", "Dokümanlar alınırken hata oluştu", e);
                    }
                });
    }

    public void exportAttendance(AttendanceItem attendanceItem) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String documentPath = course.getCourseCode() + instructorID + "/pastAttendance/" + attendanceItem.getAttendanceDate();

        db.document("CourseGroups/" + documentPath)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {

                            List<String> students = (List<String>) documentSnapshot.get("students");

                            // Ders adı ve tarihine göre dosya adını oluştur
                            String fileName = course.getCourseName() + "_" + attendanceItem.getAttendanceDate() + ".csv";

                            exportToCSV(students, fileName);
                        } else {
                            Log.d("PastAttendance", "Böyle bir belge yok");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("PastAttendance", "Belge alınırken hata oluştu", e);
                    }
                });
    }

    private void exportToCSV(List<String> students, String fileName) {
        File file = new File(getExternalFilesDir(null), fileName);

        try {
            FileWriter writer = new FileWriter(file);

            //Başlangıc
            writer.append("Ogrenci Numara\n");

            //öğrenci numaraları
            for (String student : students) {
                writer.append(student).append("\n");
            }

            writer.flush();
            writer.close();

            Toast.makeText(this, "Başarılı Olarak Aktarıldı", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("PastAttendance", "başarısız oldu", e);
            Toast.makeText(this, "Başarısız", Toast.LENGTH_SHORT).show();
        }
    }
}