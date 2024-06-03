package com.example.loginmodule.Course;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.loginmodule.PollQuesModule.PollsListActivity;
import com.example.loginmodule.R;
import com.example.loginmodule.Report.CreateReport;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class CoursePage extends AppCompatActivity {

    private TextView courseNameTV,courseCodeTV,courseSTDCountTV,courseINSTV;
    private ImageButton sendMailBTN;
    private Course course;
    private String instructorID,instructorEmail;
    private String uid, accountType;
    private CardView postCard, pollsLstBtn;
    private ProgressBar progressBar;
    private ConstraintLayout dataLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_course_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Context context = getApplicationContext();
        uid = context.getSharedPreferences("login", Context.MODE_PRIVATE).getString(context.getString(R.string.prefKey_stdID), null);
        accountType = context.getSharedPreferences("login", Context.MODE_PRIVATE).getString(context.getString(R.string.prefKey_accType), null);

        dataLayout = findViewById(R.id.CL);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        dataLayout.setVisibility(View.GONE);

        courseNameTV = findViewById(R.id.courseNameTV);
        courseCodeTV = findViewById(R.id.courseCodeTV);
        courseSTDCountTV = findViewById(R.id.courseSTDCountTV);
        courseINSTV = findViewById(R.id.courseINSTV);
        sendMailBTN = findViewById(R.id.sendMailBTN);
        pollsLstBtn = findViewById(R.id.pollListBtnId);

        course = (Course) getIntent().getSerializableExtra("course");

        pollsLstBtn.setOnClickListener(this::forwardTo);

        courseNameTV.setText(course.getCourseName());
        courseCodeTV.setText(course.getCourseCode());
        if (accountType.equals("Instructor")) {
            sendMailBTN.setVisibility(View.GONE);
        }
        else {
            postCard = findViewById(R.id.postCard);
            postCard.setOnClickListener(v -> {
                Intent intent = new Intent(this, CreateReport.class);
                intent.putExtra("courseName", course.getCourseName());
                startActivity(intent);
            });
        }
        getINSID();
    }

    private void forwardTo(View view){
        Intent intent = new Intent(CoursePage.this, PollsListActivity.class);
        intent.putExtra("docID",course.getCourseCode()+instructorID);
        startActivity(intent);
    }

    private void getINSID() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (accountType.equals("Student")) {
            db.collection("CourseGroups").whereArrayContains("students", uid).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> documents = task.getResult().getDocuments();
                    if (documents.isEmpty())
                        return;
                    for (DocumentSnapshot document : documents) {
                        if (document.getId().contains(course.getCourseCode())) {
                            instructorID = document.get("instructorID").toString();
                            courseSTDCountTV.setText(document.get("count").toString());
                            getInstructorData(instructorID);
                            break;
                        }
                    }

                } else {
                    System.out.println("Error getting documents: " + task.getException());
                }
            });
        }
        else if (accountType.equals("Instructor")) {
            db.collection("CourseGroups").document(course.getCourseCode() + uid).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    instructorID = document.get("instructorID").toString();
                    courseSTDCountTV.setText(document.get("count").toString());
                    getInstructorData(instructorID);
                } else {
                    System.out.println("Error getting documents: " + task.getException());
                }
            });
        }
    }
    private void getInstructorData(String instructorID){
        FirebaseFirestore  db = FirebaseFirestore.getInstance();
        db.collection("Users").document(instructorID).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Map<String,Object> instructorData = task.getResult().getData();
                if (instructorData == null) {
                    Log.d("CoursePage", "No such document");
                    return;
                }
                courseINSTV.setText(instructorData.get("fname").toString() + " " + instructorData.get("lname").toString());
                instructorEmail = instructorData.get("ACCemail").toString();
                sendMailBTN.setOnClickListener(v -> {
                    handleSendMail();
                });
                progressBar.setVisibility(View.GONE);
                dataLayout.setVisibility(View.VISIBLE);
            }
        });
    }
    private void handleSendMail(){
        String subject = "Regarding " + course.getCourseName();
        String message = "Hello, I am a student in your course ("+ uid + ")" + course.getCourseName();
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + instructorEmail));
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
        }
    }
}