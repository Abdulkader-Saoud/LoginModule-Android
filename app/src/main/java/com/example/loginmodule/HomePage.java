package com.example.loginmodule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.loginmodule.Course.CoursesPage;
import com.example.loginmodule.Profile.LoginPage;
import com.example.loginmodule.Profile.ProfilePage;
import com.google.firebase.auth.FirebaseAuth;

public class HomePage extends AppCompatActivity {
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
        Button logoutButton = findViewById(R.id.buttonLogout);
        Button profileButton = findViewById(R.id.buttonProfile);
        Button coursesButton = findViewById(R.id.buttonCourses);
        logoutButton.setOnClickListener(this::onClickLogout);
        profileButton.setOnClickListener(this::onClickProfile);
        coursesButton.setOnClickListener(this::onClickCourses);

    }

    public void onClickProfile(View view){
        Intent intent = new Intent(this, ProfilePage.class);
        startActivity(intent);
    }
    public void onClickLogout(View view){
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginPage.class);
        startActivity(intent);
    }
    public void onClickCourses(View view){
        Intent intent = new Intent(this, CoursesPage.class);
        startActivity(intent);
    }
}
