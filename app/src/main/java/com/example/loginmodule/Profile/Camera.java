package com.example.loginmodule.Profile;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.loginmodule.Profile.ProfilePage;
import com.example.loginmodule.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class Camera extends AppCompatActivity {

    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    private ImageView selectedImage;
    private Button cameraBtn;
    private String currentPhotoPath;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        selectedImage = findViewById(R.id.imageView);
        cameraBtn = findViewById(R.id.button
        );
        Button galleryBtn = findViewById(R.id.buttonGallery);
        Button saveBtn = findViewById(R.id.buttonSave);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        uid = sharedPreferences.getString(getString(R.string.prefKey_stdID), null);

        cameraBtn.setOnClickListener(v -> verifyPermissions(0));
        galleryBtn.setOnClickListener(v -> verifyPermissions(1));
        saveBtn.setOnClickListener(v -> uploadImageToFirebase());
    }

    private void uploadImageToFirebase(){
        StorageReference ref = storageReference.child("Profile_Pictures/"
                                + uid);
        ref.putFile(Uri.parse(currentPhotoPath))
                .addOnSuccessListener(taskSnapshot -> {
                            Toast toast = Toast.makeText(getApplicationContext(), "Image Uploaded", Toast.LENGTH_SHORT);
                            toast.show();
                            Intent intent = new Intent(this, ProfilePage.class);
                            startActivity(intent);
                        })

                .addOnFailureListener(e -> {
                    Toast toast = Toast.makeText(getApplicationContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT);
                    toast.show();
                });
    }

    private void verifyPermissions(Integer i){
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[2]) == PackageManager.PERMISSION_GRANTED){
            if (i == 0)
                dispatchTakePictureIntent();
            else {
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    CAMERA_PERM_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions, grantResults);
        if(requestCode == CAMERA_PERM_CODE){

            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                dispatchTakePictureIntent();
            }else {
                Toast.makeText(this, "Camera Permission is Required to Use camera." + grantResults[0], Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                File f = new File(currentPhotoPath);
                currentPhotoPath = Uri.fromFile(f).toString();
                selectedImage.setImageURI(Uri.parse(currentPhotoPath));
            }

        }
    }

    private File createImageFile() throws IOException {
        String imageFileName = "ProgilePic";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Log.d("tag", "Storage dir is: " + storageDir);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("tag", "Error Creating File: " + ex.getMessage());
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()),
                        "com.example.loginmodule.provider", photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {

                if (uri != null) {
                    Log.d("PhotoPicker", "Selected URI: " + uri);
                    currentPhotoPath = uri.toString();
                    selectedImage.setImageURI(uri);
                } else {
                    Log.d("PhotoPicker", "No media selected");
                }
            });

}