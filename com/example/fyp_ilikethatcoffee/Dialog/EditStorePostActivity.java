package com.example.fyp_ilikethatcoffee.Dialog;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fyp_ilikethatcoffee.FullStoreProfileActivity;
import com.example.fyp_ilikethatcoffee.Menu.CategoryFragment;
import com.example.fyp_ilikethatcoffee.Posts.PostFragment;
import com.example.fyp_ilikethatcoffee.R;
import com.example.fyp_ilikethatcoffee.StoreOwnerProfileActivity;
import com.example.fyp_ilikethatcoffee.StoreOwnerUpdateProfileActivity;
import com.example.fyp_ilikethatcoffee.StoreReviewFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditStorePostActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 113;
    private static final int PERMISSION_REQUEST_CODE = 114;
    private Uri imageUri;
    private static final int REQUEST_IMAGE_CAPTURE = 3;
    private String storeEmail,postCaption, postImage;
    private String postId;
    private EditText postCaptionEdit;
    private TextView storeName;
    private ImageView postPicture, storeProfilePic;
    private Button updateBtn;
    private String currentPhotoPath;
    private FirebaseStorage storage;
    private StorageReference mImageRef;
    private FirebaseFirestore db; // Declare Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_store_post);
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
        db = FirebaseFirestore.getInstance(); // Initialize Firestore instance

        postCaptionEdit = findViewById(R.id.post_caption_text);
        storeName = findViewById(R.id.store_display_name);
        postPicture = findViewById(R.id.image_post);
        storeProfilePic = findViewById(R.id.store_profile_picture_p);
        updateBtn = findViewById(R.id.update_post);
        mImageRef = FirebaseStorage.getInstance().getReference().child("Store Post Image");

        if (getIntent() != null) {
            storeEmail = getIntent().getStringExtra("storeEmail");
            postId = getIntent().getStringExtra("postId"); // Use a default value of -1
            postCaption = getIntent().getStringExtra("postCaption");
            postImage = getIntent().getStringExtra("postImage");
        }
        // Check if postId is valid
        if (postId == null) {
            // Handle the case where postId is invalid
            Log.e("EditStorePostActivity", "Invalid postId");
            finish(); // Optionally, you can close the activity or handle it appropriately
        }

        Log.d("EditStorePostActivity", "Current postId Retrieved: " + postId);
        Log.d("EditStorePostActivity", "Current storeEmail Retrieved: " + storeEmail);
        // Initialize your views and set the data accordingly
        fetchStoreInformation();
        fetchPostInformation();

        postPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePost();
                finish();
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
                // Create a content URI for the photo file
                imageUri = FileProvider.getUriForFile(this,
                        "com.example.fyp_ilikethatcoffee.fileprovider",  // Make sure this matches the authority in AndroidManifest.xml
                        photoFile);
                // Save the URI to the captured image
                currentPhotoPath = photoFile.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
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
            postPicture.setImageURI(imageUri);
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageUri = Uri.fromFile(new File(currentPhotoPath));
            postPicture.setImageURI(imageUri);
        }
    }

    public void fetchStoreInformation(){
        db.collection("Store").document(storeEmail)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String storeN = task.getResult().getString("StoreName");
                        String imageUrl = task.getResult().getString("ImageUrl");

                        storeName.setText(storeN);
                        Picasso.get().load(imageUrl).into(storeProfilePic);
                        Log.d("StoreOwnerProfileActivity", "Current StoreName Retrieved: " + storeName);


                    }


                });
    }

    public void fetchPostInformation(){
        db.collection("Post").document(postId)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String postC = task.getResult().getString("PostInfo");
                        String postUrlImage = task.getResult().getString("Image");

                        Log.d("StoreOwnerProfileActivity", "Current StoreName Retrieved: " + storeName);
                        postCaptionEdit.setText(postC);
                        Picasso.get().load(postUrlImage).into(postPicture);


                    }


                });
    }

    public void updatePost(){
        String postCap = postCaptionEdit.getText().toString();

        // Check if a new image is selected
        if (imageUri != null) {
            // Upload the image to Firebase Storage
            String imageName = "post_image_" + System.currentTimeMillis() + ".jpg";
            StorageReference imageRef = mImageRef.child(imageName);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Get the URL of the uploaded image
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();

                            // Update Firestore document with the new data including the image URL
                            DocumentReference docRef = db.collection("Post").document(String.valueOf(postId));
                            Map<String, Object> updatedData = new HashMap<>();
                            updatedData.put("PostInfo", postCap);
                            updatedData.put("ImageUrl", imageUrl); // Add the image URL

                            // Update the document with the new data
                            docRef.update(updatedData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(EditStorePostActivity.this, "Post Successfully Updated", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(EditStorePostActivity.this, "Store Profile failed to update", Toast.LENGTH_SHORT).show();
                                    });
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditStorePostActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // If no new image is selected, update Firestore document with other data only
            DocumentReference docRef = db.collection("Post").document(postId);
            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("PostInfo", postCap);


            docRef.update(updatedData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EditStorePostActivity.this, "Post Successfully Updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditStorePostActivity.this, "Store Profile failed to update", Toast.LENGTH_SHORT).show();
                    });
        }
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
}