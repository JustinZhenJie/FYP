package com.example.fyp_ilikethatcoffee.Menu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp_ilikethatcoffee.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MenuFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference catRef;
    private MenuAdapter menuAdapter;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private String username;
    private Query query;

    public MenuFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        catRef = db.collection("Store").document()
                .collection("Category")
                .document("1")
                .collection("MenuItem");


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_category_menu, container, false);


        // Initialize RecyclerView
        recyclerView = rootView.findViewById(R.id.menu_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up FirestoreRecyclerOptions
        FirestoreRecyclerOptions<com.example.fyp_ilikethatcoffee.Menu.MenuItem> options =
                new FirestoreRecyclerOptions.Builder<com.example.fyp_ilikethatcoffee.Menu.MenuItem>()
                        .setQuery(catRef, MenuItem.class)
                        .build();

        // Initialize adapter
        //menuAdapter = new MenuAdapter(options);

        // Set adapter to RecyclerView
        recyclerView.setAdapter(menuAdapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Start listening for data changes when the fragment starts
        //menuAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        // Stop listening for data changes when the fragment stops
        //menuAdapter.stopListening();
    }
}

/*
public class MenuFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    //private
    private CollectionReference catRef;
    private ImageView add_icon;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private MenuAdapter menuAdapter;

    public MenuFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_menu, container, false);
        user = auth.getCurrentUser();
        if (user == null){
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
        }

        catRef = db.collection("Store").document(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .collection("Category").document("1").collection("MenuItem");


        // Initialize RecyclerView
        recyclerView = rootView.findViewById(R.id.menu_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up FirestoreRecyclerOptions
        FirestoreRecyclerOptions<MenuItem> options =
                new FirestoreRecyclerOptions.Builder<MenuItem>()
                        .setQuery(catRef, MenuItem.class)
                        .build();

        // Initialize adapter
        menuAdapter = new MenuAdapter(options);

        // Set adapter to RecyclerView
        recyclerView.setAdapter(menuAdapter);

        return rootView;


    }

    @Override
    public void onStart() {
        super.onStart();
        // Start listening for data changes when the fragment starts
        menuAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        // Stop listening for data changes when the fragment stops
        menuAdapter.stopListening();
    }
}


 */