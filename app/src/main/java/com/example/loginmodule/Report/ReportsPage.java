package com.example.loginmodule.Report;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginmodule.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class ReportsPage extends AppCompatActivity {
    private ProgressBar progressBar;
    private ConstraintLayout dataLayout;
    private ArrayList<String> reportsIDSArray = new ArrayList<>();
    private ArrayList<String> reportsDatesArray = new ArrayList<>();
    private String uid;
    private RecyclerView reportsRV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reports_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dataLayout = findViewById(R.id.CL);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        dataLayout.setVisibility(View.GONE);

        reportsRV = findViewById(R.id.reportsRV);

        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        uid = sharedPreferences.getString(getString(R.string.prefKey_stdID), null);
        fetchReports();
    }
    private void fetchReports(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Reports")
                .whereArrayContains("recipients", uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            for (DocumentSnapshot document : task.getResult()) {
                                reportsIDSArray.add(document.getId());
                                Date date = document.getDate("Date");
                                String dateStr = (date.getYear() +1900) + "/" + date.getMonth()+ "/" + date.getDate();
                                reportsDatesArray.add(dateStr);
                            }
                        }
                        handlefetchReports();
                    } else {
                        Log.e("fetchCourses", "Error getting courses", task.getException());
                    }
                });
    }
    private void handlefetchReports(){
        progressBar.setVisibility(View.GONE);
        dataLayout.setVisibility(View.VISIBLE);
        ReportsAdapter reportsAdapter = new ReportsAdapter(reportsDatesArray, reportsIDSArray);
        reportsRV.setLayoutManager(new LinearLayoutManager(this));
        reportsRV.setAdapter(reportsAdapter);
    }

}