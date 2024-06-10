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
import com.example.fyp_ilikethatcoffee.profile.MyPagerAdapter;
import com.example.fyp_ilikethatcoffee.Posts.PostFragment;
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

public class StoreOwnerProfileActivity extends AppCompatActivity {


    private static final int RC_SIGN_IN = 201;

    private static final int PICK_IMAGE_REQUEST = 111;

    //Our nave Controller
    private FirebaseAuth auth;
    private FirebaseFirestore db; // Declare Firestore instance

    private FirebaseStorage storage;
    private StorageReference mImageRef;

    private BottomNavigationView bottomNavigationView;
    private FrameLayout frameLayout;

    private Uri imageUri;

    private FirebaseUser user;

    private String documentId, storeName, username;

    private ImageView profile_picture, edit_profile,  settingIv;;

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
        setContentView(R.layout.activity_test_store_owner_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore instance
        mImageRef = FirebaseStorage.getInstance().getReference().child("Store Profile Image");
        username = getIntent().getStringExtra("USERNAME");

        settingIv = findViewById(R.id.settingIvP);
        store_name = findViewById(R.id.store_name_p);
        store_desc = findViewById(R.id.store_desc_p);
        store_address = findViewById(R.id.store_address_p);
        edit_profile = findViewById(R.id.edit_profile_icon);
        moreText = findViewById(R.id.more_text);
        //logoutBtn = findViewById(R.id.logout_button_store);
        //update_profile_button = findViewById(R.id.update_my_profile_button)
        //addPost = findViewById(R.id.add_post_icon);
        ViewPager viewPager = findViewById(R.id.view_pager_store);
        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());

        // bottom navigation bar
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
        //frameLayout = findViewById(R.id.flFragment);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.navigation_home) {
                    startActivity(new Intent(StoreOwnerProfileActivity.this, StoreOwnerHomeActivity.class));
                    return true;
                }

                if (itemId == R.id.navigation_post) {
                    Intent intent = new Intent(StoreOwnerProfileActivity.this, StoreCreatePostActivity.class);
                    intent.putExtra("previousItemId", R.id.navigation_profile); // Assuming you want to navigate back to profile
                    startActivity(intent);
                    return true;
                }

                return true;
            }
        });

        profile_picture = findViewById(R.id.store_profile_picture_p);
        user = auth.getCurrentUser();
        if (user == null){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }



        TabLayout tabLayout = findViewById(R.id.tabs_store);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });



        db.collection("Store").document(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        documentId = task.getResult().getId();
                        storeName = task.getResult().getString("StoreName");
                        String storeDesc = task.getResult().getString("StoreDesc");
                        String storeAddress = task.getResult().getString("StoreAddress");
                        String image = task.getResult().getString("ImageUrl");

                        store_name.setText(storeName);
                        store_desc.setText(storeDesc);
                        store_address.setText(storeAddress);
                        Picasso.get().load(image).into(profile_picture);


                        // Now that storeName is retrieved, you can set up the fragments
                        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                        Log.d("StoreOwnerProfileActivity", "Current Username Retrieved: " + username);
                        Log.d("StoreOwnerProfileActivity", "Current StoreName Retrieved: " + storeName);

                        store_desc.post(() -> {
                            if (isTextViewEllipsized(store_desc)) {
                                moreText.setVisibility(View.VISIBLE);
                            }
                        });

                        moreText.setOnClickListener(v -> {
                            Intent intent = new Intent(StoreOwnerProfileActivity.this, FullStoreProfileActivity.class);
                            intent.putExtra("StoreName", storeName);
                            intent.putExtra("StoreDesc", storeDesc);
                            intent.putExtra("StoreAddress", storeAddress);
                            intent.putExtra("StoreEmail", email);
                            intent.putExtra("ImageUrl", image);
                            startActivity(intent);
                        });

                        // Add three tabs
                        adapter.addFrag(CategoryFragment.newInstance(email), "Menu");
                        adapter.addFrag(StoreReviewFragment.newInstance(storeName, username), "Reviews");
                        adapter.addFrag(PostFragment.newInstance(email, email), "Posts");
                        viewPager.setAdapter(adapter);

                        // Set the setting button click listener
                        settingIv.setOnClickListener(v -> openSettingActivity());
                    } else {
                        Log.e("StoreOwnerProfileActivity", "Error fetching store data", task.getException());
                    }
                });


        edit_profile.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), StoreOwnerUpdateProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private boolean isTextViewEllipsized(TextView textView) {
        Layout layout = textView.getLayout();
        if (layout != null) {
            int lines = layout.getLineCount();
            if (lines > 0) {
                return layout.getEllipsisCount(lines - 1) > 0;
            }
        }
        return false;
    }


    private void choosePicture() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);

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
                        Toast.makeText(StoreOwnerProfileActivity.this, "Store Profile Successfully Updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(StoreOwnerProfileActivity.this, "Store Profile failed to update", Toast.LENGTH_SHORT).show();
                    }
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



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode==RESULT_OK && data !=null && data.getData() !=null){
            imageUri = data.getData();
            profile_picture.setImageURI(imageUri);
        }
    }




}
/*
public class StoreOwnerProfileActivity extends AppCompatActivity {


    private static final int RC_SIGN_IN = 201;

    private static final int PICK_IMAGE_REQUEST = 111;

    //Our nave Controller
    private FirebaseAuth auth;
    private FirebaseFirestore db; // Declare Firestore instance

    private FirebaseStorage storage;
    private StorageReference mImageRef;


    private Uri imageUri;

    private FirebaseUser user;

    private ImageView profile_picture;

    private ImageView addPost;
    private TextView store_name;
    private TextView store_desc;
    private TextView store_address;

    private Button update_profile_button, logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_store_owner_profile);


        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore instance
        mImageRef = FirebaseStorage.getInstance().getReference().child("Store Profile Image");

        store_name = findViewById(R.id.store_name_p);
        store_desc = findViewById(R.id.store_desc_p);
        store_address = findViewById(R.id.store_address_p);
        update_profile_button = findViewById(R.id.update_my_profile_button);
        logoutBtn = findViewById(R.id.logout_button_store);
        addPost = findViewById(R.id.add_post_icon);
        ViewPager viewPager = findViewById(R.id.view_pager_store);
        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());

        //Add three tabs
        adapter.addFrag(new MenuFragment(), "Menu");
        adapter.addFrag(new ReviewFragment(), "Reviews");
        adapter.addFrag(new ReviewFragment(), "Posts");
        viewPager.setAdapter(adapter);


        TabLayout tabLayout = findViewById(R.id.tabs_store);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        profile_picture = findViewById(R.id.store_profile_picture_p);
        user = auth.getCurrentUser();
        if (user == null){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }

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
                    }
                    else{

                    }
                });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                Intent intent = new Intent(StoreOwnerProfileActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });


        update_profile_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), StoreOwnerUpdateProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        addPost.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), StoreCreatePostActivity.class);
                startActivity(intent);
                finish();
            }
        });



        /*profile_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
            }
        });



    }



    private void choosePicture() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);

    }

    private void getStoreProfileData(){

        db.collection("Store").document(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .get().addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null){
                        String storeName = task.getResult().getString("StoreName");
                        String storeDesc = task.getResult().getString("StoreDesc");
                        String storeAddress = task.getResult().getString("StoreAddress");

                        store_name.setText(storeName);
                        store_desc.setText(storeDesc);
                        store_address.setText(storeAddress);
                    }else{

                    }
                });
    }

    private void updateProfile(){
        String storeName = store_name.getText().toString();
        String storeDesc = store_desc.getText().toString();
        String storeAddress = store_address.getText().toString();

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
                        Toast.makeText(StoreOwnerProfileActivity.this, "Store Profile Successfully Updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(StoreOwnerProfileActivity.this, "Store Profile failed to update", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode==RESULT_OK && data !=null && data.getData() !=null){
            imageUri = data.getData();
            profile_picture.setImageURI(imageUri);
        }
    }
}

*/