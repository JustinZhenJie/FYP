package com.example.fyp_ilikethatcoffee.profile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.fyp_ilikethatcoffee.R;
import com.example.fyp_ilikethatcoffee.Store;
import com.example.fyp_ilikethatcoffee.adapter.StoreAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class FavLocationFragment extends Fragment {


    RecyclerView recyclerView;
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    public FavLocationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fav_location, container, false);
        recyclerView = view.findViewById(R.id.recyclerview);
        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadFavouriteStores();

        return view;
    }

    private void loadFavouriteStores() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference favouriteStoresRef = db.collection("UserAccount").document(mUser.getEmail()).collection("Favourite");

        favouriteStoresRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<String> favouriteStores = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String storeName = document.getString("StoreName");
                        if (storeName != null) {
                            favouriteStores.add(storeName);
                        }
                    }
                    // Set up RecyclerView
                    StoreAdapter adapter = new StoreAdapter(favouriteStores, getContext());
                    recyclerView.setAdapter(adapter);
                }
            }
        });
    }
}