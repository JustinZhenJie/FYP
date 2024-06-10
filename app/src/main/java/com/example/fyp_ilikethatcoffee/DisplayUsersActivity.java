package com.example.fyp_ilikethatcoffee;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.example.fyp_ilikethatcoffee.adapter.UserAccountAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class DisplayUsersActivity extends AppCompatActivity {

    RecyclerView recyclerview;
    UserAccountAdapter adapter;
    List<UserAccount> userAccountsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_displayuser);
        userAccountsList = new ArrayList<>();
        recyclerview = findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        loadUserAccounts();

    }


    public void loadUserAccounts() {
        FirebaseFirestore db;
        db = FirebaseFirestore.getInstance();
        CollectionReference userAccountsRef = db.collection("UserAccount");

        userAccountsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    userAccountsList = new ArrayList<>();
                    for (DocumentSnapshot document : task.getResult()) {
//                            String name = document.getString("Name");
//                            UserAccount userAccount = document.toObject(UserAccount.class);

                        String documentId = document.getId();


                        String name = document.getString("Name");
                        String email = document.getString("Email");
                        String password = document.getString("Password");
                        Long phoneLong = document.getLong("Phone"); // Retrieve as long
                        long phone = (phoneLong != null) ? phoneLong : 0L; // Set a default value if phoneLong is null

                        String userType = document.getString("UserType");

                        UserAccount a = new UserAccount(documentId, email, name, password, phone + "", userType, userType, true,"","","");


                        userAccountsList.add(a);
                    }

                    setRecyclerview();

                } else {
                    Toast.makeText(DisplayUsersActivity.this, "No Data Available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void setRecyclerview() {
        adapter = new UserAccountAdapter(userAccountsList, DisplayUsersActivity.this);
        recyclerview.setAdapter(adapter);
    }
}


