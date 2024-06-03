package com.example.loginmodule.AttendanceModule;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.loginmodule.Course.Course;
import com.example.loginmodule.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AttendancePage extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private Button attendanceButton, pastAttendance;
    private TextView timerTextView;
    private String instructorID;
    private Course course;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private CountdownService countdownService;
    private boolean isBound = false;
    SimpleDateFormat dateFormat, timeFormat;
    private Date currentTime;
    private ListView listView;
    private int attendanceCount = 0;
    private TextView attendanceCountTextView;
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CountdownService.LocalBinder binder = (CountdownService.LocalBinder) service;
            countdownService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long timeRemaining = intent.getLongExtra("timeRemaining", 0);
            updateTimerTextView(timeRemaining);
            if (timeRemaining == 0) {
                attendanceButton.setEnabled(true);
            }
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendance_page);

        attendanceButton = findViewById(R.id.attendanceB);
        pastAttendance = findViewById(R.id.pastAttendance);
        timerTextView = findViewById(R.id.timerTextView);
        listView = findViewById(R.id.listViewAttendance);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Intent intent = getIntent();
        instructorID = getIntent().getStringExtra("instructorID");
        course = (Course) intent.getSerializableExtra("course");

        attendanceCountTextView = findViewById(R.id.attendanceCountTextView);
        attendanceCountTextView.setText("Count: " + attendanceCount);


        attendanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleStartAttendance();
            }
        });

        pastAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendancePage.this, PastAttendance.class);
                intent.putExtra("course", course);
                intent.putExtra("instructorID", instructorID);
                startActivity(intent);
            }
        });

        fetchAttendanceData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent serviceIntent = new Intent(this, CountdownService.class);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        registerReceiver(broadcastReceiver, new IntentFilter("CountdownService"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
        unregisterReceiver(broadcastReceiver);
    }

    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Location location = task.getResult();
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    saveTeacherLocationToFirestore(latitude, longitude);
                    Toast.makeText(AttendancePage.this, "Latitude: " + latitude + ", Longitude: " + longitude, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AttendancePage.this, "Konum alınamadı", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastKnownLocation();
            } else {
                Toast.makeText(this, "Konum izni reddedildi", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleStartAttendance() {
        currentTime = Calendar.getInstance().getTime();
        dateFormat  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        checkLocationPermissions();
        String currentDate = dateFormat.format(currentTime);
        String currentTimeString = timeFormat.format(currentTime);

        Intent serviceIntent = new Intent(this, CountdownService.class);
        serviceIntent.putExtra("instructorID", instructorID);
        serviceIntent.putExtra("course", course);
        serviceIntent.putExtra("currentDate", currentDate);
        serviceIntent.putExtra("currentTime", currentTimeString);

        startService(serviceIntent);
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastKnownLocation();
        }
    }

    private void saveTeacherLocationToFirestore(double latitude, double longitude) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentDate = dateFormat.format(currentTime);
        String currentTimeString = timeFormat.format(currentTime);
        Map<String, Object> teacherLocation = new HashMap<>();
        teacherLocation.put("latitude", latitude);
        teacherLocation.put("longitude", longitude);
        teacherLocation.put("date", currentDate);
        teacherLocation.put("time", currentTime);

        db.collection("CourseGroups")
                .document(course.getCourseCode() + instructorID)
                .collection("TeacherLocation")
                .document("location")
                .set(teacherLocation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("AttendancePage", "Öğretmen konumu başarıyla eklendi");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("AttendancePage", "Öğretmen konumu eklenirken hata oluştu", e);
                    }
                });

        ArrayList<String> students = new ArrayList<>();
        Map<String, Object> pastAttendance = new HashMap<>();
        pastAttendance.put("students", students);

        db.collection("CourseGroups")
                .document(course.getCourseCode() + instructorID)
                .collection("pastAttendance")
                .document(currentDate+ " " +currentTimeString)
                .set(pastAttendance)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("AttendancePage", "Geçmiş katılım başarıyla eklendi: " + dateFormat+timeFormat);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("AttendancePage", "Geçmiş katılım eklenirken hata oluştu", e);
                    }
                });
    }

    private void updateTimerTextView(long millisUntilFinished) {
        long minutes = (millisUntilFinished / 1000) / 60;
        long seconds = (millisUntilFinished / 1000) % 60;
        timerTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    private void fetchAttendanceData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("CourseGroups")
                .document(course.getCourseCode() + instructorID)
                .collection("Attendance")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<String> attendanceList = new ArrayList<>();
                            attendanceCount = task.getResult().size();
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {

                                String documentName = documentSnapshot.getId();
                                attendanceList.add(documentName);
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(AttendancePage.this, R.layout.list_item, attendanceList);
                            listView.setAdapter(adapter);
                            attendanceCountTextView.setText("Count: " + attendanceCount);
                        } else {
                            Log.e("AttendancePage", "Error getting documents", task.getException());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("AttendancePage", "Error getting documents", e);
                    }
                });
    }
}