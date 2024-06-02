package com.example.loginmodule;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.loginmodule.Course.CoursesPage;
import com.example.loginmodule.Post.FSM;
import com.example.loginmodule.Profile.ProfilePage;
import com.example.loginmodule.Report.CreateReport;
import com.example.loginmodule.Report.ReportsPage;

public class HomePage extends AppCompatActivity {
    private String accountType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (FSM.getFsmToken() == null)
            new FSM();


        Context context = getApplicationContext();
        accountType = context.getSharedPreferences("login", Context.MODE_PRIVATE).getString(context.getString(R.string.prefKey_accType), null);

        ImageButton profileButton = findViewById(R.id.buttonProfile);
        ImageButton coursesButton = findViewById(R.id.buttonCourses);
        ImageButton reportButton = findViewById(R.id.buttonReport);


        profileButton.setOnClickListener(this::onClickProfile);
        coursesButton.setOnClickListener(this::onClickCourses);
        reportButton.setOnClickListener(this::onClickReport);

    }

    private void onClickProfile(View view){
        Intent intent = new Intent(this, ProfilePage.class);
        startActivity(intent);
    }

    private void onClickCourses(View view){
        Intent intent = new Intent(this, CoursesPage.class);
        startActivity(intent);
    }

    private void onClickReport(View view){
        Intent intent;
        if (accountType.equals("Instructor")){
            intent = new Intent(this, ReportsPage.class);
        }
        else {
            intent = new Intent(this, CreateReport.class);
        }
        startActivity(intent);
    }
}
