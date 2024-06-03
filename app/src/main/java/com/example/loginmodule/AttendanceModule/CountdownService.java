package com.example.loginmodule.AttendanceModule;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.loginmodule.Course.Course;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CountdownService extends Service {

    private PendingIntent pendingIntent;
    private static final String CHANNEL_ID = "CountdownServiceChannel";
    private final IBinder binder = new LocalBinder();
    private CountDownTimer countDownTimer;
    private long timeRemaining = 10 * 60 * 1000; // 10 dakika başlangıç süresi
    private Course course;
    private String instructorID, currentDate, currentTime;


    public class LocalBinder extends Binder {
        CountdownService getService() {
            return CountdownService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, AttendancePage.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Yoklama")
                .setContentText("Yoklama Devam Ediyor")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        instructorID = intent.getStringExtra("instructorID");
        course = (Course) intent.getSerializableExtra("course");
        currentDate = intent.getStringExtra("currentDate");
        currentTime = intent.getStringExtra("currentTime");
        startTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    public void startTimer() {
        countDownTimer = new CountDownTimer(timeRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                // Broadcast göndererek zamanı Activity'ye bildir
                Intent intent = new Intent("CountdownService");
                intent.putExtra("timeRemaining", timeRemaining);
                sendBroadcast(intent);
                updateNotificationText(timeRemaining);
            }

            private void updateNotificationText(long timeRemaining) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(CountdownService.this, CHANNEL_ID)
                            .setContentTitle("Yoklama Timer")
                            .setSmallIcon(android.R.drawable.ic_dialog_info)
                            .setContentIntent(pendingIntent); // Use the pendingIntent variable here
                    // Kalan süreyi dakika ve saniye cinsinden hesaplayıp göster
                    long minutes = timeRemaining / (1000 * 60);
                    long seconds = (timeRemaining / 1000) % 60;
                    String text = String.format("Kalan Süre: %02d:%02d", minutes, seconds);
                    builder.setContentText(text);

                    notificationManager.notify(1, builder.build());
                }
            }

            @Override
            public void onFinish() {
                timeRemaining = 0;
                saveDataToFirestore();
                deleteData();
                Intent intent = new Intent("CountdownService");
                intent.putExtra("timeRemaining", timeRemaining);
                sendBroadcast(intent);
                stopSelf();
            }
        }.start();


    }

    public long getTimeRemaining() {
        return timeRemaining;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Countdown Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private void saveDataToFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("CourseGroups")
                .document(course.getCourseCode() + instructorID)
                .collection("Attendance")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        ArrayList<String> studentNumbers = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            // Her belgeden öğrenci numaralarını alıp studentNumbers listesine ekleyin
                            // Örnek olarak, belgenin içerisindeki "studentNumber" alanını alıyoruz:
                            String studentNumber = documentSnapshot.getId();
                            if (studentNumber != null) {
                                studentNumbers.add(studentNumber);
                            }
                        }

                        // Tüm öğrenci numaralarını topladıktan sonra, Firestore'a ekleyin
                        Map<String, Object> attendanceData = new HashMap<>();
                        attendanceData.put("students", studentNumbers);

                        db.collection("CourseGroups")
                                .document(course.getCourseCode() + instructorID)
                                .collection("pastAttendance")
                                .document(currentDate + " " + currentTime)
                                .set(attendanceData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("AttendancePage", "Attendance data added successfully");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("AttendancePage", "Error adding attendance data", e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("AttendancePage", "Error getting documents", e);
                    }
                });
    }
    private void deleteData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("CourseGroups")
                .document(course.getCourseCode() + instructorID)
                .collection("Attendance")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            documentSnapshot.getReference().delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("DeleteData", "DocumentSnapshot successfully deleted!");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("DeleteData", "Error deleting document", e);
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DeleteData", "Error getting documents", e);
                    }
                });
    }
}