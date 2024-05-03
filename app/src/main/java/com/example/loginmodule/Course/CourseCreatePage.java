package com.example.loginmodule.Course;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.loginmodule.R;

public class CourseCreatePage extends AppCompatActivity implements DatePickerFragment.DateSetListener {
    private Button editStartdateBTN;
    private Button editEnddateBTN;
    private TextView sedateTV;
    private String seChoice;
    private int startYear, startMonth, startDay;
    private int endYear, endMonth, endDay;
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
        sedateTV = findViewById(R.id.sedateTV);
        editStartdateBTN = findViewById(R.id.editstartdateBTN);
        editEnddateBTN = findViewById(R.id.editenddateBTN);

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
}