package com.example.loginmodule.Course;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.loginmodule.R;

import java.util.ArrayList;

public class CourseGroupPage extends AppCompatActivity {
    private EditText studentIDET;
    private Button addstudentBTN;
    private ListView studentsLV;
    private ArrayList<String> instructorsList= new ArrayList<>();
    private ArrayAdapter<String> studentAdapter;

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
        studentIDET = findViewById(R.id.studentIDET);
        studentsLV = findViewById(R.id.studentsLV);
        addstudentBTN = findViewById(R.id.addstudentBTN);
        addstudentBTN.setOnClickListener(view -> addInstructor());

        instructorsList.add("Instructor 1");
        studentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, instructorsList);
        studentsLV.setAdapter(studentAdapter);
        studentsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                instructorsList.remove(i);
                studentsLV.setAdapter(studentAdapter);
            }
        });

    }
    private void addInstructor() {
        String instructorID = studentIDET.getText().toString();
        if(instructorID.isEmpty()){
            Toast toast = Toast.makeText(this, "Please enter instructor ID", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        instructorsList.add(instructorID);
        studentAdapter.notifyDataSetChanged();
        studentIDET.setText("");
    }
}