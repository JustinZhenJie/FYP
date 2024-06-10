package com.example.fyp_ilikethatcoffee;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class StoreOwnerHomeActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CREATE_POST = 1;
    private BottomNavigationView bottomNavigationView;
    private FrameLayout frameLayout;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CREATE_POST) {
            if (resultCode == RESULT_OK && data != null) {
                int selectedItemId = data.getIntExtra("selectedItemId", R.id.navigation_home);
                bottomNavigationView.setSelectedItemId(selectedItemId);
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_store_owner_profile);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                int itemId = menuItem.getItemId();

                if (itemId == R.id.navigation_post){
                    Intent intent = new Intent(StoreOwnerHomeActivity.this, StoreCreatePostActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_CREATE_POST);
                }

                if (itemId == R.id.navigation_profile){
                    startActivity(new Intent(StoreOwnerHomeActivity.this, StoreOwnerProfileActivity.class));
                }

                return false;
            }
        });
    }
}