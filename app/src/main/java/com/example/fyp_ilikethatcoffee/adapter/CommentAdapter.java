package com.example.fyp_ilikethatcoffee.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp_ilikethatcoffee.Comment;
import com.example.fyp_ilikethatcoffee.R;
import com.example.fyp_ilikethatcoffee.ViewProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder> {
    Context context;
    List<Comment> commentList;

    public CommentAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.MyViewHolder holder, int position) {
        holder.feedbackTv.setText(commentList.get(position).getCommentInfo());
        holder.userNameTv.setText(commentList.get(position).getUsername());
        // loadProfileImage(holder, reviewList.get(position));

        loadProfile(commentList.get(position).getUsername(), holder.imageProfile);

    }

    private void loadProfile(String username, CircleImageView imageProfile) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference userAccountsRef = db.collection("UserAccount");

        // Query to filter documents where the "UserName" field matches the given username
        Query query = userAccountsRef.whereEqualTo("UserName", username);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        String documentId = document.getId();
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

                        Picasso.get().load(image).placeholder(R.drawable.img).into(imageProfile);


                    }
                } else {
                    Toast.makeText(context, "No Data Available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTv, feedbackTv;
        CircleImageView imageProfile;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTv = itemView.findViewById(R.id.userNameTv);
            feedbackTv = itemView.findViewById(R.id.feedbackTv);
            imageProfile = itemView.findViewById(R.id.imageProfile);
        }
    }
}
