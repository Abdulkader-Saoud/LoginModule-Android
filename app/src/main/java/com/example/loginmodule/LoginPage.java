package com.example.loginmodule;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class LoginPage extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private  EditText emailET, passwordET;

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
        loginButton.setOnClickListener(this::onClickLogin);
        Button registerButton = findViewById(R.id.button2);
        registerButton.setOnClickListener(this::onClickRegister);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Toast toast = Toast.makeText(this, "User in session", Toast.LENGTH_SHORT);
            toast.show();
//            Intent intent = new Intent(this, CourseList.class);
//            startActivity(intent);
        }
    }

    public void onClickLogin(View view) {
        String emailstr, passwordstr;
        emailET = (EditText) findViewById(R.id.emailET);
        passwordET = (EditText) findViewById(R.id.passET);
        emailstr = emailET.getText().toString();
        passwordstr = passwordET.getText().toString();

        if(emailstr.isEmpty() || passwordstr.isEmpty()){
            Toast toast = Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        mAuth.signInWithEmailAndPassword(emailstr, passwordstr)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user == null) {
                                Toast toast = Toast.makeText(LoginPage.this, "User not found.",
                                        Toast.LENGTH_SHORT);
                                toast.show();
                                return;
                            }
                            if (!user.isEmailVerified()) {
                                Toast toast = Toast.makeText(LoginPage.this, "Verify your email.",
                                        Toast.LENGTH_SHORT);
                                toast.show();
                                return;
                            }
                            Intent intent = new Intent(LoginPage.this, RegisterPage.class);
                            startActivity(intent);
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginPage.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    public void onClickRegister(View view) {
        String emailstr, passwordstr;
        emailET = (EditText) findViewById(R.id.emailET);
        passwordET = (EditText) findViewById(R.id.passET);
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
        mAuth.createUserWithEmailAndPassword(emailstr, passwordstr)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user == null) {
                            Toast toast = Toast.makeText(LoginPage.this, "User not found.",
                                    Toast.LENGTH_SHORT);
                            toast.show();
                            return;
                        }
                        user.sendEmailVerification();
                        Toast toast = Toast.makeText(LoginPage.this, "Verify your email.",
                                Toast.LENGTH_SHORT);
                        toast.show();

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