package com.example.fyp_ilikethatcoffee;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewCommentAdapter extends RecyclerView.Adapter<ReviewCommentAdapter.ReviewViewHolder> {

    private final List<Review> items;
    private final String currentUsername;
    private String userEmail;

    public ReviewCommentAdapter(List<Review> items, String currentUsername) {
        this.items = items;
        this.currentUsername = currentUsername;
        fetchCurrentUserEmail();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_list_item, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = items.get(position);
        holder.bind(holder.itemView.getContext(), review);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {
        private final TextView reviewDescTextView;
        private final TextView ratingTextView;
        private final TextView usernameTextView;
        private final ImageView reviewImageView;
        private final ImageView profileImageView;
        private final ImageButton commentButton;
        private final Button submitCommentButton, cancelCommentButton;
        private final LinearLayout showCommentsButton;
        private final ImageView showCommentsArrow;
        private final TextView showCommentsText;
        private final EditText commentEditText;
        private final LinearLayout commentsLayout, commentInputLayout;

        public ReviewViewHolder(View itemView) {
            super(itemView);
            reviewDescTextView = itemView.findViewById(R.id.reviewDescTextView);
            ratingTextView = itemView.findViewById(R.id.ratingTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            reviewImageView = itemView.findViewById(R.id.reviewImageView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            commentButton = itemView.findViewById(R.id.commentButton);
            submitCommentButton = itemView.findViewById(R.id.submitCommentButton);
            cancelCommentButton = itemView.findViewById(R.id.cancelCommentButton);
            showCommentsButton = itemView.findViewById(R.id.showCommentsButton);
            showCommentsArrow = itemView.findViewById(R.id.showCommentsArrow);
            showCommentsText = itemView.findViewById(R.id.showCommentsText);
            commentEditText = itemView.findViewById(R.id.commentEditText);
            commentsLayout = itemView.findViewById(R.id.commentsLayout);
            commentInputLayout = itemView.findViewById(R.id.commentInputLayout);
        }

        public void bind(Context context, final Review review) {
            reviewDescTextView.setText(review.getReviewDesc());
            ratingTextView.setText(String.format("Rating: %.1f", review.getRating()));
            usernameTextView.setText(review.getUserName());

            // Load profile picture using the UserProfilePic field
            if (review.getUserProfilePic() != null && !review.getUserProfilePic().isEmpty()) {
                Picasso.get()
                        .load(review.getUserProfilePic())
                        .placeholder(R.drawable.img)
                        .error(R.drawable.img)
                        .into(profileImageView);
            } else {
                profileImageView.setImageResource(R.drawable.img);
            }

            // Load review image
            if (review.getImage() != null && !review.getImage().isEmpty()) {
                Picasso.get()
                        .load(review.getImage())
                        .placeholder(R.drawable.img)
                        .error(R.drawable.img)
                        .into(reviewImageView);
            } else {
                reviewImageView.setVisibility(View.GONE);
            }

            // Set click listener for profile picture and username to open ConsumerProfileActivity
            View.OnClickListener profileClickListener = v -> openUserProfileByUsername(context, review.getUserName());

            profileImageView.setOnClickListener(profileClickListener);
            usernameTextView.setOnClickListener(profileClickListener);

            // Clear previous comments before adding new ones
            commentsLayout.removeAllViews();

            // Add each comment associated with this review
            List<Comment> comments = review.getComments();
            if (comments != null && !comments.isEmpty()) {
                showCommentsButton.setVisibility(View.VISIBLE);
                for (Comment comment : comments) {
                    View commentView = LayoutInflater.from(context).inflate(R.layout.comment_list_item, commentsLayout, false);
                    ImageView commentProfileImageView = commentView.findViewById(R.id.commentProfileImageView);
                    TextView commentUsernameTextView = commentView.findViewById(R.id.commentUsernameTextView);
                    TextView commentTextView = commentView.findViewById(R.id.commentTextView);

                    // Load profile picture for each comment
                    if (comment.getUserProfilePic() != null && !comment.getUserProfilePic().isEmpty()) {
                        Picasso.get()
                                .load(comment.getUserProfilePic())
                                .placeholder(R.drawable.img)
                                .error(R.drawable.img)
                                .into(commentProfileImageView);
                    } else {
                        commentProfileImageView.setImageResource(R.drawable.img);
                    }

                    String usernameText = comment.getUsername();
                    if ("StoreOwner".equals(comment.getUserType())) {
                        SpannableString spannable = new SpannableString(usernameText + " (Owner)");
                        spannable.setSpan(new ForegroundColorSpan(Color.BLUE), usernameText.length(), usernameText.length() + 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spannable.setSpan(new StyleSpan(Typeface.BOLD), usernameText.length(), usernameText.length() + 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        commentUsernameTextView.setText(spannable);
                    } else {
                        commentUsernameTextView.setText(usernameText);
                    }
                    commentTextView.setText(comment.getCommentInfo());

                    // Set click listener for comment profile picture and username
                    View.OnClickListener commentProfileClickListener = v -> openUserProfileByUsername(context, comment.getUsername());
                    commentProfileImageView.setOnClickListener(commentProfileClickListener);
                    commentUsernameTextView.setOnClickListener(commentProfileClickListener);

                    commentsLayout.addView(commentView);
                }
            } else {
                showCommentsButton.setVisibility(View.GONE);
            }

            // Toggle visibility of comments section
            showCommentsButton.setOnClickListener(v -> {
                if (commentsLayout.getVisibility() == View.GONE) {
                    commentsLayout.setVisibility(View.VISIBLE);
                    showCommentsArrow.setImageResource(R.drawable.up_arrow); // Change to collapse icon
                    showCommentsText.setText("Hide Comments");
                } else {
                    commentsLayout.setVisibility(View.GONE);
                    showCommentsArrow.setImageResource(R.drawable.down_arrow); // Change to expand icon
                    showCommentsText.setText("Show Comments");
                }
            });

            // Toggle visibility of comment input
            commentButton.setOnClickListener(v -> {
                if (commentInputLayout.getVisibility() == View.GONE) {
                    commentInputLayout.setVisibility(View.VISIBLE);
                } else {
                    commentInputLayout.setVisibility(View.GONE);
                }
            });

            // Handle cancel comment action
            cancelCommentButton.setOnClickListener(v -> {
                commentEditText.setText("");
                commentInputLayout.setVisibility(View.GONE);
            });

            // Handle submit comment action
            submitCommentButton.setOnClickListener(v -> {
                String commentInfo = commentEditText.getText().toString().trim();
                if (!commentInfo.isEmpty()) {
                    String reviewIdAsString = String.valueOf(review.getReviewId());
                    saveCommentToFirestore(context, reviewIdAsString, commentInfo);
                    commentEditText.setText("");
                    commentInputLayout.setVisibility(View.GONE);
                }
            });

            // TextWatcher to change the state of the submit button
            commentEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() > 0) {
                        submitCommentButton.setEnabled(true);
                        submitCommentButton.setBackgroundResource(R.drawable.circular_button_active);
                    } else {
                        submitCommentButton.setEnabled(false);
                        submitCommentButton.setBackgroundResource(R.drawable.circular_button_inactive);
                    }
                }
            });
        }

        private void openUserProfileByUsername(Context context, String username) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("UserAccount")
                    .whereEqualTo("UserName", username)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<com.google.firebase.firestore.QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<com.google.firebase.firestore.QuerySnapshot> task) {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                                String userEmail = document.getId();  // Document ID is the email

                                Log.d("ReviewAdapter", "Navigating to profile of user with email: " + userEmail);

                                Intent intent = new Intent(context, ConsumerViewConsumerProfileActivity.class);
                                intent.putExtra("user_email", userEmail);
                                context.startActivity(intent);
                            } else {
                                Log.e("ReviewAdapter", "No matching user found in UserAccount collection.");
                            }
                        }
                    });
        }

        private void saveCommentToFirestore(Context context, String reviewId, String commentInfo) {
            if (reviewId == null || currentUsername == null) {
                Log.e("ReviewAdapter", "Review ID or username is null. Comment not saved.");
                return;
            }
            Map<String, Object> commentData = new HashMap<>();
            commentData.put("CommentInfo", commentInfo);
            commentData.put("UserName", currentUsername);
            commentData.put("UserAccountId", userEmail);
            commentData.put("DateCreated", FieldValue.serverTimestamp());

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Review").document(reviewId)
                    .collection("Comment")
                    .add(commentData)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                Log.d("ReviewAdapter", "Comment added with ID: " + task.getResult().getId());

                                ((Activity) context).runOnUiThread(() -> {
                                    commentInputLayout.setVisibility(View.GONE);
                                });
                            } else {
                                Log.e("ReviewAdapter", "Error adding comment: ", task.getException());
                            }
                        }
                    });
        }
    }

    private void fetchCurrentUserEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userEmail = user.getEmail();
        } else {
            userEmail = null;
            Log.e("ReviewCommentAdapter", "No user logged in");
        }
    }
}
