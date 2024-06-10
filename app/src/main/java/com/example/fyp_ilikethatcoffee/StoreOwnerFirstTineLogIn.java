package com.example.fyp_ilikethatcoffee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class StoreOwnerFirstTineLogIn extends AppCompatActivity {

    private static final int RC_SIGN_IN = 201;
    private FirebaseAuth auth;
    private FirebaseFirestore db; // Declare Firestore instance

    private FirebaseUser user;
    private String username;
    private TextView usernameText;
    private Button create_profile_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_store_owner_first_tine_log_in);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore instance

        usernameText = findViewById(R.id.display_username);
        create_profile_button = findViewById(R.id.create_profile_button);

        if (getIntent().hasExtra("USERNAME")) {
            username = getIntent().getStringExtra("USERNAME");
        }

        user = auth.getCurrentUser();
        
        if (user == null){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        } else{
            usernameText.setText(username);
        }


        create_profile_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createProfile();
                Intent intent = new Intent (getApplicationContext(), StoreOwnerUpdateProfileActivity.class);
                startActivity(intent);
                finish();
            }

        });



    }

    private void createProfile(){
        String usern;
        usern = user.getEmail();
        Map<String, Object> Store = new HashMap<>();
        Store.put("StoreAddress", "");
        Store.put("StoreDesc", "");
        Store.put("StoreName", "");
        Store.put("UserAccountId", usern);

        db.collection("Store").document(usern)
                .set(Store)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }
}

