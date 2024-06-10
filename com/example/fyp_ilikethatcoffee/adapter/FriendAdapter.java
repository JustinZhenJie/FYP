package com.example.fyp_ilikethatcoffee.adapter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp_ilikethatcoffee.ConsumerStoreProfileActivity;
import com.example.fyp_ilikethatcoffee.ConsumerViewConsumerProfileActivity;
import com.example.fyp_ilikethatcoffee.Friend;
import com.example.fyp_ilikethatcoffee.R;
import com.example.fyp_ilikethatcoffee.Review;
import com.example.fyp_ilikethatcoffee.UserAccount;
import com.example.fyp_ilikethatcoffee.ViewProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.MyViewHolder> {
    List<Friend> friendList;
    Context context;

    FirebaseAuth mAth;
    FirebaseUser mUser;
    StorageReference mImageRef;

    ProgressDialog progressDialog;
    String documentId;

    private FirebaseFirestore db;

    public FriendAdapter(List<Friend> friendList, Context context) {
        this.friendList = friendList;
        this.context = context;

        mAth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mUser = mAth.getCurrentUser();
    }

    @NonNull
    @Override
    public FriendAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendAdapter.MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        loadProfile(holder, friendList.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Friend friend = friendList.get(position);
                String email = null;
                if (friend.getSenderId().equals(mUser.getEmail())) {
                    email = friend.getReceiverId();
                } else {
                    email = friend.getSenderId();
                }
                //load review also
                Intent intent = new Intent(context, ConsumerViewConsumerProfileActivity.class);
                intent.putExtra("user_email", email);
                context.startActivity(intent);
            }
        });
    }

    private void loadProfile(MyViewHolder holder, Friend friend) {
        String email = null;

        if (friend.getSenderId().equals(mUser.getEmail())) {
            email = friend.getReceiverId();
        } else {
            email = friend.getSenderId();
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference userAccountsRef = db.collection("UserAccount");

        // Query to filter documents where the "Email" field matches the given email
        Query query = userAccountsRef.whereEqualTo("Email", email);


        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        documentId = document.getId();
                        String name = document.getString("Name");
                        String userEmail = document.getString("Email"); // Rename to userEmail to avoid conflict
                        String password = document.getString("Password");
                        Long phoneLong = document.getLong("Phone");
                        long phone = (phoneLong != null) ? phoneLong : 0L;
                        String userType = document.getString("UserType");
                        String userName = document.getString("UserName");
                        String image = document.getString("Image");
                        String dob = document.getString("DOB");
                        String bio = document.getString("BIO");
                        holder.emailTv.setText(userEmail);
                        holder.usernameTv.setText(userName);
                        Picasso.get().load(image).placeholder(R.drawable.img).into(holder.imageProfile);

                    }
                } else {
                    Toast.makeText(context, "No Data Available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTv;
        TextView emailTv;
        CircleImageView imageProfile;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            usernameTv = itemView.findViewById(R.id.usernameTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            imageProfile = itemView.findViewById(R.id.imageProfile);
        }
    }

    public static float doubleToFloat(double value) {
        return (float) value;
    }
}
