package com.example.fyp_ilikethatcoffee.Menu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fyp_ilikethatcoffee.Posts.Post;
import com.example.fyp_ilikethatcoffee.Posts.PostAdapter;
import com.example.fyp_ilikethatcoffee.Posts.PostFragment;
import com.example.fyp_ilikethatcoffee.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class CategoryFragment extends Fragment {
    private RecyclerView recyclerView;
    private CategoryAdapter categoryAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference catRef;
    private String username;
    private Query query;


    public CategoryFragment(){
        // Required empty public constructor
    }
    public static CategoryFragment newInstance(String username) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putString("USERNAME", username);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            username = getArguments().getString("USERNAME");
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_category_menu, container, false);
        // Initialize Firestore database reference
        catRef = db.collection("Store").document(username).collection("Category");
        // Initialize RecyclerView
        recyclerView = rootView.findViewById(R.id.menu_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Set up FirestoreRecyclerOptions
        FirestoreRecyclerOptions<Category> options =
                new FirestoreRecyclerOptions.Builder<Category>()
                        .setQuery(catRef, Category.class)
                        .build();
        // Initialize adapter
        categoryAdapter = new CategoryAdapter(options);
        // Set adapter to RecyclerView
        recyclerView.setAdapter(categoryAdapter);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        recyclerView.getRecycledViewPool().clear();
        categoryAdapter.notifyDataSetChanged();
        categoryAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        // Stop listening for data changes when the fragment stops
        categoryAdapter.stopListening();
    }
}

/*
public class CategoryFragment extends Fragment {
    private RecyclerView recyclerView;
    private CategoryAdapter categoryAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    //private
    private CollectionReference catRef;

    public CategoryFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firestore database reference
        catRef = db.collection("Store").document().collection("Category");


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_category_menu, container, false);

        // Initialize RecyclerView
        recyclerView = rootView.findViewById(R.id.menu_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up FirestoreRecyclerOptions
        FirestoreRecyclerOptions<Category> options =
                new FirestoreRecyclerOptions.Builder<Category>()
                        .setQuery(catRef, Category.class)
                        .build();

        // Initialize adapter
        categoryAdapter = new CategoryAdapter(options);

        // Set adapter to RecyclerView
        recyclerView.setAdapter(categoryAdapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Start listening for data changes when the fragment starts
        categoryAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        // Stop listening for data changes when the fragment stops
        categoryAdapter.stopListening();
    }
}

 */


