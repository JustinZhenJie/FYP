package com.example.fyp_ilikethatcoffee.Menu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp_ilikethatcoffee.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MyViewHolder> {

    private Task<List<MenuItem>> menuItemsTask;
    private List<MenuItem> menuItems = new ArrayList<>();

    public MenuAdapter(Task<List<MenuItem>> menuItemsTask) {
        this.menuItemsTask = menuItemsTask;

        // Add a listener to the task to handle the result
        menuItemsTask.addOnSuccessListener(new OnSuccessListener<List<MenuItem>>() {
            @Override
            public void onSuccess(List<MenuItem> result) {
                // Update the menuItems list when the task succeeds
                menuItems.clear();
                menuItems.addAll(result);
                notifyDataSetChanged();
            }
        });
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.menu_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int position) {
        MenuItem menuItem = menuItems.get(position);
        Log.d("MenuAdapter", "Menu item name: " + menuItem.getName());
        Log.d("MenuAdapter", "Menu item description: " + menuItem.getDescription());
        viewHolder.menuItemName.setText(menuItem.getName());
        viewHolder.menuItemDesc.setText(menuItem.getDescription());
    }

    @Override
    public int getItemCount() {
        Log.d("MenuAdapter", "getItemCount: " + menuItems.size());
        return menuItems.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView menuItemName;
        public TextView menuItemDesc;

        public MyViewHolder(View v) {
            super(v);
            menuItemName = v.findViewById(R.id.menu_item_title);
            menuItemDesc = v.findViewById(R.id.item_desc);
        }
    }
}



/*
public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MyViewHolder> {

    private final AsyncListDiffer<MenuItem> differ = new AsyncListDiffer<>(this, DIFF_CALLBACK);

    private static final DiffUtil.ItemCallback<MenuItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<MenuItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull MenuItem oldItem, @NonNull MenuItem newItem) {
            return oldItem.getCategoryId().equals(newItem.getCategoryId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull MenuItem oldItem, @NonNull MenuItem newItem) {
            return oldItem.equals(newItem);
        }
    };

    public MenuAdapter(List<MenuItem> menuItems) {
        differ.submitList(menuItems);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.menu_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int position) {
        MenuItem menuItem = differ.getCurrentList().get(position);
        viewHolder.menuItemName.setText(menuItem.getName());
        viewHolder.menuItemDesc.setText(menuItem.getDescription());
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView menuItemName;
        public TextView menuItemDesc;

        public MyViewHolder(View v) {
            super(v);
            menuItemName = v.findViewById(R.id.menu_item_title);
            menuItemDesc = v.findViewById(R.id.item_desc);
        }
    }

    public void updateMenuItems(List<MenuItem> newMenuItems) {
        differ.submitList(newMenuItems);
    }
}

 */
