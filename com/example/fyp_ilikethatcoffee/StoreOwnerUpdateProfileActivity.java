package com.example.fyp_ilikethatcoffee;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StoreOwnerUpdateProfileActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 201;
    private static final int PICK_IMAGE_REQUEST = 113;
    private static final int PERMISSION_REQUEST_CODE = 114;


    private static final int REQUEST_IMAGE_CAPTURE = 3;
    private FirebaseAuth auth;
    private FirebaseFirestore db; // Declare Firestore instance

    private FirebaseStorage storage;
    private StorageReference mImageRef;

    private Uri imageUri;

    private FirebaseUser user;
    private String currentPhotoPath;
    private ImageView profile_picture;
    private EditText store_name;
    private EditText store_desc;
    private EditText store_address;
    ProgressDialog progressDialog;


    private Button update_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_store_owner_profile);


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding");

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore instance

        store_name = findViewById(R.id.store_name);
        store_desc = findViewById(R.id.store_desc);
        store_address = findViewById(R.id.store_address);
        update_button = findViewById(R.id.update_button);
        mImageRef = FirebaseStorage.getInstance().getReference().child("Store Profile Image");
        getStoreProfileData();

        profile_picture = findViewById(R.id.store_profile_picture);
        user = auth.getCurrentUser();
        if (user == null){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }

        // Check for permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }

        update_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                updateProfile();
                Intent intent = new Intent(getApplicationContext(), StoreOwnerProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });



        profile_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });


    }



    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, (dialog, item) -> {
            if ("Take Photo".equals(options[item])) {
                dispatchTakePictureIntent();
            } else if ("Choose from Gallery".equals(options[item])) {
                openGallery();
            } else {
                dialog.dismiss();
            }
        });
        builder.show();
    }



    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
                Toast.makeText(this, "Error occurred while creating the file", Toast.LENGTH_SHORT).show();
                return;
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.fyp_ilikethatcoffee.fileprovider",  // Make sure this matches the authority in AndroidManifest.xml
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profile_picture.setImageURI(imageUri);
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageUri = Uri.fromFile(new File(currentPhotoPath));
            profile_picture.setImageURI(imageUri);
        }
    }

    private void getStoreProfileData(){

        db.collection("Store").document(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .get().addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null){
                        String storeName = task.getResult().getString("StoreName");
                        String storeDesc = task.getResult().getString("StoreDesc");
                        String storeAddress = task.getResult().getString("StoreAddress");
                        String image = task.getResult().getString("ImageUrl");

                        store_name.setText(storeName);
                        store_desc.setText(storeDesc);
                        store_address.setText(storeAddress);
                        Picasso.get().load(image).into(profile_picture);
                    }else{

                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with accessing external storage
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Permission to access external storage is required.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateProfile() {
        String storeName = store_name.getText().toString();
        String storeDesc = store_desc.getText().toString();
        String storeAddress = store_address.getText().toString();

        // Check if a new image is selected
        if (imageUri != null) {
            // Upload the image to Firebase Storage
            String imageName = "profile_image_" + System.currentTimeMillis() + ".jpg";
            StorageReference imageRef = mImageRef.child(imageName);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the URL of the uploaded image
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();

                            // Update Firestore document with the new data including the image URL
                            DocumentReference docRef = db.collection("Store").document(user.getEmail());
                            Map<String, Object> updatedData = new HashMap<>();
                            updatedData.put("StoreName", storeName);
                            updatedData.put("StoreDesc", storeDesc);
                            updatedData.put("StoreAddress", storeAddress);
                            updatedData.put("ImageUrl", imageUrl); // Add the image URL

                            // Update the document with the new data
                            docRef.update(updatedData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(StoreOwnerUpdateProfileActivity.this, "Store Profile Successfully Updated", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(StoreOwnerUpdateProfileActivity.this, "Store Profile failed to update", Toast.LENGTH_SHORT).show();
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(StoreOwnerUpdateProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // If no new image is selected, update Firestore document with other data only
            DocumentReference docRef = db.collection("Store").document(user.getEmail());
            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("StoreName", storeName);
            updatedData.put("StoreDesc", storeDesc);
            updatedData.put("StoreAddress", storeAddress);

            docRef.update(updatedData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(StoreOwnerUpdateProfileActivity.this, "Store Profile Successfully Updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(StoreOwnerUpdateProfileActivity.this, "Store Profile failed to update", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /*
    private void updateProfile(){
        String storeName = store_name.getText().toString();
        String storeDesc = store_desc.getText().toString();
        String storeAddress = store_address.getText().toString();
        String image = imageUri.toString();


        DocumentReference docRef = db.collection("Store").document(user.getEmail());
        // Create a Map object with the updated data
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("StoreName", storeName);
        updatedData.put("StoreDesc", storeDesc);
        updatedData.put("StoreAddress", storeAddress);

        // Update the document with the new data
        docRef.update(updatedData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(StoreOwnerUpdateProfileActivity.this, "Store Profile Successfully Updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(StoreOwnerUpdateProfileActivity.this, "Store Profile failed to update", Toast.LENGTH_SHORT).show();
                    }
                });
    }

     */




}