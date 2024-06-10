package com.example.fyp_ilikethatcoffee;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.auth.FirebaseAuth;

import java.util.List;


public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder> {

    Context context;
    FirebaseAuth firebaseAuth;
    String useraccountid;
    List<ModelUsers> list;
    public AdapterUsers(Context context, List<ModelUsers> list) {
        this.context = context;
        this.list = list;
        firebaseAuth = FirebaseAuth.getInstance();
        useraccountid = firebaseAuth.getUid();
    }




    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_user, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
        final String useraccountid = list.get(position).getUid();
        String username = list.get(position).getName();
        String usermail = list.get(position).getEmail();
        String userusername = list.get(position).getUsername();
        holder.name.setText(username);
        holder.email.setText(usermail);
        holder.username.setText(userusername);
        holder.accountid.setText(useraccountid);

        try {

        } catch (Exception e) {
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {


        TextView name, email, username, accountid;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.namep);
            email = itemView.findViewById(R.id.emailp);
            username = itemView.findViewById(R.id.usernamep);
            accountid = itemView.findViewById(R.id.useraccountidp);

        }
    }
}

