package com.example.loginmodule.Profile;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.loginmodule.HomePage;
import com.example.loginmodule.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Map;

public class LoginPage extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private  EditText emailET, passwordET;
    private ProgressBar progressBar;
    SharedPreferences sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        Button loginButton = findViewById(R.id.button);
        Button registerButton = findViewById(R.id.button2);
        progressBar = findViewById(R.id.progressBar);

        loginButton.setOnClickListener(this::onClickLogin);
        registerButton.setOnClickListener(this::onClickRegister);

        sharedPref = getSharedPreferences(getString(R.string.prefName_login), Context.MODE_PRIVATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkIfRegistered();
    }

    //Check if user is registered in db with this id
    private void checkIfRegistered(){
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            return;
        }
//        if (!user.isEmailVerified()) {
//            return;
//        }
        if (sharedPref.getString(getString(R.string.prefKey_stdID), null) != null && sharedPref.getString(getString(R.string.prefKey_email), null) != null){
            Intent intent = new Intent(LoginPage.this, HomePage.class);
            startActivity(intent);
            return;
        }
        String Uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users")
                .whereEqualTo("uid", Uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        QueryDocumentSnapshot aVoid;
                        try {
                            aVoid = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                        } catch (Exception e) {
                            Intent intent = new Intent(LoginPage.this, RegisterPage.class);
                            startActivity(intent);
                            return;
                        }
                        Map<String, Object> data = aVoid.getData();
                        SharedPreferences.Editor editor = sharedPref.edit();

                        editor.putString(getString(R.string.prefKey_fName), (String) data.get("fname"));
                        editor.putString(getString(R.string.prefKey_stdID), (String) data.get("stdID"));
                        editor.putString(getString(R.string.prefKey_accType), (String) data.get("accountType"));
                        editor.apply();
                        if (sharedPref.getString(getString(R.string.prefKey_accType), null) == null){
                            Toast toast = Toast.makeText(LoginPage.this, "NULLL",
                                    Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        else {
                            Intent intent = new Intent(LoginPage.this, HomePage.class);
                            startActivity(intent);
                        }

                    }
                    else {
                        Intent intent = new Intent(LoginPage.this, RegisterPage.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener( e -> {
                    Toast toast = Toast.makeText(LoginPage.this, "Something went wrong.",
                            Toast.LENGTH_SHORT);
                    toast.show();
                });
    }
    public void onClickLogin(View view) {
        String emailstr, passwordstr;
        emailET =  findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passET);
        emailstr = emailET.getText().toString();
        passwordstr = passwordET.getText().toString();


        if(emailstr.isEmpty() || passwordstr.isEmpty()){
            Toast toast = Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(emailstr, passwordstr)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user == null) {
                            Toast toast = Toast.makeText(LoginPage.this, "User not found.",
                                    Toast.LENGTH_SHORT);
                            toast.show();
                            return;
                        }
//                        if (!user.isEmailVerified()) {
//                            Toast toast = Toast.makeText(LoginPage.this, "Verify your email.",
//                                    Toast.LENGTH_SHORT);
//                            toast.show();
//                            return;
//                        }

                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.prefKey_email), emailstr);
                        editor.apply();

                        checkIfRegistered();
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginPage.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public void onClickRegister(View view) {
        String emailstr, passwordstr;
        emailET =  findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passET);
        emailstr = emailET.getText().toString();
        passwordstr = passwordET.getText().toString();


        if(emailstr.isEmpty() || passwordstr.isEmpty()){
            Toast toast = Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        else if (passwordstr.length() < 6) {
            Toast toast = Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        else if (!emailstr.contains("@") || !emailstr.contains("yildiz.edu")){
            Toast toast = Toast.makeText(this, "Only yildiz mails are accepted", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(emailstr, passwordstr)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user == null) {
                            Toast toast = Toast.makeText(LoginPage.this, "User not found.",
                                    Toast.LENGTH_SHORT);
                            toast.show();
                            return;
                        }
//                        user.sendEmailVerification();
//                        Toast toast = Toast.makeText(LoginPage.this, "Verify your email.",
//                                Toast.LENGTH_SHORT);
//                        toast.show();

                    } else {
                        // If sign in fails, display a message to the user.
                        FirebaseAuthException firebaseAuthException = (FirebaseAuthException) task.getException();
                        if (firebaseAuthException == null) {
                            Toast toast = Toast.makeText(LoginPage.this, "Authentication Failed.",
                                    Toast.LENGTH_SHORT);
                            toast.show();
                            return;
                        }
                        String errorCode = firebaseAuthException.getErrorCode();
                        Log.w(TAG, "Error code: " + errorCode);
                        if (errorCode.equals("ERROR_EMAIL_ALREADY_IN_USE")) {
                            Toast toast = Toast.makeText(LoginPage.this, "Email already in use.",
                                    Toast.LENGTH_SHORT);
                            toast.show();
                        } else {
                            Toast toast = Toast.makeText(LoginPage.this, "Authentication Failed.",
                                    Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                });
    }
}