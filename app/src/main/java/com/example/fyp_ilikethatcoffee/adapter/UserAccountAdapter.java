package com.example.fyp_ilikethatcoffee.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp_ilikethatcoffee.R;
import com.example.fyp_ilikethatcoffee.UserAccount;

import java.util.List;

public class UserAccountAdapter extends RecyclerView.Adapter<UserAccountAdapter.ViewHolder> {

    private List<UserAccount> userAccounts;
    Context context;

    public UserAccountAdapter(List<UserAccount> userAccounts, Context context) {
        this.userAccounts = userAccounts;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_account, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserAccount userAccount = userAccounts.get(position);
        holder.usenameTv.setText(userAccount.getName());
        holder.phoneTv.setText(userAccount.getPhone());
        holder.emailTv.setText(userAccount.getEmail());
        // Bind other user data to corresponding views
    }

    @Override
    public int getItemCount() {
        return userAccounts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usenameTv;
        TextView phoneTv;
        TextView emailTv;
        // Define other TextViews here

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            usenameTv = itemView.findViewById(R.id.usernameTv);
            phoneTv = itemView.findViewById(R.id.phoneTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            // Initialize other TextViews here
        }
    }
}