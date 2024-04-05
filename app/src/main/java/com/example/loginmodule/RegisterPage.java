package com.example.loginmodule;

import static android.content.ContentValues.TAG;

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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterPage extends AppCompatActivity {
    private EditText fnameET, lnameET, stdIDET;
    private Map<String, Object> user = new HashMap<>();
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
        Button registerButton = findViewById(R.id.button);
        registerButton.setOnClickListener(this::onClickRegister);
    }
    @Override
    protected void onStart() {
        super.onStart();
    }
    public void onClickRegister(View view){
        if (fnameET.getText().toString().isEmpty() || lnameET.getText().toString().isEmpty() || stdIDET.getText().toString().isEmpty()){
            Toast toast = Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        user.put("fname", fnameET.getText().toString());
        user.put("lname", lnameET.getText().toString());
        user.put("stdID", stdIDET.getText().toString());
        String Uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Users").document(Uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast toast = Toast.makeText(RegisterPage.this, "Welcome " + user.get("fname"), Toast.LENGTH_SHORT);
                    toast.show();
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));

    }
}