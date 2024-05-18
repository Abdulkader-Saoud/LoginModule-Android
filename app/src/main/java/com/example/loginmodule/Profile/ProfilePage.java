package com.example.loginmodule.Profile;

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

import com.bumptech.glide.Glide;
import com.example.loginmodule.HomePage;
import com.example.loginmodule.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    private Spinner spinneredu;
    private Button saveBtn, forwardBtn, passButton, logoutButton;
    private CheckBox checkboxSocials,checkboxContact;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private String uid;

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
        stdid = findViewById(R.id.cidET);
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
        saveBtn = findViewById(R.id.laodCSVBTN);
        forwardBtn = findViewById(R.id.forwardButton);
        passButton = findViewById(R.id.passButton);
        logoutButton = findViewById(R.id.logoutButton);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        uid = sharedPreferences.getString(getString(R.string.prefKey_stdID), null);

        profilePic.setOnClickListener(v -> openCamera());
        saveBtn.setOnClickListener(v -> saveToFirebase());
        forwardBtn.setOnClickListener(v -> forward());
        passButton.setOnClickListener(v -> passwrodChange());
        logoutButton.setOnClickListener(v -> onClickLogout());
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchProfileData();
    }
    public void onClickLogout(){
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginPage.class);
        startActivity(intent);
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
    private void passwrodChange(){

        mAuth.sendPasswordResetEmail(mAuth.getCurrentUser().getEmail()).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Toast toast = Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT);
                toast.show();
            }
            else {
                Log.e("ProfilePage", "passwrodChange: " + task.getException());
                Toast toast = Toast.makeText(this, "Password reset email could not be sent", Toast.LENGTH_SHORT);
                toast.show();
            }
            Intent intent = new Intent(this, HomePage.class);
            startActivity(intent);
        });

    }
    private void saveToFirebase(){
        if (fname.getText().toString().isEmpty()){
            Toast toast = Toast.makeText(this, "Please fill the name", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        if (lname.getText().toString().isEmpty()){
            Toast toast = Toast.makeText(this, "Please fill the surname", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        if (stdid.getText().toString().isEmpty()){
            Toast toast = Toast.makeText(this, "Please fill the student ID", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
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
                    SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.prefName_login), MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(getString(R.string.prefKey_stdID), stdid.getText().toString());
                    editor.putString(getString(R.string.prefKey_fName), fname.getText().toString());
                    editor.putString(getString(R.string.prefKey_accType), type.getText().toString());
                    editor.apply();
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

        StorageReference ref = storageReference.child("Profile_Pictures/"  + uid);
        ref.getDownloadUrl().addOnSuccessListener(uri -> {
            Log.d("ProfilePage", "assignFields: " + uri);
            Glide.with(this).load(uri).into(profilePic);
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            dataLayout.setVisibility(ConstraintLayout.VISIBLE);
        }).addOnFailureListener(e -> {
            Log.e("ProfilePage", "assignFields: " + e.getMessage());
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            dataLayout.setVisibility(ConstraintLayout.VISIBLE);
        });
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