package com.example.fyp_ilikethatcoffee.ui.notifications;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp_ilikethatcoffee.FriendRequestAdapter;
import com.example.fyp_ilikethatcoffee.R;
import com.example.fyp_ilikethatcoffee.UserAccount;
import com.example.fyp_ilikethatcoffee.databinding.FragmentNotificationsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private RecyclerView recyclerView;
    private FriendRequestAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = root.findViewById(R.id.recycler_friend_requests);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Fetch and display friend requests
        fetchFriendRequests();

        return root;
    }

    private void fetchFriendRequests() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            // No user is signed in
            Log.d(TAG, "No user signed in");
            return;
        }

        CollectionReference friendRequestsRef = db.collection("Friend");

        Query query = friendRequestsRef.whereEqualTo("receiverId", currentUser.getEmail())
                .whereEqualTo("Status", "pending");

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<DocumentSnapshot> friendRequests = new ArrayList<>(); // Change to hold DocumentSnapshot
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                friendRequests.add(documentSnapshot); // Add DocumentSnapshot directly
            }
            // Create adapter with the list of DocumentSnapshots
            adapter = new FriendRequestAdapter(friendRequests);
            recyclerView.setAdapter(adapter);

        }).addOnFailureListener(e -> {
            // Handle failure to fetch friend requests
            Log.e(TAG, "Error getting friend requests", e);
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
