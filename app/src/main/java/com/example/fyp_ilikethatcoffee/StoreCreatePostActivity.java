package com.example.fyp_ilikethatcoffee;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StoreCreatePostActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 201;

    private static final int PICK_IMAGE_REQUEST = 111;
    private static final int PERMISSION_REQUEST_CODE = 112;


    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private int postID = 1; // Default value


    private FirebaseAuth auth;
    private FirebaseFirestore db; // Declare Firestore instance

    private FirebaseStorage storage;
    private StorageReference mImageRef;
    private Uri imageUri;
    private FirebaseUser user;

    private String currentPhotoPath;
    private ImageView post_picture, exit_icon;
    private EditText post_caption;
    private Button post_button;
    private TextView selectImageText;
    private FieldValue FieldValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_create_post);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore instance
        mImageRef = FirebaseStorage.getInstance().getReference().child("Store Post Image");
        exit_icon = findViewById(R.id.exit_icon);

        post_picture = findViewById(R.id.post_picture);
        post_caption = findViewById(R.id.post_caption_text);
        post_button = findViewById(R.id.post_button);
        selectImageText = findViewById(R.id.select_image_label);
        int previousItemId = getIntent().getIntExtra("previousItemId", -1);

        user = auth.getCurrentUser();
        if (user == null) {
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

        post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPost();
            }
        });

        exit_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the current activity
                // Send the selected item ID back to the parent activity
                Intent intent = new Intent();

                // Determine the desired activity based on some condition
                int desiredItemId;
                if (previousItemId == R.id.navigation_profile) {
                    desiredItemId = R.id.navigation_profile;
                } else {
                    desiredItemId = R.id.navigation_profile; // Default activity
                }

                intent.putExtra("selectedItemId", desiredItemId);
                setResult(RESULT_OK, intent);
                finish();

            }
        });

        post_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
                if (post_picture != null){
                    selectImageText.setVisibility(View.GONE);
                }
            }
        });

        // Retrieve the Post ID from Firestore
        db.collection("Post")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Check if any documents are retrieved
                            if (!task.getResult().isEmpty()) {
                                int tempPostId = 1; // Start with 1 if no documents are found
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    // Extract the review ID from the document ID
                                    String documentId = document.getId();
                                    int id = Integer.parseInt(documentId);
                                    // Update the reviewId if the current ID is greater
                                    if (id >= tempPostId) {
                                        tempPostId = id + 1;
                                    }
                                }
                                // Now tempPostId contains the next available ID
                                initializePostId(tempPostId);
                            } else {
                                // No documents found, initialize with default value
                                initializePostId(postID);
                            }
                        } else {
                            // Error getting review documents
                            Toast.makeText(StoreCreatePostActivity.this, "Error getting post documents: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void initializePostId(int tempPostId) {
        this.postID = tempPostId;
        //Toast.makeText(StoreCreatePostActivity.this, "Post ID initialized: " + postID, Toast.LENGTH_SHORT).show();
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



    private void createPost() {
        String postCaption = post_caption.getText().toString();

        if (imageUri == null) {
            Toast.makeText(this, "Select image", Toast.LENGTH_SHORT).show();
        } else if (postCaption.isEmpty()) {
            Toast.makeText(this, "Add a caption", Toast.LENGTH_SHORT).show();
        } else {
            //progressDialog.show();
            mImageRef.child(user.getUid()).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        mImageRef.child(user.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                setPostText(uri.toString());
                            }
                        });
                    } else {
                        //progressDialog.dismiss();
                        Toast.makeText(StoreCreatePostActivity.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void setPostText(String image) {
        String postCaption = post_caption.getText().toString();
        String usern = user.getEmail();

        db.collection("Store").document(usern)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String storeName = task.getResult().getString("StoreName");

                        // Inside the callback, so storeNameF is accessible
                        Map<String, Object> Post = new HashMap<>();
                        Post.put("PostInfo", postCaption);
                        Post.put("UserAccountId", usern);
                        Post.put("Image", image);
                        Post.put("StoreName", storeName); // Store the value directly
                        Post.put("DateCreated", FieldValue.serverTimestamp()); // Include timestamp
                        Post.put("PostId", String.valueOf(postID)); // Add postId to the document

                        // Now you can proceed with saving the post to Firestore
                        // For example:
                        db.collection("Post").document(String.valueOf(postID))
                                .set(Post)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(StoreCreatePostActivity.this, "Posted!", Toast.LENGTH_SHORT).show();
                                        // Clear input fields after successful submission
                                        post_caption.setText("");
                                        // Increment post ID for the next review
                                        postID++;
                                        // Finish the activity and go back to the previous page
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(StoreCreatePostActivity.this, "Failed to post, please try again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // Handle the case where the document doesn't exist or there's an error
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
                //Toast.makeText(this, "Permission to access external storage is required.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            post_picture.setImageURI(imageUri);
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageUri = Uri.fromFile(new File(currentPhotoPath));
            post_picture.setImageURI(imageUri);
        }
    }
}


/*
public class StoreCreatePostActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 201;

    private static final int PICK_IMAGE_REQUEST = 111;
    private static final int PERMISSION_REQUEST_CODE = 112;


    private int postID = 1; // Default value


    private FirebaseAuth auth;
    private FirebaseFirestore db; // Declare Firestore instance

    private FirebaseStorage storage;
    private StorageReference mImageRef;
    private Uri imageUri;
    private FirebaseUser user;

    private ImageView post_picture, exit_icon;
    private EditText post_caption;
    private Button post_button;
    private TextView selectImageText;
    private FieldValue FieldValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_create_post);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore instance
        mImageRef = FirebaseStorage.getInstance().getReference().child("Store Post Image");
        exit_icon = findViewById(R.id.exit_icon);

        post_picture = findViewById(R.id.post_picture);
        post_caption = findViewById(R.id.post_caption_text);
        post_button = findViewById(R.id.post_button);
        selectImageText = findViewById(R.id.select_image_label);
        int previousItemId = getIntent().getIntExtra("previousItemId", -1);

        user = auth.getCurrentUser();
        if (user == null) {
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

        post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPost();
            }
        });

        exit_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the current activity
                // Send the selected item ID back to the parent activity
                Intent intent = new Intent();

                // Determine the desired activity based on some condition
                int desiredItemId;
                if (previousItemId == R.id.navigation_profile) {
                    desiredItemId = R.id.navigation_profile;
                } else {
                    desiredItemId = R.id.navigation_home; // Default activity
                }

                intent.putExtra("selectedItemId", desiredItemId);
                setResult(RESULT_OK, intent);
                finish();

            }
        });

        post_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageFromGallery();
            }
        });

        // Retrieve the Post ID from Firestore
        db.collection("Post")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Check if any documents are retrieved
                            if (!task.getResult().isEmpty()) {
                                int tempPostId = 1; // Start with 1 if no documents are found
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    // Extract the review ID from the document ID
                                    String documentId = document.getId();
                                    int id = Integer.parseInt(documentId);
                                    // Update the reviewId if the current ID is greater
                                    if (id >= tempPostId) {
                                        tempPostId = id + 1;
                                    }
                                }
                                // Now tempPostId contains the next available ID
                                initializePostId(tempPostId);
                            } else {
                                // No documents found, initialize with default value
                                initializePostId(postID);
                            }
                        } else {
                            // Error getting review documents
                            Toast.makeText(StoreCreatePostActivity.this, "Error getting post documents: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void initializePostId(int tempPostId) {
        this.postID = tempPostId;
        //Toast.makeText(StoreCreatePostActivity.this, "Post ID initialized: " + postID, Toast.LENGTH_SHORT).show();
    }


    private void createPost() {
        String postCaption = post_caption.getText().toString();

        if (imageUri == null) {
            Toast.makeText(this, "Select image", Toast.LENGTH_SHORT).show();
        } else if (postCaption.isEmpty()) {
            Toast.makeText(this, "Add a caption", Toast.LENGTH_SHORT).show();
        } else {
            //progressDialog.show();
            mImageRef.child(user.getUid()).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        mImageRef.child(user.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                setPostText(uri.toString());
                            }
                        });
                    } else {
                        //progressDialog.dismiss();
                        Toast.makeText(StoreCreatePostActivity.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void setPostText(String image) {
        String postCaption = post_caption.getText().toString();
        String usern = user.getEmail();

        db.collection("Store").document(usern)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String storeName = task.getResult().getString("StoreName");

                        // Inside the callback, so storeNameF is accessible
                        Map<String, Object> Post = new HashMap<>();
                        Post.put("PostInfo", postCaption);
                        Post.put("UserAccountId", usern);
                        Post.put("Image", image);
                        Post.put("StoreName", storeName); // Store the value directly
                        Post.put("DateCreated", FieldValue.serverTimestamp()); // Include timestamp
                        Post.put("PostId", postID); // Add postId to the document

                        // Now you can proceed with saving the post to Firestore
                        // For example:
                        db.collection("Post").document(String.valueOf(postID))
                                .set(Post)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(StoreCreatePostActivity.this, "Posted!", Toast.LENGTH_SHORT).show();
                                        // Clear input fields after successful submission
                                        post_caption.setText("");
                                        // Increment post ID for the next review
                                        postID++;
                                        // Finish the activity and go back to the previous page
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(StoreCreatePostActivity.this, "Failed to post, please try again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // Handle the case where the document doesn't exist or there's an error
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

    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            try {
                if (imageUri != null) {
                    // Use ContentResolver to get the input stream
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    post_picture.setImageBitmap(bitmap);
                    selectImageText.setVisibility(View.GONE);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error getting selected file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}

 */