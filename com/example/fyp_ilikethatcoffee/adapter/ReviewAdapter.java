package com.example.fyp_ilikethatcoffee.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp_ilikethatcoffee.Comment;
import com.example.fyp_ilikethatcoffee.ConsumerStoreProfileActivity;
import com.example.fyp_ilikethatcoffee.R;
import com.example.fyp_ilikethatcoffee.Review;
import com.example.fyp_ilikethatcoffee.ViewStoreProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.MyViewHolder> {
    List<Review> reviewList;
    Context context;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference storeRef = db.collection("Store");

    public ReviewAdapter(List<Review> reviewList, Context context) {
        this.reviewList = reviewList;
        this.context = context;
    }


    @NonNull
    @Override
    public ReviewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapter.MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.ratingBar.setRating(doubleToFloat(reviewList.get(position).getRating()));
        holder.reviewDesc.setText(reviewList.get(position).getReviewDesc());
        holder.nameTv.setText(reviewList.get(position).getUserName());
        holder.storenameTv.setText(reviewList.get(position).getStoreName());

        if (reviewList.get(position).getImage() != null && !reviewList.get(position).getImage().isEmpty()) {
            Picasso.get().load(reviewList.get(position).getImage()).into(holder.productImageIv);
            holder.productImageIv.setVisibility(View.VISIBLE);
        } else {
            holder.productImageIv.setVisibility(View.GONE);
        }



        loadProfileImage(holder, reviewList.get(position));

        if (reviewList.get(position).getComments() != null) {
            if (!reviewList.get(position).getComments().isEmpty()) {
                holder.commentsRv.setVisibility(View.VISIBLE);
                List<Comment> comments = reviewList.get(position).getComments();
                holder.commentsRv.setAdapter(new CommentAdapter(context, comments));
            } else {
                holder.commentsRv.setVisibility(View.GONE);
            }
        }
    }

    private void loadProfileImage(MyViewHolder holder, Review review) {
        Query query = storeRef.whereEqualTo("StoreName", review.getStoreName());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        final String StoreAddress = document.getString("StoreAddress");
                        final String StoreDesc = document.getString("StoreDesc");
                        final String StoreName = document.getString("StoreName");
                        final String UserAccountId = document.getString("UserAccountId");


                        //load store profile information of a person
                        CollectionReference userAccountsRef = db.collection("UserAccount");

                        db.collection("UserAccount")
                                .document(UserAccountId) // Assuming 'emailD' is the document ID
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {
                                                String bio = document.getString("BIO");
                                                String email = document.getString("Email");
                                                String image = document.getString("Image");
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
                                                        context.startActivity(intent);
                                                    }
                                                });

                                            } else {
                                                Toast.makeText(context, "Data Missed", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Log.d("Firestore", "get failed with ", task.getException());
                                        }
                                    }
                                });
                    }
                }
            }
        });


        db.collection("UserAccount")
                .document(review.getUserAccountId()) // Assuming 'emailD' is the document ID
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String bio = document.getString("BIO");
                                String email = document.getString("Email");
                                String image = document.getString("Image");


                                if (image != null && !image.isEmpty()) {
                                    Picasso.get().load(image).into(holder.imageProfile);
                                }
                            } else {
                                Log.d("Firestore", "No such document");
                            }
                        } else {
                            Log.d("Firestore", "get failed with ", task.getException());
                        }
                    }
                });
    }


    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        RatingBar ratingBar;
        TextView reviewDesc, nameTv, storenameTv;
        CircleImageView imageProfile;
        ImageView productImageIv;
        RecyclerView commentsRv;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            storenameTv = itemView.findViewById(R.id.storenameTv);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            reviewDesc = itemView.findViewById(R.id.reviewDesc);
            imageProfile = itemView.findViewById(R.id.imageProfile);
            nameTv = itemView.findViewById(R.id.nameTv);
            productImageIv = itemView.findViewById(R.id.productImageIv);
            commentsRv = itemView.findViewById(R.id.commentsRv);
            commentsRv.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        }
    }

    public static float doubleToFloat(double value) {
        return (float) value;
    }
}
