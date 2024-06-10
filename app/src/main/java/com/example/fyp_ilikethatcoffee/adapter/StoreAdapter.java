package com.example.fyp_ilikethatcoffee.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp_ilikethatcoffee.ConsumerStoreProfileActivity;
import com.example.fyp_ilikethatcoffee.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.MyViewHolder> {
    List<String> list;
    Context context;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference storeRef = db.collection("Store");

    public StoreAdapter(List<String> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.store_layot, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.storeNameTv.setText(list.get(position));

        Query query = storeRef.whereEqualTo("StoreName", list.get(position));
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        final String StoreAddress = document.getString("StoreAddress");
                        final String StoreDesc = document.getString("StoreDesc");
                        final String StoreName = document.getString("StoreName");
                        final String UserAccountId = document.getString("UserAccountId");
                        final String ImageUrl = document.getString("ImageUrl");

                        CollectionReference userAccountsRef = db.collection("UserAccount");
                        Query query = userAccountsRef.whereEqualTo("Email", UserAccountId);

                        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (DocumentSnapshot document : task.getResult()) {
                                        final String userName = document.getString("UserName");

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(context, ConsumerStoreProfileActivity.class);
                                                intent.putExtra("StoreName", StoreName);
                                                intent.putExtra("StoreDesc", StoreDesc);
                                                intent.putExtra("StoreAddress", StoreAddress);
                                                intent.putExtra("StoreEmail", UserAccountId);
                                                intent.putExtra("USERNAME", userName);
                                                intent.putExtra("ImageUrl", ImageUrl);
                                                context.startActivity(intent);
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView storeNameTv;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            storeNameTv = itemView.findViewById(R.id.storeNameTv);

        }
    }

}
