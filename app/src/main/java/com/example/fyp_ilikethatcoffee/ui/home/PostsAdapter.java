package com.example.fyp_ilikethatcoffee.ui.home;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fyp_ilikethatcoffee.ConsumerStoreProfileActivity;
import com.example.fyp_ilikethatcoffee.Dialog.ConsumerPostDialogueFragment;
import com.example.fyp_ilikethatcoffee.Posts.Post;
import com.example.fyp_ilikethatcoffee.R;
import com.example.fyp_ilikethatcoffee.Store;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private List<Post> postList;

    public PostsAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postList.get(position);

        // Existing code to handle post details
        String postCaption = post.getPostInfo();
        int maxLength = 100;
        if (postCaption.length() > maxLength) {
            holder.readMoreTextView.setVisibility(View.VISIBLE);
            holder.storePostCaption.setText(postCaption.substring(0, maxLength) + "...");
            holder.readMoreTextView.setOnClickListener(v -> {
                holder.storePostCaption.setText(postCaption);
                holder.readMoreTextView.setVisibility(View.GONE);
            });
        } else {
            holder.storePostCaption.setText(postCaption);
            holder.readMoreTextView.setVisibility(View.GONE);
        }


        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        String dateString = dateFormat.format(post.getDateCreated().toDate());
        holder.postDate.setText(dateString);

        Glide.with(holder.itemView.getContext())
                .load(post.getImage())
                .into(holder.postPicture);

        // Fetch and display store details
        FirebaseFirestore.getInstance().collection("Store")
                .document(post.getUserAccountId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Store store = documentSnapshot.toObject(Store.class);
                    if (store != null) {
                        holder.storeDisplayName.setText(store.getStoreName());
                        Glide.with(holder.itemView.getContext()).load(store.getImageUrl()).into(holder.profilePic);
                        holder.bindToStoreProfile(holder.itemView.getContext(), store); // Set the onClickListeners

                        holder.meatballMoreIcon.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Create an instance of ConsumerPostDialogueFragment
                                ConsumerPostDialogueFragment dialogFragment =
                                        ConsumerPostDialogueFragment.newInstance(store.getUserAccountId(), post.getPostInfo(), post.getImage());
                                // Show the dialog fragment
                                FragmentManager fragmentManager = ((FragmentActivity) holder.itemView.getContext()).getSupportFragmentManager();
                                dialogFragment.show(fragmentManager, "ConsumerPostDialogFragment");
                            }
                        });
                    }
                });
    }


    @Override
    public int getItemCount() {
        return postList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView storeDisplayName, storePostCaption, postDate, readMoreTextView;
        ImageView profilePic, postPicture, meatballMoreIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            storeDisplayName = itemView.findViewById(R.id.store_display_name);
            storePostCaption = itemView.findViewById(R.id.post_caption_text);
            postDate = itemView.findViewById(R.id.date_text);
            readMoreTextView = itemView.findViewById(R.id.read_more_text);
            profilePic = itemView.findViewById(R.id.store_profile_picture_p);
            postPicture = itemView.findViewById(R.id.image_post);
            meatballMoreIcon = itemView.findViewById(R.id.meatball_icon_more);
        }

        // Add a method to setup onClick listeners
        public void bindToStoreProfile(Context context, Store store) {
            View.OnClickListener listener = v -> {
                Intent intent = new Intent(context, ConsumerStoreProfileActivity.class);
                intent.putExtra("StoreName", store.getStoreName());
                intent.putExtra("StoreDesc", store.getStoreDesc());
                intent.putExtra("StoreAddress", store.getStoreAddress());
                intent.putExtra("StoreEmail", store.getUserAccountId());
                context.startActivity(intent);
            };

            profilePic.setOnClickListener(listener);
            storeDisplayName.setOnClickListener(listener);
        }
    }

}
