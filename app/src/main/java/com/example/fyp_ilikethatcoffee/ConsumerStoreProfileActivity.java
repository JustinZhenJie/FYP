package com.example.fyp_ilikethatcoffee;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.fyp_ilikethatcoffee.Menu.CategoryFragment;
import com.example.fyp_ilikethatcoffee.Posts.PostFragment;
import com.example.fyp_ilikethatcoffee.profile.MyPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ConsumerStoreProfileActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 201;
    private FirebaseAuth auth;
    private FirebaseFirestore db; // Declare Firestore instance

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private Uri imageUri;

    private FirebaseUser user;

    private ImageView profile_picture;
    private TextView store_name;
    private TextView store_desc;
    private TextView store_address;
    private Button createReviewBtn, logoutBtn;
    private String username;
    private String storeName;
    private String storeDesc;
    private String storeAddress;
    private String storeEmail;
    private String storeProfileImageUrl;
    private ImageButton btnFavorite;
    private TextView moreText;
    private static final int CREATE_REVIEW_REQUEST = 1; // Request code for starting CreateReviewActivity

    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test_consumer_view_store);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore instance

        // Fetch current user email
        fetchCurrentUserEmail();
        // Retrieve data from intent extras
        storeName = getIntent().getStringExtra("StoreName");
        storeDesc = getIntent().getStringExtra("StoreDesc");
        storeAddress = getIntent().getStringExtra("StoreAddress");
        storeEmail = getIntent().getStringExtra("StoreEmail");
        storeProfileImageUrl = getIntent().getStringExtra("ImageUrl");
        username = getIntent().getStringExtra("USERNAME");
        Log.d("ConsumerStoreProfileActivity", "Current Username Retrieved: " + username);
        //Log.d("ConsumerStoreProfileActivity", "Current ImageUrl Retrieved: " + storeProfileImageUrl);


        store_name = findViewById(R.id.store_name_p);
        store_desc = findViewById(R.id.store_desc_p);
        store_address = findViewById(R.id.store_address_p);
        createReviewBtn = findViewById(R.id.createReviewBtn);
        //logoutBtn = findViewById(R.id.logout_button);
        btnFavorite = findViewById(R.id.btnFavorite);
        moreText = findViewById(R.id.more_text);

        profile_picture = findViewById(R.id.store_profile_picture_p);
        ViewPager viewPager = findViewById(R.id.view_pager_store);
        store_name.setText(storeName);
        store_desc.setText(storeDesc);
        store_address.setText(storeAddress);
        //Picasso.get().load(storeProfileImageUrl).into(profile_picture);

        db.collection("Store").document(storeEmail)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String image = task.getResult().getString("ImageUrl");

                        Picasso.get().load(image).into(profile_picture);
                        Log.d("ConsumerStoreProfileActivity", "Current Username Retrieved: " + image);

                    } else {
                        Log.e("StoreOwnerProfileActivity", "Error fetching store profile picture", task.getException());
                    }
                });



        //On click more button
        store_desc.post(() -> {
            if (isTextViewEllipsized(store_desc)) {
                moreText.setVisibility(View.VISIBLE);
            }
        });

        moreText.setOnClickListener(v -> {
            Intent intent = new Intent(ConsumerStoreProfileActivity.this, FullStoreProfileActivity.class);
            intent.putExtra("StoreName", storeName);
            intent.putExtra("StoreDesc", storeDesc);
            intent.putExtra("StoreAddress", storeAddress);
            intent.putExtra("StoreEmail", storeEmail);
            intent.putExtra("ImageUrl", storeProfileImageUrl);
            startActivity(intent);
        });
        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());

        // Add two tabs
        adapter.addFrag(CategoryFragment.newInstance(storeEmail), "Menu");
        adapter.addFrag(StoreReviewFragment.newInstance(storeName, username), "Reviews");
        adapter.addFrag(PostFragment.newInstance(storeEmail, userEmail), "Posts");
        Log.d("ConsumerStoreProfileActivity", "Current useremail Retrieved: " + userEmail);


        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs_store);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        Log.d("TAG", "onCreate: Started");

        /*
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                Intent intent = new Intent(ConsumerStoreProfileActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

         */



        if (userEmail != null) {
            // Path to the favorites collection
            CollectionReference favRef = db.collection("UserAccount").document(userEmail).collection("Favourite");

            // Load initial favorite status
            favRef.document(storeName).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    btnFavorite.setImageResource(R.drawable.bookmark_filled); // Set this to your filled icon
                } else {
                    btnFavorite.setImageResource(R.drawable.bookmark_empty); // Set this to your unfilled icon
                }
            });

            // Toggle favorite status on button click
            btnFavorite.setOnClickListener(v -> {
                favRef.document(storeName).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        // If already favorited, remove it
                        favRef.document(storeName).delete()
                                .addOnSuccessListener(aVoid -> {
                                    btnFavorite.setImageResource(R.drawable.bookmark_empty);
                                    Toast.makeText(ConsumerStoreProfileActivity.this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // If not favorited, add it
                        Map<String, Object> storeData = new HashMap<>();
                        storeData.put("StoreName", storeName);

                        favRef.document(storeName).set(storeData)
                                .addOnSuccessListener(aVoid -> {
                                    btnFavorite.setImageResource(R.drawable.bookmark_filled);
                                    Toast.makeText(ConsumerStoreProfileActivity.this, "Added to Favorites", Toast.LENGTH_SHORT).show();
                                });
                    }
                });
            });
        }

        createReviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();

                // Retrieve the username, store name, description, and address from intent extras
                String storeName = getIntent().getStringExtra("StoreName");
                String storeDesc = getIntent().getStringExtra("StoreDesc");
                String storeAddress = getIntent().getStringExtra("StoreAddress");
                String username = intent.getStringExtra("USERNAME");
                String storeEmail = intent.getStringExtra("StoreEmail");

                // Start CreateReviewActivity and pass the username, store name, description, and address as extras
                Intent createReviewIntent = new Intent(ConsumerStoreProfileActivity.this, CreateReviewActivity.class);
                createReviewIntent.putExtra("USERNAME", username);
                createReviewIntent.putExtra("StoreName", storeName);
                createReviewIntent.putExtra("StoreDesc", storeDesc);
                createReviewIntent.putExtra("StoreAddress", storeAddress);
                createReviewIntent.putExtra("StoreEmail", storeEmail);
                startActivityForResult(createReviewIntent, CREATE_REVIEW_REQUEST);
            }
        });

        user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }


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

    // Method to fetch the current user's email
    private void fetchCurrentUserEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userEmail = user.getEmail();
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            userEmail = null;
        }
    }

    private void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_REVIEW_REQUEST && resultCode == RESULT_OK && data != null) {
            username = data.getStringExtra("USERNAME");
            storeName = data.getStringExtra("StoreName");
            storeDesc = data.getStringExtra("StoreDesc");
            storeAddress = data.getStringExtra("StoreAddress");
            storeProfileImageUrl = data.getStringExtra("ImageUrl");
            profile_picture.setImageURI(imageUri);
            updateUI();
        }
    }

    private void updateUI() {
        store_name.setText(storeName);
        store_desc.setText(storeDesc);
        store_address.setText(storeAddress);
        Picasso.get().load(storeProfileImageUrl).into(profile_picture);
    }
}
