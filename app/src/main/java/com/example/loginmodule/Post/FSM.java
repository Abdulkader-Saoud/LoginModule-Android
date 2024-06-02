package com.example.loginmodule.Post;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

public class FSM {
    private static String fsmToken = null;
    public static String getFsmToken() {
        return fsmToken;
    }
    public FSM() {
        if (fsmToken == null) {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.w("Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        fsmToken = task.getResult();
                    });

        }
    }

}
