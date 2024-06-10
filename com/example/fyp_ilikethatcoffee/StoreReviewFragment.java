package com.example.fyp_ilikethatcoffee;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class StoreReviewFragment extends Fragment {
    private RecyclerView reviewRecyclerView;
    private ReviewCommentAdapter adapter;
    private List<Review> items;
    private FirebaseFirestore db;
    private String storeName;
    private String username;

    public static StoreReviewFragment newInstance(String storeName, String username) {
        StoreReviewFragment fragment = new StoreReviewFragment();
        Bundle args = new Bundle();
        args.putString("StoreName", storeName);
        args.putString("USERNAME", username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            storeName = getArguments().getString("StoreName");
            username = getArguments().getString("USERNAME");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review, container, false);
        Log.d("TAG", "ReviewFragment onCreateView: Started");

        db = FirebaseFirestore.getInstance();
        reviewRecyclerView = view.findViewById(R.id.reviewRecyclerView);
        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        items = new ArrayList<>();
        adapter = new ReviewCommentAdapter(items, username);
        reviewRecyclerView.setAdapter(adapter);

        listenForReviewUpdates();

        return view;
    }

    private void listenForReviewUpdates() {
        Query reviewQuery = db.collection("Review").whereEqualTo("StoreName", storeName);
        reviewQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("TAG", "Error listening for review updates: ", error);
                    return;
                }

                if (value != null) {
                    for (DocumentChange change : value.getDocumentChanges()) {
                        QueryDocumentSnapshot document = change.getDocument();
                        Review review = document.toObject(Review.class);
                        review.setReviewId(Long.valueOf(document.getId()));

                        switch (change.getType()) {
                            case ADDED:
                                loadUserProfilePicture(review.getUserName(), review::setUserProfilePic);
                                items.add(review);
                                loadCommentsForReview(document.getId(), review);
                                break;
                            case MODIFIED:
                                for (int i = 0; i < items.size(); i++) {
                                    if (items.get(i).getReviewId().equals(review.getReviewId())) {
                                        items.set(i, review);
                                        loadCommentsForReview(document.getId(), review);
                                        break;
                                    }
                                }
                                break;
                            case REMOVED:
                                items.removeIf(r -> r.getReviewId().equals(review.getReviewId()));
                                break;
                        }
                    }

                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void loadCommentsForReview(String reviewId, final Review review) {
        db.collection("Review").document(reviewId).collection("Comment")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("TAG", "Error listening for comment updates: ", error);
                            return;
                        }

                        if (value != null) {
                            List<Comment> comments = new ArrayList<>();
                            for (QueryDocumentSnapshot commentDocument : value) {
                                Comment comment = commentDocument.toObject(Comment.class);

                                // Load the user's profile picture
                                loadUserProfilePicture(comment.getUsername(), comment::setUserProfilePic);

                                String userAccountId = commentDocument.getString("UserAccountId");
                                // Fetch user type and update comment display accordingly
                                db.collection("UserAccount").document(userAccountId).get().addOnSuccessListener(documentSnapshot -> {
                                    String userType = documentSnapshot.getString("UserType");
                                    if ("StoreOwner".equals(userType)) {
                                        comment.setUserType(userType);  // Assuming a setUserType method exists
                                    }

                                    comments.add(comment);
                                    if (comments.size() == value.size()) {
                                        review.setComments(comments);
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    }
                });
    }


    private void loadUserProfilePicture(String userName, Consumer<String> setImageCallback) {
        db.collection("UserAccount")
                .whereEqualTo("UserName", userName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String imageUrl = task.getResult().getDocuments().get(0).getString("Image");
                        setImageCallback.accept(imageUrl);
                        Log.e("UserProfile", "Fetched user profile picture for: " + imageUrl);
                    } else {
                        setImageCallback.accept(null); // or provide a default image URL
                        Log.e("UserProfile", "Error fetching user profile picture for: " + userName);
                    }
                });
    }
}
