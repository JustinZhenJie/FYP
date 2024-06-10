package com.example.fyp_ilikethatcoffee.Posts;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp_ilikethatcoffee.Dialog.ConsumerPostDialogueFragment;
import com.example.fyp_ilikethatcoffee.Dialog.StoreOwnerPostDialogueFragment;
import com.example.fyp_ilikethatcoffee.R;
import com.bumptech.glide.Glide;
import com.example.fyp_ilikethatcoffee.Store;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PostAdapter extends FirestoreRecyclerAdapter<Post, PostAdapter.MyViewHolder> {
    private String storeEmail, currentUserEmail;

    public PostAdapter(@NonNull FirestoreRecyclerOptions<Post> options, String currentUserEmail) {
        super(options);
        this.currentUserEmail = currentUserEmail;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;
        public TextView storeDisplayName;
        public TextView storePostCaption;
        public TextView postDate;
        public ImageView profilePic;
        public ImageView postPicture;
        public ImageView meatballMoreIcon;
        public TextView readMoreTextView;

        public MyViewHolder(View v) {
            super(v);
            mCardView = v.findViewById(R.id.card_view_store);
            storeDisplayName = v.findViewById(R.id.store_display_name);
            storePostCaption = v.findViewById(R.id.post_caption_text);
            postDate = v.findViewById(R.id.date_text);
            postPicture = v.findViewById(R.id.image_post);
            profilePic = v.findViewById(R.id.store_profile_picture_p);
            readMoreTextView = v.findViewById(R.id.read_more_text);
            meatballMoreIcon = v.findViewById(R.id.meatball_icon_more);
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull MyViewHolder viewHolder, int position, @NonNull Post post) {
        // Set the post caption and handle "read more" functionality
        viewHolder.storePostCaption.setText(post.getPostInfo());
        String postCaption = post.getPostInfo();
        int maxLength = 100;
        if (postCaption.length() > maxLength) {
            viewHolder.readMoreTextView.setVisibility(View.VISIBLE);
            viewHolder.storePostCaption.setText(postCaption.substring(0, maxLength) + "...");
            viewHolder.readMoreTextView.setOnClickListener(v -> {
                viewHolder.storePostCaption.setText(postCaption);
                viewHolder.readMoreTextView.setVisibility(View.GONE);
            });
        } else {
            viewHolder.storePostCaption.setText(postCaption);
            viewHolder.readMoreTextView.setVisibility(View.GONE);
        }

        // Set the post date
        Timestamp timestamp = post.getDateCreated();
        Date date = timestamp.toDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm:ss a 'UTC'Z");
        String dateString = dateFormat.format(date);
        viewHolder.postDate.setText(dateString);

        // Load post picture using Glide
        Glide.with(viewHolder.postPicture.getContext()).load(post.getImage()).into(viewHolder.postPicture);

        // Get the document ID for the current post
        String documentId = getSnapshots().getSnapshot(position).getId();
        Log.d("PostAdapter", "Current PostId: " + documentId);

        // Fetch Store information
        FirebaseFirestore.getInstance().collection("Store")
                .document(post.getUserAccountId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Store store = documentSnapshot.toObject(Store.class);
                        if (store != null) {
                            storeEmail = store.getUserAccountId();
                            Log.d("PostAdapter", "storeEmail: " + storeEmail);
                            viewHolder.storeDisplayName.setText(store.getStoreName());
                            // Load store profile picture using Glide
                            Glide.with(viewHolder.profilePic.getContext()).load(store.getImageUrl()).into(viewHolder.profilePic);

                            // Fetch corresponding UserAccount document
                            FirebaseFirestore.getInstance().collection("UserAccount")
                                    .document(currentUserEmail)
                                    .get()
                                    .addOnSuccessListener(userAccountDocument -> {
                                        if (userAccountDocument.exists()) {
                                            String userType = userAccountDocument.getString("UserType");
                                            if (userType != null) {
                                                if (userType.equals("StoreOwner")) {
                                                    viewHolder.meatballMoreIcon.setOnClickListener(v -> {
                                                        StoreOwnerPostDialogueFragment dialogFragment =
                                                                StoreOwnerPostDialogueFragment.newInstance(store.getUserAccountId(), post.getPostInfo(), post.getImage(), documentId);
                                                        FragmentManager fragmentManager = ((FragmentActivity) viewHolder.itemView.getContext()).getSupportFragmentManager();
                                                        dialogFragment.show(fragmentManager, "StorePostDialogue");
                                                    });
                                                } else if (userType.equals("Consumer")) {
                                                    viewHolder.meatballMoreIcon.setOnClickListener(v -> {
                                                        ConsumerPostDialogueFragment dialogFragment = new ConsumerPostDialogueFragment();
                                                        FragmentManager fragmentManager = ((FragmentActivity) viewHolder.itemView.getContext()).getSupportFragmentManager();
                                                        dialogFragment.show(fragmentManager, "ConsumerPostDialogFragment");
                                                    });
                                                }
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("PostAdapter", "Failed to fetch UserAccount document", e);
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("PostAdapter", "Failed to fetch store information", e);
                });
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.post_item, parent, false);
        return new MyViewHolder(view);
    }
}

/*
public class ChatAdapter extends FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private String currentUserId; // Added for storing current user ID

    public ChatAdapter(@NonNull FirestoreRecyclerOptions<Message> options, String currentUserId) {
        super(options);
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        // Determine the view type based on the sender of the message
        Message message = getItem(position);
        if (message != null && message.getSenderId() != null && message.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        RecyclerView.ViewHolder viewHolder;

        // Inflate the appropriate layout based on the view type
        if (viewType == VIEW_TYPE_SENT) {
            view = inflater.inflate(R.layout.item_chat_sent, parent, false);
            viewHolder = new SentMessageViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.item_chat_received, parent, false);
            viewHolder = new ReceivedMessageViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull Message message) {
        // Bind the data to the views based on the view type
        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    // Define ViewHolder classes for sent and received messages
    private static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProfile;
        TextView textSenderName;
        TextView textMessageContent;
        TextView textMessageTimestamp;

        SentMessageViewHolder(View itemView) {
            super(itemView);
            imageViewProfile = itemView.findViewById(R.id.image_profile_sent);
            textSenderName = itemView.findViewById(R.id.text_sender_name);
            textMessageContent = itemView.findViewById(R.id.text_message_content_sent);
            textMessageTimestamp = itemView.findViewById(R.id.text_message_timestamp_sent);
        }

        void bind(Message message) {
            Log.d("SentMessageViewHolder", "Message Content: " + message.getMessageContent());
            // Bind data to views here
            // For example:
            textSenderName.setText(message.getSenderName());
            textMessageContent.setText(message.getMessageContent());
            textMessageTimestamp.setText(message.getTimestamp());
            // You can also bind profile picture or other data if needed
        }

    }


    private static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProfile;
        TextView textSenderName;
        TextView textMessageContent;
        TextView textMessageTimestamp;

        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            imageViewProfile = itemView.findViewById(R.id.image_profile_received);
            textSenderName = itemView.findViewById(R.id.text_sender_name);
            textMessageContent = itemView.findViewById(R.id.text_message_content_received);
            textMessageTimestamp = itemView.findViewById(R.id.text_message_timestamp_received);
        }

        void bind(Message message) {
            // Bind data to views here
            // For example:
            textSenderName.setText(message.getSenderName());
            textMessageContent.setText(message.getMessageContent());
            textMessageTimestamp.setText(message.getTimestamp());
        }
    }


}

 */

/*
public class PostAdapter extends FirestoreRecyclerAdapter<Post, PostAdapter.MyViewHolder> {


    public PostAdapter(@NonNull FirestoreRecyclerOptions<Post> options) {
        super(options);
    }

    public static class StoreViewHolder extends RecyclerView.ViewHolder {
        TextView storeNameTextView;


        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            storeNameTextView = itemView.findViewById(R.id.store_display_name); // Replace with the actual ID
        }
    }


    @Override
    protected void onBindViewHolder(@NonNull MyViewHolder viewHolder, int i, @NonNull Post post) {
        viewHolder.storeDisplayName.setText(post.getUserAccountId());
        viewHolder.storePostCaption.setText(post.getPostInfo());

        // Get the DateCreated timestamp from the Post object
        Timestamp timestamp = post.getDateCreated();
        // Convert the timestamp to a Date object
        Date date = timestamp.toDate();
        // Format the date as a string
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm:ss a 'UTC'Z");
        String dateString = dateFormat.format(date);
        // Set the formatted date string to the postDate TextView
        viewHolder.postDate.setText(dateString);

        //Glide.with(viewHolder.profilePic.getContext()).load(post.getProfileUrl()).into(viewHolder.profilePic);
        Glide.with(viewHolder.postPicture.getContext()).load(post.getImage()).into(viewHolder.postPicture);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;
        public TextView storeDisplayName;
        public TextView storePostCaption;
        public TextView postDate;
        //public ImageView profilePic;
        public ImageView postPicture;

        public MyViewHolder(View v) {
            super(v);
            mCardView = v.findViewById(R.id.card_view_store);
            storeDisplayName = v.findViewById(R.id.store_display_name);
            storePostCaption = v.findViewById(R.id.post_caption_text);
            postDate = v.findViewById(R.id.date_text);
            //profilePic = (ImageView) v.findViewById(R.id.store_profile_picture);
            postPicture = v.findViewById(R.id.image_post);


        }
    }



    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.post_item, parent, false);
        return new MyViewHolder(view);

    }





}

 */