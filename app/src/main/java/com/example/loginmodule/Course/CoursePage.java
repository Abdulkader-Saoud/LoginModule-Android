package com.example.loginmodule.Course;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.loginmodule.AttendanceModule.AttendancePage;
import com.example.loginmodule.HomePage;
import com.example.loginmodule.Post.FSM;
import com.example.loginmodule.Post.Post;
import com.example.loginmodule.Post.PostAdapter;

import com.example.loginmodule.PollQuesModule.PollsListActivity;
import com.example.loginmodule.R;
import com.example.loginmodule.Report.CreateReport;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CoursePage extends AppCompatActivity {

    private TextView courseNameTV,courseCodeTV,courseSTDCountTV,courseINSTV;
    private ImageButton sendMailBTN;
    private Course course;
    private String instructorID,instructorEmail;
    private String uid, accountType;
    private RecyclerView postRV;
    private CardView pollsLstBtn;
    private ProgressBar progressBar;
    private ConstraintLayout dataLayout,insPart,stdPart;
    private ArrayList<Post> posts = new ArrayList<>();
    private PostAdapter postAdapter;
    private Button addPostBTN, attendanceBTN, attendanceJoinBTN;
    private EditText postTitleET;
    private RadioGroup radioFilterGroup;
    private Boolean onlySub = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private FusedLocationProviderClient fusedLocationProviderClient;


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
        insPart = findViewById(R.id.insPart);
        stdPart = findViewById(R.id.stdPart);

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
        pollsLstBtn = findViewById(R.id.pollListBtnId);
        postRV = findViewById(R.id.postsRV);
        addPostBTN = findViewById(R.id.addPostBTN);
        postTitleET = findViewById(R.id.postTitleET);

        addPostBTN.setOnClickListener(v -> handleAddPost());


        course = (Course) getIntent().getSerializableExtra("course");

        pollsLstBtn.setOnClickListener(this::forwardTo);

        courseNameTV.setText(course.getCourseName());
        courseCodeTV.setText(course.getCourseCode());
        if (accountType.equals("Instructor")) {
            sendMailBTN.setVisibility(View.GONE);
            insPart.setVisibility(View.VISIBLE);
            stdPart.setVisibility(View.GONE);
        }
        getINSID();

        //Yoklama
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        attendanceBTN = findViewById(R.id.attendanceBTN);
        attendanceJoinBTN = findViewById(R.id.attendanceJoinBTN);
        if (accountType.equals("Instructor")) {
            attendanceBTN.setVisibility(View.VISIBLE);
            attendanceJoinBTN.setVisibility(View.GONE);

            attendanceBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CoursePage.this, AttendancePage.class);
                    intent.putExtra("course", course);
                    intent.putExtra("instructorID", instructorID);
                    startActivity(intent);
                }
            });
        } else if (accountType.equals("Student")) {
            attendanceBTN.setVisibility(View.GONE);
            attendanceJoinBTN.setVisibility(View.VISIBLE);
            attendanceJoinBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    joinAttendance();
                }
            });
        }
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
        else if (accountType.equals("Instructor")) {
            db.collection("CourseGroups").document(course.getCourseCode() + uid).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    instructorID = document.get("instructorID").toString();
                    courseSTDCountTV.setText(document.get("count").toString());
                    progressBar.setVisibility(View.GONE);
                    dataLayout.setVisibility(View.VISIBLE);
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

    //Yoklama

    private void joinAttendance() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Location studentLocation = task.getResult();
                        getTeacherLocation(studentLocation);
                    } else {
                        Log.e("CoursePage", "Konum alınamadı", task.getException());
                        Toast.makeText(CoursePage.this, "Konum Alınamadı. Konumunuzu Açınız", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void saveStudentLocationToFirestore() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Location location = task.getResult();
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    String studentID = uid;
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Map<String, Object> studentLocation = new HashMap<>();
                    studentLocation.put("latitude", latitude);
                    studentLocation.put("longitude", longitude);
                    db.collection("CourseGroups")
                            .document(course.getCourseCode() + instructorID)
                            .collection("Attendance")
                            .document(studentID)
                            .set(studentLocation)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("CoursePage", "Öğrenci konumu başarıyla kaydedildi");
                                    Toast.makeText(CoursePage.this, "Başarıyla Kaydedildi", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("CoursePage", "Öğrenci konumu kaydedilirken hata oluştu", e);
                                    Toast.makeText(CoursePage.this, "Kayıt Başarısız", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Log.e("CoursePage", "Konum alınamadı", task.getException());
                    Toast.makeText(CoursePage.this, "Location not available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkDistanceFromTeacher(Location studentLocation, Location teacherLocation) {
        float distanceInMeters = studentLocation.distanceTo(teacherLocation);
        if (distanceInMeters <= 30) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("CourseGroups")
                    .document(course.getCourseCode() + instructorID)
                    .collection("TeacherLocation")
                    .document("location")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                Date firestoreTime = documentSnapshot.getDate("time");
                                if (firestoreTime != null) {
                                    Date currentTime = new Date();
                                    long differenceInMillis = currentTime.getTime() - firestoreTime.getTime();
                                    long tenMinutesInMillis = 10 * 60 * 1000; // 10 dakika milisaniye cinsinden
                                    if (differenceInMillis > tenMinutesInMillis) {
                                        // 10 dakikadan fazla süredir geçmiş, öğrenci kayıt olamaz
                                        Toast.makeText(CoursePage.this, "Son yoklamadan sonra 10 dakikadan fazla zaman geçmiş yoklamanız kaydedilemedi", Toast.LENGTH_SHORT).show();
                                    } else {
                                        saveStudentLocationToFirestore();
                                    }
                                } else {
                                    Log.e("CoursePage", "Firestore time is null");
                                }
                            } else {
                                Log.e("CoursePage", "Document doesn't exist for student ID: " + uid);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("CoursePage", "Error getting time from Firestore", e);
                        }
                    });
        } else {
            // Öğrenci kaydı yapılamaz, çünkü öğrenci öğretmenden 30 metreden uzakta
            Toast.makeText(this, "You are too far from the teacher to record attendance.", Toast.LENGTH_SHORT).show();
        }
    }

    private void getTeacherLocation(Location studentLocation) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("CourseGroups")
                .document(course.getCourseCode() + instructorID)
                .collection("TeacherLocation")
                .document("location")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            double teacherLatitude = documentSnapshot.getDouble("latitude");
                            double teacherLongitude = documentSnapshot.getDouble("longitude");
                            Location teacherLocation = new Location("Teacher");
                            teacherLocation.setLatitude(teacherLatitude);
                            teacherLocation.setLongitude(teacherLongitude);
                            checkDistanceFromTeacher(studentLocation, teacherLocation);
                        } else {
                            Log.d("CoursePage", "Teacher location document doesn't exist");
                            // Öğretmenin konumu bulunamadı
                            // Burada gerekli hata işlemleri yapılabilir
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("CoursePage", "Error getting teacher location", e);
                    }
                });
    }

}