package com.example.fyp_ilikethatcoffee.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp_ilikethatcoffee.Friend;
import com.example.fyp_ilikethatcoffee.R;
import com.example.fyp_ilikethatcoffee.Review;
import com.example.fyp_ilikethatcoffee.ReviewCommentAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FriendsReviewFragment extends Fragment {
    private RecyclerView recyclerView;
    private ReviewCommentAdapter adapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Review> reviewList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review, container, false);
        recyclerView = view.findViewById(R.id.reviewRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchCurrentUserEmail();

        return view;
    }

    private void fetchCurrentUserEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userEmail = user.getEmail();
            fetchUsername(userEmail);
        } else {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchUsername(String userEmail) {
        db.collection("UserAccount")
                .whereEqualTo("Email", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String userName = queryDocumentSnapshots.getDocuments().get(0).getString("UserName");
                        initializeAdapter(userName);
                        loadFriends(userEmail);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to fetch user info", Toast.LENGTH_SHORT).show());
    }

    private void initializeAdapter(String userName) {
        adapter = new ReviewCommentAdapter(reviewList, userName);
        recyclerView.setAdapter(adapter);
    }

    private void loadFriends(String userEmail) {
        // First query to get friends where current user is the sender
        db.collection("Friend")
                .whereEqualTo("Status", "accepted")
                .whereEqualTo("senderId", userEmail)
                .get()
                .addOnSuccessListener(senderQuerySnapshot -> processFriendQueryResults(senderQuerySnapshot, userEmail))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to fetch friends: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void processFriendQueryResults(QuerySnapshot senderQuerySnapshot, String currentUserEmail) {
        List<String> friendEmails = new ArrayList<>();

        // Add friends where the current user is the sender
        for (DocumentSnapshot document : senderQuerySnapshot) {
            String receiverEmail = document.getString("receiverId");
            friendEmails.add(receiverEmail);
        }

        // Query for friends where the current user is the receiver
        db.collection("Friend")
                .whereEqualTo("Status", "accepted")
                .whereEqualTo("receiverId", currentUserEmail)
                .get()
                .addOnSuccessListener(receiverQuerySnapshot -> {
                    // Add friends where the current user is the receiver
                    for (DocumentSnapshot document : receiverQuerySnapshot) {
                        String senderEmail = document.getString("senderId");
                        friendEmails.add(senderEmail);
                    }
                    // Fetch reviews of all valid friends
                    if (!friendEmails.isEmpty()) {
                        fetchFriendReviews(friendEmails);
                    } else {
                        Log.d("FriendsReviewFragment", "No friends found or user has no accepted friends.");
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to fetch friends: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void fetchFriendReviews(List<String> friendEmails) {
        db.collection("Review")
                .whereIn("UserAccountId", friendEmails)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    reviewList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Review review = document.toObject(Review.class);
                        reviewList.add(review);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error loading reviews", Toast.LENGTH_SHORT).show());
    }
}
