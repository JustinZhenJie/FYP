package com.example.fyp_ilikethatcoffee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.fyp_ilikethatcoffee.databinding.ActivityAdminBinding;

public class AdminActivity extends AppCompatActivity {

    private ActivityAdminBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup the CardView for User Accounts
        CardView cardAllAccount = findViewById(R.id.cardAllAccount);
        cardAllAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the AccountActivity
                startActivity(new Intent(AdminActivity.this, AccountActivity.class));
            }
        }); // The closing bracket for cardAllAccount's OnClickListener was missing

        // Find the logout button in the admin activity's layout
        Button buttonLogout = findViewById(R.id.buttonLogout);

        // Set click listener to the logout button
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Perform logout action here specific to admin
                finish(); // Or navigate back to the admin login page
            }
        });
    }
}

