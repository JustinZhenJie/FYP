package com.example.fyp_ilikethatcoffee.ui.discover;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp_ilikethatcoffee.ConsumerViewConsumerProfileActivity;
import com.example.fyp_ilikethatcoffee.MapsActivity;
import com.example.fyp_ilikethatcoffee.R;
import com.example.fyp_ilikethatcoffee.UserAccount;
import com.example.fyp_ilikethatcoffee.UserAdapter;
import com.example.fyp_ilikethatcoffee.databinding.FragmentDiscoverBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DiscoverFragment extends Fragment {

    private FragmentDiscoverBinding binding;
    private androidx.appcompat.widget.SearchView searchView;
    private RecyclerView recyclerViewSearchResults;
    private UserAdapter userAdapter;

    private FirebaseAuth mAuth;
    private String currentUserUsername;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDiscoverBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        searchView = root.findViewById(R.id.searchView);
        recyclerViewSearchResults = root.findViewById(R.id.recyclerViewSearchResults);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Initialize RecyclerView
        recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter = new UserAdapter();
        recyclerViewSearchResults.setAdapter(userAdapter);

        // Get the current user's information
        getCurrentUserInfo();

        // Set up search functionality
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Optional: You can also perform real-time search here
                return false;
            }
        });

        if (getArguments() != null) {
            currentUserUsername = getArguments().getString("USERNAME");
        }

        CardView cardViewMap = root.findViewById(R.id.cardViewMap);
        cardViewMap.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MapsActivity.class);
            intent.putExtra("USERNAME", currentUserUsername);
            startActivity(intent);
        });

        return root;
    }

    private void getCurrentUserInfo() {
        // Check if a user is currently authenticated
        if (mAuth.getCurrentUser() != null) {
            // Get the current user's email
            String currentUserEmail = mAuth.getCurrentUser().getEmail();

            // Query Firestore database for the user document corresponding to the current user's email
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("UserAccount")
                    .whereEqualTo("Email", currentUserEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Get the username from the retrieved user document
                                currentUserUsername = document.getString("UserName");
                                Log.d("DiscoverFragment", "Current User Username: " + currentUserUsername);
                            }
                        } else {
                            // Handle failure
                            Log.e("DiscoverFragment", "Error getting user document: ", task.getException());
                            Toast.makeText(getContext(), "Failed to retrieve user information", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getContext(), "No user is currently authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void performSearch(String query) {
        performSearchWithUsername(query, currentUserUsername);
    }

    private void performSearchWithUsername(String query, String currentUserUsername) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("UserAccount")
                .whereEqualTo("UserType", "Consumer") // Add this condition
                .whereGreaterThanOrEqualTo("UserName", query)
                .whereLessThan("UserName", query + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<UserAccount> searchResults = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            UserAccount user = document.toObject(UserAccount.class);
                            if (!user.getUserName().equals(currentUserUsername)) {
                                searchResults.add(user);
                            }
                        }
                        userAdapter.setUserList(searchResults);
                        userAdapter.setOnItemClickListener(user -> {
                            Intent intent = new Intent(getActivity(), ConsumerViewConsumerProfileActivity.class);
                            intent.putExtra("user_email", user.getEmail());
                            startActivity(intent);
                        });
                    } else {
                        Toast.makeText(getContext(), "Search failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
