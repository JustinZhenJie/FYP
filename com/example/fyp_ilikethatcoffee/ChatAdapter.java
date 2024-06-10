package com.example.fyp_ilikethatcoffee;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

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
//            imageViewProfile = itemView.findViewById(R.id.image_profile_sent);
//            textSenderName = itemView.findViewById(R.id.text_sender_name);
            textMessageContent = itemView.findViewById(R.id.text_message_content_sent);
            textMessageTimestamp = itemView.findViewById(R.id.text_message_timestamp_sent);
        }

        void bind(Message message) {
            Log.d("SentMessageViewHolder", "Message Content: " + message.getMessageContent());
            // Bind data to views here
            // For example:
//            textSenderName.setText(message.getSenderName());
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
//            imageViewProfile = itemView.findViewById(R.id.image_profile_received);
//            textSenderName = itemView.findViewById(R.id.text_sender_name);
            textMessageContent = itemView.findViewById(R.id.text_message_content_received);
            textMessageTimestamp = itemView.findViewById(R.id.text_message_timestamp_received);
        }

        void bind(Message message) {
            // Bind data to views here
            // For example:
//            textSenderName.setText(message.getSenderName());
            textMessageContent.setText(message.getMessageContent());
            textMessageTimestamp.setText(message.getTimestamp());
        }
    }


}
