package com.example.loginmodule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfilePage extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText fname, lname, stdid, etSemester;
    private ImageView profilePic;
    private TextView type;
    private Map<String, Object> profileData = new HashMap<>();
    private ProgressBar progressBar;
    private ConstraintLayout dataLayout;
    private SharedPreferences sharedPreferences;
    private Spinner spinneredu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        fname = findViewById(R.id.etName);
        lname = findViewById(R.id.etSurname);
        stdid = findViewById(R.id.etStdid);
        type = findViewById(R.id.TVtype);
        spinneredu = findViewById(R.id.spinnerEdu);
        progressBar = findViewById(R.id.progressBar);
        profilePic = findViewById(R.id.imgView);
        dataLayout = findViewById(R.id.CL);
        etSemester = findViewById(R.id.etSemester);

        profilePic.setOnClickListener(v -> openCamera());
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchProfileData();
    }

    public void openCamera(){
        Intent intent = new Intent(this, Camera.class);
        startActivity(intent);
    }
    private void assignFields() {
        if (profileData != null) {
            fname.setText(profileData.containsKey("fname") ? Objects.requireNonNull(profileData.get("fname")).toString() : "NON");
            lname.setText(profileData.containsKey("lname") ? Objects.requireNonNull(profileData.get("lname")).toString() : "NON");
            stdid.setText(profileData.containsKey("stdID") ? Objects.requireNonNull(profileData.get("stdID")).toString() : "NON");
            type.setText(profileData.containsKey("accountType") ? Objects.requireNonNull(profileData.get("accountType")).toString() : "NON");
            spinneredu.setSelection(profileData.containsKey("eduLevel") ? ((ArrayAdapter) spinneredu.getAdapter()).getPosition(Objects.requireNonNull(profileData.get("eduLevel")).toString()) : 0);
            etSemester.setText(profileData.containsKey("semester") ? Objects.requireNonNull(profileData.get("semester")).toString() : "1");
        }
        else {
            Log.e("ProfilePage", "assignFields: profileData is empty");
        }

        sharedPreferences = getSharedPreferences(getString(R.string.prefName_login), MODE_PRIVATE);
        String imagePath = sharedPreferences.getString(getString(R.string.prefKey_imagePath), null);
        Log.d("ProfilePage", "assignFields: " + imagePath);
        if (imagePath != null) {
            profilePic.setImageURI(Uri.parse(imagePath));
        }


        progressBar.setVisibility(ProgressBar.INVISIBLE);
        dataLayout.setVisibility(ConstraintLayout.VISIBLE);
    }
    private void fetchProfileData() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        dataLayout.setVisibility(ConstraintLayout.INVISIBLE);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users").document(user.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    profileData = task.getResult().getData();
                    Log.d("ProfilePage", "fetchProfileData: " + profileData);
                    assignFields();
                }
                else {
                    Log.e("ProfilePage", "fetchProfileData: " + task.getException());
                }
            });
        }
    }
}