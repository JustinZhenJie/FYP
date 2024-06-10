package com.example.fyp_ilikethatcoffee;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.FriendRequestViewHolder> {

    private static final String TAG = "FriendRequestAdapter";
    private List<DocumentSnapshot> friendRequests;

    public FriendRequestAdapter(List<DocumentSnapshot> friendRequests) {
        this.friendRequests = friendRequests;
    }

    @NonNull
    @Override
    public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_request_item, parent, false);
        return new FriendRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestViewHolder holder, int position) {
        DocumentSnapshot friendRequestSnapshot = friendRequests.get(position);
        String senderId = friendRequestSnapshot.getString("senderId");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("UserAccount").document(senderId)
                .get()
                .addOnSuccessListener(userDocumentSnapshot -> {
                    UserAccount user = userDocumentSnapshot.toObject(UserAccount.class);
                    if (user != null) {
                        holder.userNameTextView.setText(user.getUserName());

                        // Load the profile image using Glide
                        Glide.with(holder.profileImageView.getContext())
                                .load(user.getImage())
                                .placeholder(R.drawable.img) // Placeholder image
                                .into(holder.profileImageView);

                        holder.acceptButton.setOnClickListener(v -> {
                            updateFriendRequestStatus(friendRequestSnapshot, "accepted", holder.itemView);
                            removeFriendRequest(holder.getAdapterPosition());
                        });

                        holder.rejectButton.setOnClickListener(v -> {
                            updateFriendRequestStatus(friendRequestSnapshot, "rejected", holder.itemView);
                            removeFriendRequest(holder.getAdapterPosition());
                        });
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error getting user details", e));
    }

    private void removeFriendRequest(int position) {
        friendRequests.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, friendRequests.size());
    }

    @Override
    public int getItemCount() {
        return friendRequests.size();
    }

    public static class FriendRequestViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView profileImageView;
        private TextView userNameTextView;
        private Button acceptButton;
        private Button rejectButton;

        public FriendRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image);
            userNameTextView = itemView.findViewById(R.id.text_user_name);
            acceptButton = itemView.findViewById(R.id.button_accept);
            rejectButton = itemView.findViewById(R.id.button_reject);
        }
    }

    private void updateFriendRequestStatus(DocumentSnapshot friendRequestSnapshot, String status, View itemView) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        friendRequestSnapshot.getReference().update("Status", status)
                .addOnSuccessListener(aVoid -> {
                    // Update successful
                    Log.d(TAG, "Friend request status updated successfully.");
                    // Show toast notification
                    String message = status.equals("accepted") ? "Friend request accepted" : "Friend request rejected";
                    Toast.makeText(itemView.getContext(), message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.w(TAG, "Error updating friend request status", e);
                    // Show toast notification for failure
                    Toast.makeText(itemView.getContext(), "Failed to update friend request status", Toast.LENGTH_SHORT).show();
                });
    }
}
