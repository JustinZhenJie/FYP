package com.example.fyp_ilikethatcoffee;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp_ilikethatcoffee.adapter.FaqAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HelpActivity extends AppCompatActivity {

    RecyclerView recyclerview;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        db = FirebaseFirestore.getInstance();

        recyclerview = findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));

        loadData();

    }

    private void loadData() {
        db.collection("FAQ").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Faq> faqList = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Faq faq = document.toObject(Faq.class);
                    if (faq.getCategory().equals("General") || faq.getCategory().equals("Consumer")) {
                        faqList.add(faq);
                    }
                }
                // Display data in RecyclerView
                displayFaqData(faqList);
            } else {
                Toast.makeText(this, "No data available!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayFaqData(List<Faq> faqList) {
        // Initialize RecyclerView
        FaqAdapter adapter = new FaqAdapter(faqList, HelpActivity.this);
        recyclerview.setAdapter(adapter);
    }
}