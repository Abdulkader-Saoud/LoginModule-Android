package com.example.loginmodule;

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
import com.example.loginmodule.Profile.ProfilePage;
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
        ImageButton profileButton = findViewById(R.id.buttonProfile);
        ImageButton coursesButton = findViewById(R.id.buttonCourses);
        profileButton.setOnClickListener(this::onClickProfile);
        coursesButton.setOnClickListener(this::onClickCourses);

    }

    public void onClickProfile(View view){
        Intent intent = new Intent(this, ProfilePage.class);
        startActivity(intent);
    }

    public void onClickCourses(View view){
        Intent intent = new Intent(this, CoursesPage.class);
        startActivity(intent);
    }
}
