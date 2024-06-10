package com.example.fyp_ilikethatcoffee.Menu;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp_ilikethatcoffee.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class CategoryAdapter extends FirestoreRecyclerAdapter<Category, CategoryAdapter.MyViewHolder> {

    public CategoryAdapter(@NonNull FirestoreRecyclerOptions<Category> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull MyViewHolder viewHolder, int position, @NonNull Category category) {
        viewHolder.categoryName.setText(category.getCategory());

        // Set up inner RecyclerView (rvMenuItem) for MenuAdapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                viewHolder.itemView.getContext(),
                LinearLayoutManager.VERTICAL,
                false
        );
        viewHolder.rvMenuItem.setLayoutManager(layoutManager);

        // Create and set MenuItemAdapter
        MenuAdapter menuItemAdapter = new MenuAdapter(category.getMenuItems());
        viewHolder.rvMenuItem.setAdapter(menuItemAdapter);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;
        public TextView categoryName;
        public RecyclerView rvMenuItem;

        public MyViewHolder(View v) {
            super(v);
            mCardView = v.findViewById(R.id.card_view_category);
            categoryName = v.findViewById(R.id.category_title);
            rvMenuItem = v.findViewById(R.id.rv_menu_item);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.category_item, parent, false);
        return new MyViewHolder(view);
    }
}


/*
public class CategoryAdapter extends FirestoreRecyclerAdapter<Category, CategoryAdapter.MyViewHolder> {

    public CategoryAdapter(@NonNull FirestoreRecyclerOptions<Category> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull MyViewHolder viewHolder, int position, @NonNull Category category) {
        viewHolder.categoryName.setText(category.getCategory());

        // Set up inner RecyclerView (rvMenuItem) for MenuAdapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                viewHolder.itemView.getContext(),
                LinearLayoutManager.VERTICAL,
                false
        );
        viewHolder.rvMenuItem.setLayoutManager(layoutManager);

        // Fetch menu items and set MenuAdapter
        category.getMenuItems().addOnSuccessListener(new OnSuccessListener<List<MenuItem>>() {
            @Override
            public void onSuccess(List<MenuItem> menuItems) {
                // Create MenuAdapter with fetched menu items
                MenuAdapter menuItemAdapter = new MenuAdapter(menuItems);
                // Set MenuAdapter to RecyclerView
                viewHolder.rvMenuItem.setAdapter(menuItemAdapter);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle failure to fetch menu items (e.g., log error, show message to user)
                Log.e("CategoryAdapter", "Failed to fetch menu items: " + e.getMessage());
            }
        });
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;
        public TextView categoryName;
        public RecyclerView rvMenuItem;

        public MyViewHolder(View v) {
            super(v);
            mCardView = v.findViewById(R.id.card_view_category);
            categoryName = v.findViewById(R.id.category_title);
            rvMenuItem = v.findViewById(R.id.rv_menu_item);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.category_item, parent, false);
        return new MyViewHolder(view);
    }
}

 */
