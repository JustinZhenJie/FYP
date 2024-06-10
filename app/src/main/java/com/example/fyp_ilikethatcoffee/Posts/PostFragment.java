package com.example.fyp_ilikethatcoffee.Posts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.fyp_ilikethatcoffee.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class PostFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference postRef;
    private String userEmail;
    private String storeEmail;
    private Query query;

    public PostFragment() {
        // Required empty public constructor
    }

    public static PostFragment newInstance(String storeEmail, String currentUserEmail) {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        args.putString("storeEmail", storeEmail);
        args.putString("currentUserEmail", currentUserEmail);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments() != null) {
            storeEmail = getArguments().getString("storeEmail");
            userEmail = getArguments().getString("currentUserEmail");
        }


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_all_posts, container, false);

        // Initialize Firestore database reference
        postRef = db.collection("Post");
        // Query only the posts belonging to the current store
        query = postRef.whereEqualTo("UserAccountId", storeEmail);
        // Initialize RecyclerView
        recyclerView = rootView.findViewById(R.id.posts_RV);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        // Set up FirestoreRecyclerOptions
        FirestoreRecyclerOptions<Post> options =
                new FirestoreRecyclerOptions.Builder<Post>()
                        .setQuery(query.orderBy("DateCreated", Query.Direction.DESCENDING), Post.class)
                        .build();

        // Initialize adapter
        postAdapter = new PostAdapter(options, userEmail);

        // Set adapter to RecyclerView
        recyclerView.setAdapter(postAdapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Start listening for data changes when the fragment starts
        recyclerView.getRecycledViewPool().clear();
        postAdapter.notifyDataSetChanged();
        postAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        // Stop listening for data changes when the fragment stops
        postAdapter.stopListening();
    }
}
/*

public class PostFragment extends Fragment {
    private RecyclerView recyclerView;

    public PostFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_all_posts, container, false);

        RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.posts_RV);
        rv.setHasFixedSize(true);
        PostAdapter adapter = new PostAdapter(new String[]{"test one", "test two", "test three", "test four", "test five" , "test six" , "test seven"});
        rv.setAdapter(adapter);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        return rootView;
    }
}

public class PostFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference postRef;

    public PostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firestore database reference
        postRef = db.collection("Post");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_all_posts, container, false);

        // Initialize RecyclerView
        recyclerView = rootView.findViewById(R.id.posts_RV);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up FirestoreRecyclerOptions
        FirestoreRecyclerOptions<Post> options =
                new FirestoreRecyclerOptions.Builder<Post>()
                        .setQuery(postRef, Post.class)
                        .build();

        // Initialize adapter
        postAdapter = new PostAdapter(options);

        // Set adapter to RecyclerView
        recyclerView.setAdapter(postAdapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Start listening for data changes when the fragment starts
        postAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        // Stop listening for data changes when the fragment stops
        postAdapter.stopListening();
    }
}



 */
