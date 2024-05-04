package com.example.loginmodule.Profile;

import static android.content.ContentValues.TAG;

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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterPage extends AppCompatActivity {
    private EditText fnameET, lnameET, stdIDET;
    private Map<String, Object> user = new HashMap<>();
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        fnameET = findViewById(R.id.etName);
        lnameET = findViewById(R.id.etSurname);
        stdIDET = findViewById(R.id.etSTDID);
        progressBar = findViewById(R.id.progressBar);

        Button registerButton = findViewById(R.id.button);
        registerButton.setOnClickListener(this::onClickRegister);
    }
    @Override
    protected void onStart() {
        super.onStart();
    }
    public void onClickRegister(View view){
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        String accountType = "Instructor";

        if (fnameET.getText().toString().isEmpty() || lnameET.getText().toString().isEmpty() || stdIDET.getText().toString().isEmpty()){
            Toast toast = Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String email = sharedPreferences.getString(getString(R.string.prefKey_email), "");
        if (email.contains("std")){
            accountType = "Student";
        }
        Log.d(TAG, "onClickRegister: " + email);
        user.put("fname", fnameET.getText().toString());
        user.put("lname", lnameET.getText().toString());
        user.put("stdID", stdIDET.getText().toString());
        user.put("uid", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        user.put("accountType", accountType);

        String Uid = Objects.requireNonNull(user.get("stdID")).toString();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Users").document(Uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast toast = Toast.makeText(RegisterPage.this, "Welcome " + user.get("fname"), Toast.LENGTH_SHORT);
                    toast.show();
                    progressBar.setVisibility(View.GONE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(getString(R.string.prefKey_fName), user.get("fname").toString());
                    editor.putString(getString(R.string.prefKey_stdID), user.get("stdID").toString());
                    editor.putString(getString(R.string.prefKey_accType), user.get("accountType").toString());

                    editor.apply();

                    Intent intent = new Intent(RegisterPage.this, HomePage.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
        progressBar.setVisibility(View.GONE);
    }

}