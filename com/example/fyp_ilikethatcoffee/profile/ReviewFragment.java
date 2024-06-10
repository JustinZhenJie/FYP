package com.example.fyp_ilikethatcoffee.profile;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.fyp_ilikethatcoffee.Comment;
import com.example.fyp_ilikethatcoffee.R;
import com.example.fyp_ilikethatcoffee.Review;
import com.example.fyp_ilikethatcoffee.UserAccount;
import com.example.fyp_ilikethatcoffee.adapter.ReviewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;


public class ReviewFragment extends Fragment {

    FirebaseAuth mAth;
    FirebaseUser mUser;
    StorageReference mImageRef;

    String documentId;

    private FirebaseFirestore db;

    RecyclerView recyclerview;
    com.example.fyp_ilikethatcoffee.adapter.ReviewAdapter adapter;

    public ReviewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_review2, container, false);
        recyclerview = view.findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));

        mAth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mUser = mAth.getCurrentUser();
        mImageRef = FirebaseStorage.getInstance().getReference().child("Image");


        loadUserAccounts(mUser.getEmail());

        return view;
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
                        loadReviews(userName);
                    }
                } else {
                    Toast.makeText(getContext(), "No Data Available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadReviews(String userName) {
        // Create a query to retrieve reviews where UserName matches the specified value
        Query query = db.collection("Review").whereEqualTo("UserName", userName);

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
                                                String UserType = commentDocument.getString(("UserType"));

                                                Comment comment = new Comment(userName, commentInfo, UserProfilePic, UserAccountId, UserType);
                                                commentList.add(comment);
                                            }


                                            Review review = new Review(ReviewId, reviewDesc, (float) rating, userName, image, commentList, userAccountId, finalUserProfilePic, finalStoreName,StoreId);
                                            reviewList.add(review);

                                            // Check if all reviews have been processed
                                            if (reviewList.size() == queryDocumentSnapshots.size()) {
                                                // Pass the reviewList to the adapter
                                                adapter = new ReviewAdapter(reviewList, getContext());
                                                recyclerview.setAdapter(adapter);
                                            }
                                        } else {
                                            // Handle errors
                                            // For example, log the error or show a message to the user indicating failure to load comments
                                            //Log.e("LoadComments", "Error loading comments: " + task.getException().getMessage());
                                            Toast.makeText(getContext(), "Failed to load comments for review. Please try again later.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                } else {
                    // Handle case where no reviews are found for the given username
                    // For example, show a message to the user indicating no reviews found
                    Toast.makeText(getContext(), "No reviews found for the specified user", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle errors
                // For example, log the error or show a message to the user indicating failure to load reviews
                //Log.e("LoadReviews", "Error loading reviews: " + e.getMessage());
                Toast.makeText(getContext(), "Failed to load reviews. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}