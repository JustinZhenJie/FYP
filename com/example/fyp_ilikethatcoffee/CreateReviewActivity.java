package com.example.fyp_ilikethatcoffee;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;

import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class CreateReviewActivity extends AppCompatActivity {
    private EditText reviewDesc;
    private RatingBar ratingBar;
    private Button reviewSubmitBtn;
    private TextView StoreNameTextView, StoreLocationTextView;
    private FirebaseFirestore db;
    private int reviewId = 1;
    private String username, storeName, storeDesc, storeAddress, UserAccountId, storeEmail;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private Uri imageUri;
    private ImageView imageView;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_review);
        initializeViews();
        retrieveIntentData();
        initializeFirestore();
        fetchCurrentUserEmail();
        setListeners();
    }

    private void initializeViews() {
        reviewDesc = findViewById(R.id.ReviewDesc);
        ratingBar = findViewById(R.id.ratingBar);
        reviewSubmitBtn = findViewById(R.id.reviewSubmitBtn);
        StoreNameTextView = findViewById(R.id.StoreNameTextView);
        StoreLocationTextView = findViewById(R.id.StoreLocationTextView);
        imageView = findViewById(R.id.imageUpload);
    }

    private void retrieveIntentData() {
        Intent intent = getIntent();
        storeName = intent.getStringExtra("StoreName");
        storeDesc = intent.getStringExtra("StoreDesc");
        storeAddress = intent.getStringExtra("StoreAddress");
        username = intent.getStringExtra("USERNAME");
        Log.d("CreateReviewActivity", "Current user username: " + username);
        storeEmail = intent.getStringExtra("StoreEmail");
        StoreNameTextView.setText(storeName);
    }

    private void initializeFirestore() {
        db = FirebaseFirestore.getInstance();
        fetchNextReviewId();
        retrieveStoreInfo();
    }

    private void fetchCurrentUserEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            UserAccountId = user.getEmail();
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
        }
    }


    private void setListeners() {
        findViewById(R.id.imageButton).setOnClickListener(v -> selectImage());
        reviewSubmitBtn.setOnClickListener(v -> submitReview());
    }
    private void fetchNextReviewId() {
        db.collection("Review")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int maxId = 0;  // Start with 0
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            int currentId = Integer.parseInt(document.getId());
                            if (currentId > maxId) {
                                maxId = currentId;
                            }
                        }
                        reviewId = maxId + 1;  // Set to max ID found + 1
                        Toast.makeText(this, "Next review ID: " + reviewId, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to fetch review IDs.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void retrieveStoreInfo() {
        db.collection("Store")
                .whereEqualTo("StoreName", storeName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            storeAddress = document.getString("StoreAddress");
                            storeEmail = document.getString("UserAccountId");  // Make sure this field exists in Firestore
                            StoreLocationTextView.setText(storeAddress);

                            // Log to check the correct retrieval of email
                            Log.d("CreateReview", "Store email retrieved: " + storeEmail);
                        }
                        // You may want to handle the scenario where no documents are found
                        if (task.getResult().isEmpty()) {
                            Log.d("CreateReview", "No stores found with the name: " + storeName);
                        }
                    } else {
                        Toast.makeText(this, "Error getting store information: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // Method to get current timestamp
    private String getCurrentTimestamp() {
        // Implement your logic to get the current timestamp
        // For example, you can use SimpleDateFormat to format the current date and time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void submitReview() {
        String reviewDescription = reviewDesc.getText().toString().trim();
        float rating = ratingBar.getRating();
        String currentDate = getCurrentTimestamp();

        if (reviewDescription.isEmpty() || rating == 0) {
            reviewDesc.setError("Review content cannot be empty");
            Toast.makeText(this, "Please ensure the review is not empty and rating is not zero.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("ReviewDesc", reviewDescription);
        reviewData.put("Rating", rating);
        reviewData.put("StoreName", storeName);
        reviewData.put("UserName", username);
        reviewData.put("ReviewId", reviewId);
        reviewData.put("DateCreated", FieldValue.serverTimestamp());
        reviewData.put("UserAccountId", UserAccountId);
        reviewData.put("StoreId", storeEmail);


        // Check if an image is selected and needs to be uploaded
        if (imageUri != null) {
            uploadImageAndSaveReview(reviewData);
        } else {
            saveReviewToFirestore(reviewData, "");  // Save without an image
        }
    }

    private void uploadImageAndSaveReview(Map<String, Object> reviewData) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("review_images/" + UUID.randomUUID().toString() + ".jpg");

        imageRef.putFile(imageUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                imageRef.getDownloadUrl().addOnCompleteListener(urlTask -> {
                    if (urlTask.isSuccessful()) {
                        String downloadUrl = urlTask.getResult().toString();
                        saveReviewToFirestore(reviewData, downloadUrl);
                    } else {
                        Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveReviewToFirestore(Map<String, Object> reviewData, String imageUrl) {
        reviewData.put("Image", imageUrl);
        db.collection("Review").document(String.valueOf(reviewId))
                .set(reviewData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Review submitted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to submit review", Toast.LENGTH_SHORT).show();
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
            imageView.setImageURI(imageUri);
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageUri = Uri.fromFile(new File(currentPhotoPath));
            imageView.setImageURI(imageUri);
        }
    }
}
