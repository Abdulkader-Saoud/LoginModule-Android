package com.example.loginmodule.Course;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginmodule.HomePage;
import com.example.loginmodule.Post.FSM;
import com.example.loginmodule.Post.Post;
import com.example.loginmodule.Post.PostAdapter;
import com.example.loginmodule.R;
import com.example.loginmodule.Report.CreateReport;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoursePage extends AppCompatActivity {

    private TextView courseNameTV,courseCodeTV,courseSTDCountTV,courseINSTV;
    private ImageButton sendMailBTN;
    private Course course;
    private String instructorID,instructorEmail;
    private String uid, accountType;
    private RecyclerView postRV;
    private ProgressBar progressBar;
    private ConstraintLayout dataLayout;
    private ArrayList<Post> posts = new ArrayList<>();
    private PostAdapter postAdapter;
    private Button addPostBTN;
    private EditText postTitleET;
    private RadioGroup radioFilterGroup;
    private Boolean onlySub = false;

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

        radioFilterGroup = findViewById(R.id.radioFilterGroup);
        radioFilterGroup.check(R.id.radio_none);
        radioFilterGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d("CoursePage", "id " + checkedId);
                if (R.id.radio_none == checkedId )
                    onlySub = false;
                else
                    onlySub = true;
                getINSID();
            }
        });
        courseNameTV = findViewById(R.id.courseNameTV);
        courseCodeTV = findViewById(R.id.courseCodeTV);
        courseSTDCountTV = findViewById(R.id.courseSTDCountTV);
        courseINSTV = findViewById(R.id.courseINSTV);
        sendMailBTN = findViewById(R.id.sendMailBTN);
        postRV = findViewById(R.id.postsRV);
        addPostBTN = findViewById(R.id.addPostBTN);
        postTitleET = findViewById(R.id.postTitleET);

        addPostBTN.setOnClickListener(v -> handleAddPost());

        course = (Course) getIntent().getSerializableExtra("course");

        courseNameTV.setText(course.getCourseName());
        courseCodeTV.setText(course.getCourseCode());
        if (accountType.equals("Instructor")) {
            sendMailBTN.setVisibility(View.GONE);
        }
//        else {
//            postCard = findViewById(R.id.postCard);
//            postCard.setOnClickListener(v -> {
//                Intent intent = new Intent(this, CreateReport.class);
//                intent.putExtra("courseName", course.getCourseName());
//                startActivity(intent);
//            });
//        }

        getINSID();
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
                            Log.d("CoursePage","ins ID" + instructorID);
                            getPosts();
                            Log.d("CoursePage","getting posts");
                            break;
                        }
                    }

                } else {
                    System.out.println("Error getting documents: " + task.getException());
                }
            });
        }
        // Dont need this
        else if (accountType.equals("Instructor")) {
            db.collection("CourseGroups").document(course.getCourseCode() + uid).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    instructorID = document.get("instructorID").toString();
                    courseSTDCountTV.setText(document.get("count").toString());
                    getPosts();
                } else {
                    System.out.println("Error getting documents: " + task.getException());
                }
            });
        }
    }
    private void getPosts(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        posts = new ArrayList<>();
        db.collection("CourseGroups").document(course.getCourseCode() + instructorID).collection("Posts").orderBy("date") .get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                List<DocumentSnapshot> postDocuments = task1.getResult().getDocuments();
                Log.d("CoursePage","posts count " + postDocuments.size());
                for (DocumentSnapshot postDocument : postDocuments) {
                    Post post = new Post(postDocument.getId(), postDocument.get("title").toString(), postDocument.getDate("date"), Integer.parseInt(postDocument.get("commentsCount").toString()), postDocument.getReference().getPath());
                    ArrayList<String> subs = (ArrayList<String>) postDocument.get("subs");
                    if (subs != null){
                        int j = 0;
                        while (j < subs.size()){
                            if (subs.get(j).equals(FSM.getFsmToken())){
                                post.setIsSub(true);
                                break;
                            }
                            j += 1;
                        }
                    }
                    posts.add(post);
                }
                if (!posts.isEmpty()){
                    postAdapter = new PostAdapter(posts,onlySub);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                    postRV.setLayoutManager(linearLayoutManager);
                    postRV.setAdapter(postAdapter);
                }
                getInstructorData(instructorID);
            } else {
                Intent intent = new Intent(CoursePage.this,HomePage.class);
                startActivity(intent);
                Log.e("CoursePage","Cant fetch posts.");
            }
        });
    }
    private void getInstructorData(String instructorID){
        FirebaseFirestore  db = FirebaseFirestore.getInstance();
        Log.d("CoursePage","Fetching ins data");
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
    private void handleAddPost(){
        String postTitle = postTitleET.getText().toString();
        if (postTitle.isEmpty()) {
            postTitleET.setError("Title is required");
            postTitleET.requestFocus();
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("title", postTitle);
        data.put("date", new Date());
        data.put("commentsCount", 0);
        progressBar.setVisibility(View.VISIBLE);
        dataLayout.setVisibility(View.GONE);
        db.collection("CourseGroups").document(course.getCourseCode() + instructorID).collection("Posts").document().set(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Post added successfully", Toast.LENGTH_SHORT).show();
                postTitleET.setText("");
                posts.clear();
                getPosts();
            } else {
                Toast.makeText(this, "Failed to add post", Toast.LENGTH_SHORT).show();
            }
        });
    }
}