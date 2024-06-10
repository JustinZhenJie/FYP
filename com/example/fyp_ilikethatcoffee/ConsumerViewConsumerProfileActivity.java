package com.example.fyp_ilikethatcoffee;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp_ilikethatcoffee.adapter.ReviewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;

public class ConsumerViewConsumerProfileActivity extends AppCompatActivity {

    private Button addFriendButton; // New "Add Friend" button
    private TextView usernameTv; // Declare TextView variables
    private TextView dobTv;
    private TextView bioTv;
    private CircleImageView imageIv; // Declare CircleImageView variable
    private FirebaseFirestore db;

    RecyclerView recyclerview;
    com.example.fyp_ilikethatcoffee.adapter.ReviewAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumer_view_consumer_profile);
        recyclerview=findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        // Get the email of the user to be displayed from the intent extras
        String userEmail = getIntent().getStringExtra("user_email");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        db = FirebaseFirestore.getInstance();

        addFriendButton = findViewById(R.id.addFriendButton);
        usernameTv = findViewById(R.id.usernameTv); // Initialize TextView variables
        dobTv = findViewById(R.id.dobTv);
        bioTv = findViewById(R.id.bioTv);
        imageIv = findViewById(R.id.imageIv); // Initialize CircleImageView variable

        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                if (addFriendButton.getText().toString().equals("Added")) {
                    // Show confirmation dialog
                    new AlertDialog.Builder(ConsumerViewConsumerProfileActivity.this)
                            .setTitle("Delete Friend")
                            .setMessage("Are you sure you want to delete this friend?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteFriend(currentUserEmail, userEmail);
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    // Send friend request
                    sendFriendRequest(currentUserEmail, userEmail);
                }
            }
        });

        // Load user account data using the retrieved email
        loadUserAccounts(userEmail);
    }

    private void loadUserAccounts(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("UserAccount")
                .whereEqualTo("Email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            // Retrieve user account data
                            UserAccount userAccount = document.toObject(UserAccount.class);
                            // Populate the UI elements with user account data
                            assignData(userAccount);

                            // Log user data for debugging
                            Log.d("UserAccountData", "Username: " + userAccount.getUserName());
                            Log.d("UserAccountData", "Email: " + userAccount.getEmail());

                            // Check if the user is already a friend before updating the button
                            String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                            checkFriendRequest(currentUserEmail, email);

                            loadReviews(userAccount.getUserName());
                            // Add more log statements for other fields as needed
                        }
                    } else {
                        // Log error message for debugging
                        Log.e("UserAccountData", "Error getting user account data: ", task.getException());
                        Toast.makeText(ConsumerViewConsumerProfileActivity.this, "No Data Available", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void assignData(UserAccount userAccount) {
        usernameTv.setText(userAccount.getUserName());
        dobTv.setText(userAccount.getDob());
        bioTv.setText(userAccount.getBio());

        if (userAccount.getImage() != null && !userAccount.getImage().isEmpty()) {
            Picasso.get().load(userAccount.getImage()).into(imageIv);
        }
    }

    private void checkFriendRequest(String senderId, String receiverId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Friend")
                .whereEqualTo("senderId", senderId)
                .whereEqualTo("receiverId", receiverId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String status = document.getString("Status");
                            if (status != null) {
                                switch (status) {
                                    case "pending":
                                        addFriendButton.setText("Pending");
                                        addFriendButton.setEnabled(false);
                                        addFriendButton.setBackgroundColor(getResources().getColor(R.color.grey));
                                        return;
                                    case "accepted":
                                        addFriendButton.setText("Added");
                                        addFriendButton.setEnabled(true);
                                        addFriendButton.setBackgroundColor(getResources().getColor(R.color.green));
                                        return;
                                    default:
                                        break;
                                }
                            }
                        }
                        checkOppositeFriendRequest(receiverId, senderId);
                    } else {
                        Toast.makeText(ConsumerViewConsumerProfileActivity.this, "Error checking friend request", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void checkOppositeFriendRequest(String senderId, String receiverId) {
        // Query the Firestore database to check if a friend request exists with sender and receiver switched
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Friend")
                .whereEqualTo("senderId", senderId)
                .whereEqualTo("receiverId", receiverId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            // If a friend request exists, check its status
                            String status = document.getString("Status");
                            if (status != null) {
                                switch (status) {
                                    case "pending":
                                        // If the status is pending, change the button text to "Pending request"
                                        addFriendButton.setText("Pending");
                                        addFriendButton.setEnabled(false); // Disable the button
                                        addFriendButton.setBackgroundColor(getResources().getColor(R.color.grey)); // Change button color to grey
                                        return;
                                    case "accepted":
                                        // If the status is accepted, change the button text to "Added" with green color
                                        addFriendButton.setText("Added");
                                        addFriendButton.setEnabled(true); // Disable the button
                                        addFriendButton.setBackgroundColor(getResources().getColor(R.color.green)); // Change button color to green
                                        return;
                                    default:
                                        break;
                                }
                            }
                        }
                        // If no friend request exists, do nothing
                    } else {
                        // Handle errors
                        Toast.makeText(ConsumerViewConsumerProfileActivity.this, "Error checking friend request", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendFriendRequest(String senderId, String receiverId) {
        // Create a new document in the Friend collection to send a friend request
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> friendRequest = new HashMap<>();
        friendRequest.put("senderId", senderId);
        friendRequest.put("receiverId", receiverId);
        friendRequest.put("Status", "pending");

        db.collection("Friend")
                .add(friendRequest)
                .addOnSuccessListener(documentReference -> {
                    // Show a toast indicating the friend request has been sent
                    Toast.makeText(ConsumerViewConsumerProfileActivity.this, "Friend request sent", Toast.LENGTH_SHORT).show();
                    addFriendButton.setText("Pending");
                    addFriendButton.setEnabled(false); // Disable the button
                    addFriendButton.setBackgroundColor(getResources().getColor(R.color.grey)); // Change button color to grey
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    Toast.makeText(ConsumerViewConsumerProfileActivity.this, "Failed to send friend request", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteFriend(String senderId, String receiverId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Friend")
                .whereEqualTo("senderId", senderId)
                .whereEqualTo("receiverId", receiverId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(ConsumerViewConsumerProfileActivity.this, "Friend deleted", Toast.LENGTH_SHORT).show();
                                        addFriendButton.setText("Add Friend");
                                        addFriendButton.setEnabled(true);
                                        addFriendButton.setBackgroundColor(getResources().getColor(R.color.purple_500)); // Reset to original color
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(ConsumerViewConsumerProfileActivity.this, "Failed to delete friend", Toast.LENGTH_SHORT).show();
                                    });
                        }
                        checkOppositeFriendRequestForDeletion(receiverId, senderId);
                    } else {
                        Toast.makeText(ConsumerViewConsumerProfileActivity.this, "Error deleting friend", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkOppositeFriendRequestForDeletion(String senderId, String receiverId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Friend")
                .whereEqualTo("senderId", senderId)
                .whereEqualTo("receiverId", receiverId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(ConsumerViewConsumerProfileActivity.this, "Friend deleted", Toast.LENGTH_SHORT).show();
                                        addFriendButton.setText("Add Friend");
                                        addFriendButton.setEnabled(true);
                                        addFriendButton.setBackgroundColor(getResources().getColor(R.color.purple_500)); // Reset to original color
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(ConsumerViewConsumerProfileActivity.this, "Failed to delete friend", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(ConsumerViewConsumerProfileActivity.this, "Error deleting friend", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadReviews(String username) {
        // Create a query to retrieve reviews where UserName matches the specified value
        Query query = db.collection("Review").whereEqualTo("UserName", username);

        // Execute the query
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            // Check if the query result is not empty
            if (!queryDocumentSnapshots.isEmpty()) {
                List<Review> reviewList = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    // Debug log to check document data
                    Log.d("ReviewData", "Document: " + document.getData());

                    // Extract fields safely
                    long reviewId = document.contains("ReviewId") ? document.getLong("ReviewId") : -1;
                    String image = document.contains("Image") ? document.getString("Image") : null;
                    double rating = document.contains("Rating") ? document.getDouble("Rating") : 0.0;
                    String reviewDesc = document.contains("ReviewDesc") ? document.getString("ReviewDesc") : "";
                    String storeName = document.contains("StoreName") ? document.getString("StoreName") : "";
                    String userAccountId = document.contains("UserAccountId") ? document.getString("UserAccountId") : "";
                    String reviewUserName = document.contains("UserName") ? document.getString("UserName") : "";
                    String storeId = document.contains("StoreId") ? document.getString("StoreId") : "";

                    String userProfilePic = document.contains("UserProfilePic") ? document.getString("UserProfilePic") : null;
                    String finalStoreName = document.contains("StoreName") ? document.getString("StoreName") : null;

                    // Load comments for this review
                    db.collection("Review").document(document.getId()).collection("Comment")
                            .get().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    List<Comment> commentList = new ArrayList<>();
                                    for (QueryDocumentSnapshot commentDocument : task.getResult()) {
                                        // Extract comment data safely
                                        String commentId = commentDocument.getId();
                                        String commentInfo = commentDocument.getString("CommentInfo");
                                        String commentUserName = commentDocument.getString("UserName");
                                        String commentUserAccountId = commentDocument.getString("UserAccountId");
                                        String commentUserProfilePic = commentDocument.contains("UserProfilePic") ? commentDocument.getString("UserProfilePic") : null;
                                        String userType = commentDocument.getString("UserType");

                                        Comment comment = new Comment(commentUserName, commentInfo, commentUserProfilePic, commentUserAccountId, userType);
                                        commentList.add(comment);
                                    }

                                    // Create the review object with associated comments
                                    Review review = new Review(reviewId, reviewDesc, (float) rating, reviewUserName, image, commentList, userAccountId, userProfilePic, finalStoreName, storeId);
                                    reviewList.add(review);

                                    // Check if all reviews have been processed
                                    if (reviewList.size() == queryDocumentSnapshots.size()) {
                                        // Pass the reviewList to the adapter
                                        adapter = new ReviewAdapter(reviewList, ConsumerViewConsumerProfileActivity.this);
                                        recyclerview.setAdapter(adapter);
                                    }
                                } else {
                                    // Handle errors
                                    Toast.makeText(ConsumerViewConsumerProfileActivity.this, "Failed to load comments for review. Please try again later.", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            } else {
                // Handle case where no reviews are found for the given username
                Toast.makeText(ConsumerViewConsumerProfileActivity.this, "No reviews found for the specified user", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            // Handle errors
            Toast.makeText(ConsumerViewConsumerProfileActivity.this, "Failed to load reviews. Please try again later.", Toast.LENGTH_SHORT).show();
        });
    }


}