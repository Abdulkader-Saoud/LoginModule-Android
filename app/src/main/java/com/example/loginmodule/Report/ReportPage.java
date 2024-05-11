package com.example.loginmodule.Report;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.loginmodule.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReportPage extends AppCompatActivity {
    private ProgressBar progressBar;
    private ConstraintLayout dataLayout;
    private TextView scopeTV,courseTV,subjectTVValue,bodyTV;
    private String reportID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dataLayout = findViewById(R.id.CL);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        dataLayout.setVisibility(View.GONE);

        Intent intent = getIntent();
        reportID = intent.getStringExtra("reportID");

        scopeTV = findViewById(R.id.scopeTV);
        courseTV = findViewById(R.id.courseTV);
        subjectTVValue = findViewById(R.id.subjectTVValue);
        bodyTV = findViewById(R.id.bodyTV);
        fetchReport();

    }
    private void fetchReport(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Reports")
                .document(reportID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        scopeTV.setText(task.getResult().getString("scope"));
                        courseTV.setText(task.getResult().getString("course"));
                        subjectTVValue.setText(task.getResult().getString("subject"));
                        bodyTV.setText(task.getResult().getString("body"));
                    }
                    progressBar.setVisibility(View.GONE);
                    dataLayout.setVisibility(View.VISIBLE);
                });
    }
}