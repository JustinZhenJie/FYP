package com.example.fyp_ilikethatcoffee;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StoresFragment extends Fragment {

    // Declare variables
    private ListView listView;
    private StoreAdapter adapter;
    private List<Store> storeList;
    private List<Store> fullStoreList;  // To keep the original list
    private String username;
    private SearchView searchView;
    private Spinner ratingSpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragments_stores, container, false);

        // Retrieve the username argument passed from MainActivity
        username = getArguments().getString("USERNAME");

        // Initialize views
        listView = view.findViewById(R.id.listView);
        searchView = view.findViewById(R.id.searchView);
        ratingSpinner = view.findViewById(R.id.ratingSpinner);

//        // Set the hint for the SearchView
//        searchView.setQueryHint("Search for stores");

        // Initialize store list
        storeList = new ArrayList<>();
        fullStoreList = new ArrayList<>();  // Initialize the full store list

        // Initialize adapter
        adapter = new StoreAdapter(requireContext(), R.layout.list_item_store, storeList);
        listView.setAdapter(adapter);

        // Load stores from database
        loadStoresFromDatabase();

        // Set item click listener
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            // Handle item click, navigate to store profile fragment
            Store store = storeList.get(position);
            startActivityForStoreProfile(store);
        });

        // Set up search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    // If the search query is empty, display the full list of stores
                    adapter.clear();
                    adapter.addAll(fullStoreList);
                    adapter.notifyDataSetChanged();
                } else {
                    // Filter the list of stores based on the search query
                    filterStores(newText);
                }
                return true;
            }
        });

        // Set up rating filter functionality
        ratingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected rating
                String selectedRating = parent.getItemAtPosition(position).toString();
                if (selectedRating.equals("All Ratings")) {
                    adapter.clear();
                    adapter.addAll(fullStoreList);
                    adapter.notifyDataSetChanged();
                } else {
                    int rating = Integer.parseInt(selectedRating);
                    filterStoresByRating(rating);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        return view;
    }

    private void loadStoresFromDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        storeList.clear();  // Clear the existing data to avoid duplicating stores
        fullStoreList.clear();  // Clear the full store list as well
        db.collection("Store")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task<?>> reviewTasks = new ArrayList<>(); // To track review fetching tasks
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Store store = new Store(
                                    document.getString("StoreAddress"),
                                    document.getString("StoreDesc"),
                                    document.getString("StoreName"),
                                    document.getString("UserAccountId")
                            );
                            storeList.add(store);
                            fullStoreList.add(store);  // Add to the full store list as well
                            Task<QuerySnapshot> fetchReviewsTask = db.collection("Review")
                                    .whereEqualTo("StoreName", store.getStoreName())
                                    .get()
                                    .addOnCompleteListener(reviewCompletionTask -> {
                                        if (reviewCompletionTask.isSuccessful()) {
                                            int reviewCount = reviewCompletionTask.getResult().size();
                                            double totalRating = 0;
                                            for (QueryDocumentSnapshot review : reviewCompletionTask.getResult()) {
                                                totalRating += review.getDouble("Rating");
                                            }
                                            double averageRating = reviewCount > 0 ? totalRating / reviewCount : 0;
                                            store.setReviewCount(reviewCount);
                                            store.setAverageRating(averageRating);
                                        }
                                    });
                            reviewTasks.add(fetchReviewsTask);
                        }

                        Tasks.whenAllComplete(reviewTasks).addOnCompleteListener(allReviewsCompleted -> {
                            adapter.notifyDataSetChanged(); // Update adapter after all reviews are processed
                            Log.d("StoresFragment", "All reviews processed and UI updated.");
                        });
                    } else {
                        Log.e("Firestore", "Error getting documents: ", task.getException());
                    }
                });
    }

    // Method to filter stores based on the search query
    private void filterStores(String query) {
        Log.d("StoresFragment", "Filtering stores with query: " + query);
        List<Store> filteredList = new ArrayList<>();
        for (Store store : fullStoreList) {
            if (store.getStoreName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(store);
            }
        }
        adapter.clear();
        adapter.addAll(filteredList);
        adapter.notifyDataSetChanged();
        Log.d("StoresFragment", "Filtered stores size: " + filteredList.size());
    }

    // Method to filter stores based on the selected rating
    private void filterStoresByRating(int rating) {
        Log.d("StoresFragment", "Filtering stores with rating: " + rating);
        List<Store> filteredList = new ArrayList<>(fullStoreList);  // Start with the full list
        Collections.sort(filteredList, new Comparator<Store>() {
            @Override
            public int compare(Store s1, Store s2) {
                return Double.compare(Math.abs(s1.getAverageRating() - rating), Math.abs(s2.getAverageRating() - rating));
            }
        });
        adapter.clear();
        adapter.addAll(filteredList);
        adapter.notifyDataSetChanged();
        Log.d("StoresFragment", "Filtered stores size: " + filteredList.size());
    }

    private void startActivityForStoreProfile(Store store) {
        // Log the user account ID before starting the activity
        Log.d("getUserAccountId", "User Account ID: " + store.getUserAccountId());
        // Start ConsumerStoreProfileActivity passing store information
        Intent intent = new Intent(requireContext(), ConsumerStoreProfileActivity.class);
        intent.putExtra("StoreName", store.getStoreName());
        intent.putExtra("StoreDesc", store.getStoreDesc());
        intent.putExtra("StoreAddress", store.getStoreAddress());
        intent.putExtra("StoreEmail", store.getUserAccountId());
        intent.putExtra("USERNAME", username);
        startActivity(intent);
    }
}
