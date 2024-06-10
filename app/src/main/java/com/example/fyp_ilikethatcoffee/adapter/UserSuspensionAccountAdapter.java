package com.example.fyp_ilikethatcoffee.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp_ilikethatcoffee.R;
import com.example.fyp_ilikethatcoffee.UpdateUserAccount;
import com.example.fyp_ilikethatcoffee.UserAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserSuspensionAccountAdapter extends RecyclerView.Adapter<UserSuspensionAccountAdapter.ViewHolder> {

    List<UserAccount> userAccountsList;
    Context context;

    UpdateUserAccount updateUserAccount;


    public UserSuspensionAccountAdapter(List<UserAccount> userAccounts, Context context) {
        this.userAccountsList = userAccounts;
        this.context = context;
        updateUserAccount= (UpdateUserAccount) context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_account_suspend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        UserAccount userAccount = userAccountsList.get(position);
        holder.usenameTv.setText(userAccount.getName());
        holder.phoneTv.setText(userAccount.getPhone());
        holder.emailTv.setText(userAccount.getEmail());

        UpdateButton(holder.suspendTv, userAccount);

        holder.suspendTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserAccount.updateUserAccount(userAccount);
            }
        });
    }





    private void UpdateButton(Button suspendBtn, UserAccount userAccount) {
        suspendBtn.setText(userAccount.isEnable() ? "Suspend User" : "Enable User"); // Update button text
        suspendBtn.setBackgroundColor(context.getResources().getColor(userAccount.isEnable() ? R.color.lavender : R.color.red)); // Update button color
    }


    @Override
    public int getItemCount() {
        return userAccountsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usenameTv;
        TextView phoneTv;
        TextView emailTv;
        Button suspendTv;
        // Define other TextViews here

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            usenameTv = itemView.findViewById(R.id.usernameTv);
            phoneTv = itemView.findViewById(R.id.phoneTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            suspendTv = itemView.findViewById(R.id.suspendTv);
            // Initialize other TextViews here
        }
    }
}