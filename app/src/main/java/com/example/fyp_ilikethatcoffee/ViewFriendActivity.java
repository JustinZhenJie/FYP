package com.example.fyp_ilikethatcoffee;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp_ilikethatcoffee.adapter.FriendAdapter;
import com.example.fyp_ilikethatcoffee.adapter.ReviewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ViewFriendActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    RecyclerView recyclerview;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    private FirebaseFirestore db;
    StorageReference mImageRef;
    FriendAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_friend);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        recyclerview=findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        mImageRef = FirebaseStorage.getInstance().getReference().child("Image");
        progressDialog = new ProgressDialog(ViewFriendActivity.this);

        loadFriends();

    }


    private void loadFriends() {
        // Create a query to retrieve friends where either receiverId or senderId matches the specified ID
        Query query = db.collection("Friend")
                .whereEqualTo("Status", "accepted");

        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Handle query result
                        List<Friend> friendList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Friend friend = document.toObject(Friend.class);
                            if (friend.getReceiverId().equals(mUser.getEmail()) || friend.getSenderId().equals(mUser.getEmail())) {
                                friendList.add(friend);
                            }
                        }
                        adapter = new FriendAdapter(friendList, ViewFriendActivity.this);
                        recyclerview.setAdapter(adapter);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle errors
                    }
                });

    }
}