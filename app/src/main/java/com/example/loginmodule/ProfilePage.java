package com.example.loginmodule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
    private EditText fname, lname, stdid, etSemester,etEmail,etPhone,etInstagram,etTwitter;
    private ImageView profilePic;
    private TextView type;
    private Map<String, Object> profileData = new HashMap<>();
    private ProgressBar progressBar;
    private ConstraintLayout dataLayout;
    private SharedPreferences sharedPreferences;
    private Spinner spinneredu;
    private Button saveBtn, forwardBtn;
    private CheckBox checkboxSocials,checkboxContact;

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
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etInstagram = findViewById(R.id.etInstagram);
        etTwitter = findViewById(R.id.etTwitter);
        checkboxSocials = findViewById(R.id.checkboxSocials);
        checkboxContact = findViewById(R.id.checkboxContact);
        saveBtn = findViewById(R.id.saveButton);
        forwardBtn = findViewById(R.id.forwardButton);

        profilePic.setOnClickListener(v -> openCamera());
        saveBtn.setOnClickListener(v -> saveToFirebase());
        forwardBtn.setOnClickListener(v -> forward());
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchProfileData();
    }
    private void forward(){
        String message = "Hello, Im " + fname.getText().toString()  + " " + lname.getText().toString() + "." ;
        if (spinneredu.getSelectedItemPosition() != 0){
            message += " I am a " + spinneredu.getSelectedItem().toString() + " student.";
            if (!etSemester.getText().toString().isEmpty()){
                message += " Currently in semester " + etSemester.getText().toString() + ".";
            }
        }
        if (!checkboxContact.isChecked()){
            if (!etEmail.getText().toString().isEmpty()){
                message += " You can contact me via email at " + etEmail.getText().toString() + ".";
            }
            if (!etPhone.getText().toString().isEmpty()){
                message += " You can contact me via phone at " + etPhone.getText().toString() + ".";
            }
        }
        if (!checkboxSocials.isChecked()){
            if (!etInstagram.getText().toString().isEmpty()){
                message += " You can find me on Instagram at " + etInstagram.getText().toString() + ".";
            }
            if (!etTwitter.getText().toString().isEmpty()){
                message += " You can find me on Twitter at " + etTwitter.getText().toString() + ".";
            }
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.setType("text/plain");

        Intent chooser = Intent.createChooser(intent, "Send Message via:");
        startActivity(chooser);
    }
    private void saveToFirebase(){
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> data = new HashMap<>();
            data.put("fname", fname.getText().toString());
            data.put("lname", lname.getText().toString());
            data.put("stdID", stdid.getText().toString());
            data.put("accountType", type.getText().toString());
            data.put("eduLevel", spinneredu.getSelectedItem().toString());
            data.put("semester", etSemester.getText().toString());
            data.put("email", etEmail.getText().toString());
            data.put("phone", etPhone.getText().toString());
            data.put("instagram", etInstagram.getText().toString());
            data.put("twitter", etTwitter.getText().toString());
            data.put("socials", checkboxSocials.isChecked());
            data.put("contact", checkboxContact.isChecked());
            db.collection("Users").document(user.getUid()).set(data).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("ProfilePage", "saveToFirebase: Data saved successfully");
                    Toast toast = Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    Log.e("ProfilePage", "saveToFirebase: " + task.getException());
                    Toast toast = Toast.makeText(this, "Data could not be saved", Toast.LENGTH_SHORT);
                    toast.show();

                }
            });
            Intent intent = new Intent(this, HomePage.class);
            startActivity(intent);
        }
        else {
            Log.e("ProfilePage", "saveToFirebase: User is null");
            Toast toast = Toast.makeText(this, "User is null", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    public void openCamera(){
        Intent intent = new Intent(this, Camera.class);
        startActivity(intent);
    }
    private void assignFields() {
        if (profileData != null) {
            fname.setText(profileData.containsKey("fname") ? Objects.requireNonNull(profileData.get("fname")).toString() : "");
            lname.setText(profileData.containsKey("lname") ? Objects.requireNonNull(profileData.get("lname")).toString() : "");
            stdid.setText(profileData.containsKey("stdID") ? Objects.requireNonNull(profileData.get("stdID")).toString() : "");
            type.setText(profileData.containsKey("accountType") ? Objects.requireNonNull(profileData.get("accountType")).toString() : "");
            spinneredu.setSelection(profileData.containsKey("eduLevel") ? ((ArrayAdapter) spinneredu.getAdapter()).getPosition(Objects.requireNonNull(profileData.get("eduLevel")).toString()) : 0);
            etSemester.setText(profileData.containsKey("semester") ? Objects.requireNonNull(profileData.get("semester")).toString() : "1");
            etEmail.setText(profileData.containsKey("email") ? Objects.requireNonNull(profileData.get("email")).toString() : "");
            etPhone.setText(profileData.containsKey("phone") ? Objects.requireNonNull(profileData.get("phone")).toString() : "");
            etInstagram.setText(profileData.containsKey("instagram") ? Objects.requireNonNull(profileData.get("instagram")).toString() : "");
            etTwitter.setText(profileData.containsKey("twitter") ? Objects.requireNonNull(profileData.get("twitter")).toString() : "");
            checkboxSocials.setChecked(profileData.containsKey("socials") ? (boolean) profileData.get("socials") : false);
            checkboxContact.setChecked(profileData.containsKey("contact") ? (boolean) profileData.get("contact") : false);
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