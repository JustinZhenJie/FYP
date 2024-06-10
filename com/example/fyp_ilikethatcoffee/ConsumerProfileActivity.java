package com.example.fyp_ilikethatcoffee;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.ViewPager;

import com.example.fyp_ilikethatcoffee.adapter.ViewPagerAdapter;
import com.example.fyp_ilikethatcoffee.profile.MyPagerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.nio.Buffer;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConsumerProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 111;
    TextView editUpdateTv;
    CircleImageView imageIv;
    TextView dobTv;
    EditText usernameTv;
    EditText bioTv;
    Uri uri;
    ImageView settingIv;
    ImageView viewFriend;

    FirebaseAuth mAth;
    FirebaseUser mUser;
    StorageReference mImageRef;

    String documentId;
    String email;
    String username;


    ProgressDialog progressDialog;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumer_profile2);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding");

        mAth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mUser = mAth.getCurrentUser();
        mImageRef = FirebaseStorage.getInstance().getReference().child("Image");

        settingIv = findViewById(R.id.settingIv);

        viewFriend = findViewById(R.id.viewFriend);
        editUpdateTv = findViewById(R.id.editUpdateTv);
        imageIv = findViewById(R.id.imageIv);
        dobTv = findViewById(R.id.dobTv);
        usernameTv = findViewById(R.id.usernameTv);
        bioTv = findViewById(R.id.bioTv);

        ViewPager viewPager = findViewById(R.id.view_pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        editUpdateTv.setOnClickListener(v -> updateData());


        imageIv.setOnClickListener(v -> selectImageFromGallery());

        loadUserAccounts(mUser.getEmail());

        settingIv.setOnClickListener(v -> openSettingActivity());

        viewFriend.setOnClickListener(v -> {
            Intent intent = new Intent(ConsumerProfileActivity.this, ViewFriendActivity.class);
            startActivity(intent);
        });
    }


    private void openSettingActivity() {
        if (documentId != null) {
            Intent intent = new Intent(this, SettingActivity.class);
            intent.putExtra("id", documentId);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Please wait a moment to load person info then try again!", Toast.LENGTH_SHORT).show();
        }
    }


    public void loadUserAccounts(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference userAccountsRef = db.collection("UserAccount");

        // Query to filter documents where the "Email" field matches the given email
        Query query = userAccountsRef.whereEqualTo("Email", email);


        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        documentId = document.getId();
                        String name = document.getString("Name");
                        String userEmail = document.getString("Email"); // Rename to userEmail to avoid conflict
                        String password = document.getString("Password");
                        Long phoneLong = document.getLong("Phone");
                        long phone = (phoneLong != null) ? phoneLong : 0L;
                        String userType = document.getString("UserType");
                        String userName = document.getString("UserName");
                        String image = document.getString("Image");
                        String dob = document.getString("DOB");
                        String bio = document.getString("BIO");

                        // Create UserAccount object or do whatever you need with the retrieved data
                        UserAccount a = new UserAccount(documentId, userEmail, name, password, phone + "", userType, userName, true, image, dob, bio);
                        assignData(a);
                    }
                } else {
                    Toast.makeText(ConsumerProfileActivity.this, "No Data Available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void assignData(UserAccount a) {
        loadReviews(a.getUserName());
        usernameTv.setText(a.getUserName());

        usernameTv.setText(a.getUserName());
        if (a.getDob() == null || a.getDob().isEmpty()) {
            dobTv.setText("DOB not available");
        } else {
            dobTv.setText(a.getDob());
        }

        if (a.getBio() == null || a.getBio().isEmpty()) {
            bioTv.setText("BIO not available");
        } else {
            bioTv.setText(a.getBio());
        }

        if (a.getImage() == null || a.getImage().isEmpty()) {
            imageIv.setImageResource(R.drawable.img);
        } else {
            Picasso.get().load(a.getImage()).into(imageIv);
        }
    }

    private void loadReviews(String userName) {
        Query query = db.collection("Review")
                .whereEqualTo("UserName", userName);

        // Execute the query
        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Handle query result
                        // queryDocumentSnapshots contains the reviews where UserName matches
                        // You can iterate through the documents and extract the review data
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle errors
                    }
                });
    }


    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uri = data.getData();
            imageIv.setImageURI(uri);
        }
    }


    private void updateData() {
        String bio = bioTv.getText().toString();
        if (uri == null && bio == null) {
            Toast.makeText(this, "Please Select image Or Bio", Toast.LENGTH_SHORT).show();
        } else if (uri != null) {
            progressDialog.show();
            mImageRef.child(mUser.getUid()).putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        mImageRef.child(mUser.getUid()).getDownloadUrl().addOnSuccessListener(uri -> updateText(bio, uri.toString()));
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(ConsumerProfileActivity.this, "" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else if (uri == null && bio != null) {
            progressDialog.show();
            updateText(bio, null);
        }
    }

    private void updateText(String bio, String image) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("UserAccount").document(documentId);

        Map<String, Object> updates = new HashMap<>();
        if (image != null) {
            updates.put("Image", image);
        }
        if (bio != null) {
            updates.put("BIO", bio);
        }
        userRef.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                hideKeyboard();
                progressDialog.dismiss();
                loadUserAccounts(mUser.getEmail());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideKeyboard();
                progressDialog.dismiss();
                Toast.makeText(ConsumerProfileActivity.this, "" + e.getLocalizedMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        bioTv.setFocusable(false);
    }
}