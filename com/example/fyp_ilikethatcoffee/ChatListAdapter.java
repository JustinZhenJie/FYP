package com.example.fyp_ilikethatcoffee;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class ChatListAdapter extends BaseAdapter {

    private List<Conversations> conversationList;
    private LayoutInflater inflater;
    private Context context; // Add context field

    // Define the Firestore database instance
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ChatListAdapter(Context context, List<Conversations> conversationList) {
        this.context = context;
        this.conversationList = conversationList;
        this.inflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return conversationList.size();
    }
    @Override
    public Object getItem(int position) {
        return conversationList.get(position);
    }

    @Override
    public long getItemId(int position) {
       return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_chat_list, parent, false);
            holder = new ViewHolder();
            holder.imageViewProfilePic = convertView.findViewById(R.id.imageViewProfilePic);
            holder.textViewFriendName = convertView.findViewById(R.id.textViewFriendName);
            holder.textViewLastMessageContent = convertView.findViewById(R.id.textViewLastMessageContent);
            holder.textViewLastMessageTimestamp = convertView.findViewById(R.id.textViewLastMessageTimestamp);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Conversations conversation = conversationList.get(position);

        // Fetch friend's data asynchronously based on user ID
        fetchFriendData(conversation.getUserIDs(), holder.textViewFriendName, holder.imageViewProfilePic);

        // Bind other data to views
        holder.textViewLastMessageContent.setText(conversation.getLastMessageContent());
        holder.textViewLastMessageTimestamp.setText(conversation.getLastMessageTimeStamp());

        return convertView;
    }

    private void fetchFriendData(List<String> userIds, TextView textViewFriendName, ImageView imageViewProfilePic) {
        // Assuming you have Firebase Authentication set up
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Find friend's email (user ID) that is not the current user's email
        String friendUserId = "";
        for (String userId : userIds) {
            if (!userId.equals(currentUserEmail)) { // Replace currentUserEmail with the current user's email
                friendUserId = userId;
                break;
            }
        }

        // Query UserAccount collection to get friend's name and profile picture based on their email
        db.collection("UserAccount")
                .document(friendUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve friend's data
                        String friendName = documentSnapshot.getString("UserName");
                        String friendProfilePicUrl = documentSnapshot.getString("Image");

                        // Set friend's name and profile picture
                        textViewFriendName.setText(friendName);
                        // Use any image loading library (e.g., Picasso, Glide) to load the profile picture
                        // For example, using Glide:
                        Glide.with(context)
                                .load(friendProfilePicUrl)
                                .placeholder(R.drawable.img)
                                .error(R.drawable.img)
                                .into(imageViewProfilePic);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.e(TAG, "Error fetching friend's data", e);
                });
    }

    static class ViewHolder {
        ImageView imageViewProfilePic;
        TextView textViewFriendName;
        TextView textViewLastMessageContent;
        TextView textViewLastMessageTimestamp;
    }
}

