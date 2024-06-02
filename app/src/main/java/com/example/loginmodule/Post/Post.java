package com.example.loginmodule.Post;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.loginmodule.Course.Course;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Post implements Serializable {
    private static final long serialVersionUID = 1001;
    private String id,title;
    private Date date;
    private int commentsCount= 0;
    private String path;
    private Boolean isSub = false;

    public Post(String id, String title, Date date , int commentsCount, String path) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.commentsCount = commentsCount;
        this.path = path;

    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Date getDate() {
        return date;
    }
    public void setIsSub(Boolean isSub){
        this.isSub = isSub;
    }
    public Boolean getIsSub(){
        return isSub;
    }

    public int getCommentsCount() {
        return commentsCount;
    }
    public String getPath() {
        return path;
    }

    public void notifySubs(){
        Map<String,String> data = new HashMap<>();
        data.put("postPath",path);
        data.put("sender", FSM.getFsmToken());
        FirebaseFunctions.getInstance()
                .getHttpsCallable("notifySubscribers")
                .call(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Notification", "Notifications sent successfully");
                    } else {
                        Log.e("Notification", "Error sending notifications", task.getException());
                    }
                });
    }
}
