package com.example.fyp_ilikethatcoffee;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class AccountActivity extends AppCompatActivity {

    CardView cardAllAccount;
    CardView cardSuspendAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        cardAllAccount=findViewById(R.id.cardAllAccount);
        cardSuspendAccount=findViewById(R.id.cardSuspendAccount);

        cardAllAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AccountActivity.this,DisplayUsersActivity.class));
            }
        });

        cardSuspendAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AccountActivity.this,SuspendAccountActivity.class));
            }
        });

    }
}