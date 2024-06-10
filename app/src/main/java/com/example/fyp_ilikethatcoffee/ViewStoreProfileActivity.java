package com.example.fyp_ilikethatcoffee;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp_ilikethatcoffee.adapter.ReviewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewStoreProfileActivity extends AppCompatActivity {

    String storeName;
    CircleImageView profileIv;
    TextView StoreNameTv;
    TextView StoreAddressTv;
    TextView reviewCountTv;
    TextView averageRatingTv;

    RecyclerView recyclerview;
    private FirebaseFirestore db;


    com.example.fyp_ilikethatcoffee.adapter.ReviewAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_store_profile);

        storeName = getIntent().getStringExtra("storeName");


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        db = FirebaseFirestore.getInstance();

        recyclerview = findViewById(R.id.recyclerview);
        profileIv = findViewById(R.id.profileIv);
        StoreNameTv = findViewById(R.id.StoreNameTv);
        StoreAddressTv = findViewById(R.id.StoreAddressTv);
        reviewCountTv = findViewById(R.id.reviewCountTv);
        averageRatingTv = findViewById(R.id.averageRatingTv);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));

        loadStoreProfile();
        loadReviews(storeName);
    }


    private void loadReviews(String StoreName) {
        // Create a query to retrieve reviews where UserName matches the specified value
        Query query = db.collection("Review").whereEqualTo("StoreName", storeName);

        // Execute the query
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                // Check if the query result is not empty
                if (!queryDocumentSnapshots.isEmpty()) {
                    List<Review> reviewList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        long ReviewId = document.getLong("ReviewId");
                        String image = document.getString("Image");
                        double rating = document.getDouble("Rating");
                        String reviewDesc = document.getString("ReviewDesc");
                        String userAccountId = document.getString("UserAccountId");
                        String userName = document.getString("UserName");
                        String StoreId = document.getString("StoreId");

                        String UserProfilePic=null;
                        String StoreName=null;

                        if (document.contains("UserProfilePic")) {
                            UserProfilePic = document.getString("UserProfilePic");
                        }

                        if (document.contains("StoreName")) {
                            StoreName = document.getString("StoreName");
                        }

                        // Load comments for this review
                        String finalUserProfilePic = UserProfilePic;
                        String finalStoreName = StoreName;

                        // Load comments for this review
                        db.collection("Review").document(document.getId()).collection("Comment")
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            List<Comment> commentList = new ArrayList<>();
                                            for (QueryDocumentSnapshot commentDocument : task.getResult()) {
                                                // Extract comment data and add it to the list
                                                String commentId = commentDocument.getId();
                                                String commentInfo = commentDocument.getString("CommentInfo");
                                                String userName = commentDocument.getString("UserName");

                                                String UserAccountId = commentDocument.getString("UserAccountId");
                                                String UserProfilePic = commentDocument.getString("UserProfilePic");
                                                String UserType = commentDocument.getString("UserType");

                                                Comment comment = new Comment(userName, commentInfo, UserProfilePic, UserAccountId, UserType);
                                                commentList.add(comment);
                                            }

                                            Review review = new Review(ReviewId, reviewDesc, (float) rating, userName, image, commentList, userAccountId, finalUserProfilePic, finalStoreName,StoreId);
                                            reviewList.add(review);

                                            // Check if all reviews have been processed
                                            if (reviewList.size() == queryDocumentSnapshots.size()) {
                                                // Pass the reviewList to the adapter
                                                adapter = new ReviewAdapter(reviewList, ViewStoreProfileActivity.this);
                                                recyclerview.setAdapter(adapter);
                                            }
                                        } else {
                                            // Handle errors
                                            // For example, log the error or show a message to the user indicating failure to load comments
                                            //Log.e("LoadComments", "Error loading comments: " + task.getException().getMessage());
                                            Toast.makeText(ViewStoreProfileActivity.this, "Failed to load comments for review. Please try again later.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                } else {
                    // Handle case where no reviews are found for the given username
                    // For example, show a message to the user indicating no reviews found
                    Toast.makeText(ViewStoreProfileActivity.this, "No reviews found for the specified user", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle errors
                // For example, log the error or show a message to the user indicating failure to load reviews
                //Log.e("LoadReviews", "Error loading reviews: " + e.getMessage());
                Toast.makeText(ViewStoreProfileActivity.this, "Failed to load reviews. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadStoreProfile() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference userAccountsRef = db.collection("Store");
        // Query to filter documents where the "Email" field matches the given email
        Query query = userAccountsRef.whereEqualTo("StoreName", storeName);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {

                        String StoreAddress = document.getString("StoreAddress");
                        String StoreDesc = document.getString("StoreDesc");
                        String StoreName = document.getString("StoreName");
                        String UserAccountId = document.getString("UserAccountId");

                        if (document.contains("TotalReviews")) {
                            long reviewCount = document.getLong("TotalReviews");
                            reviewCountTv.setText("TotalReviews: " + reviewCount);
                        }

                        // long averageRating = document.getLong("averageRating");
                        String ImageUrl = document.getString("ImageUrl");

                        if (ImageUrl != null) {
                            Picasso.get().load(ImageUrl).into(profileIv);
                        }
                        if (StoreAddress != null) {
                            StoreAddressTv.setText(StoreAddress);
                            StoreNameTv.setText(StoreName);
                        }
                    }
                }
            }
        });
    }
}
