package com.example.fyp_ilikethatcoffee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.style.UpdateAppearance;
import android.widget.Button;
import android.widget.Toast;

import com.example.fyp_ilikethatcoffee.adapter.UserSuspensionAccountAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SuspendAccountActivity extends AppCompatActivity implements UpdateUserAccount {


    RecyclerView recyclerview;
    UserSuspensionAccountAdapter adapter;
    List<UserAccount> userAccountsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suspend_account);


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
                        long phone = (phoneLong != null) ? phoneLong : 0L;
                        String userType = document.getString("UserType");

                        boolean isEnable = document.getBoolean("IsEnable"); // Retrieve as boolean

                        UserAccount a = new UserAccount(documentId, email, name, password, phone + "", userType, userType, isEnable, "","","");

                        userAccountsList.add(a);
                    }

                    setRecyclerview();

                } else {
                    Toast.makeText(SuspendAccountActivity.this, "No Data Available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void setRecyclerview() {
        adapter = new UserSuspensionAccountAdapter(userAccountsList, SuspendAccountActivity.this);
        recyclerview.setAdapter(adapter);
    }

    @Override
    public void updateUserAccount(UserAccount userAccount) {
        FirebaseFirestore db = db = FirebaseFirestore.getInstance();
        boolean isNewEnabled = !userAccount.isEnable();
        DocumentReference userAccountRef = db.collection("UserAccount").document(userAccount.getDocKey());
        userAccountRef.update("IsEnable", isNewEnabled)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        loadUserAccounts();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                    }
                });
    }

}
