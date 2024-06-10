package com.example.fyp_ilikethatcoffee;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.fyp_ilikethatcoffee.Menu.CategoryFragment;
import com.example.fyp_ilikethatcoffee.Posts.PostFragment;
import com.example.fyp_ilikethatcoffee.profile.MyPagerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class FullStoreProfileActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 201;

    private static final int PICK_IMAGE_REQUEST = 111;

    private FirebaseAuth auth;
    private FirebaseFirestore db; // Declare Firestore instance

    private Uri imageUri;

    private FirebaseUser user;

    private String storeName, username, storeEmail, storeDesc, storeProfileImage, storeAddress;

    private ImageView profile_picture;

    //private ImageView addPost;
    private TextView store_name;
    private TextView store_desc;
    private TextView store_address;
    private TextView moreText;
    private Button update_profile_button, logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_full_store_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        username = getIntent().getStringExtra("USERNAME");
        storeEmail = getIntent().getStringExtra("StoreEmail");
        storeName = getIntent().getStringExtra("StoreName");
        storeAddress = getIntent().getStringExtra("StoreAddress");
        storeDesc = getIntent().getStringExtra("StoreDesc");
        storeProfileImage = getIntent().getStringExtra("ImageUrl");

        store_name = findViewById(R.id.store_name_p);
        store_desc = findViewById(R.id.store_desc_p);
        store_address = findViewById(R.id.store_address_p);
        moreText = findViewById(R.id.more_text);

        profile_picture = findViewById(R.id.store_profile_picture_p);
        user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }

        db.collection("Store").document(storeEmail)
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
        protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
                imageUri = data.getData();
                profile_picture.setImageURI(imageUri);
            }
        }



}

