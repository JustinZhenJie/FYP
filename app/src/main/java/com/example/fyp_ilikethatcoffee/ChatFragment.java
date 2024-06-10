package com.example.fyp_ilikethatcoffee;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText editTextMessage;
    private Button buttonSend;
    private ChatAdapter adapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference messageRef;
    private String currentUserId; // Added for storing current user ID
    private String conversationId;
    private String friendEmail;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = root.findViewById(R.id.recycler_chat);
        editTextMessage = root.findViewById(R.id.edit_text_message);
        buttonSend = root.findViewById(R.id.button_send);

        // Retrieve current user ID and conversation ID from arguments
        Bundle args = getArguments();
        currentUserId = args.getString("currentUserId");
        conversationId = args.getString("conversationId");

        // If conversationId is null, it means it's from AddChatFragment
        if (conversationId == null) {
            // Get friend's email from arguments
            friendEmail = args.getString("friendEmail");
            // Generate conversation ID based on current user ID and friend's email
            conversationId = generateConversationId(currentUserId, friendEmail);
        }

        Log.d(TAG, "Current User ID: " + currentUserId);
        Log.d(TAG, "Conversation ID: " + conversationId);
        Log.d(TAG, "Friend ID: " + friendEmail);

        // Set up RecyclerView with LinearLayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Construct the reference to the messages subcollection under the specific conversation
        messageRef = db.collection("Conversations").document(conversationId).collection("Message");

        // Set up FirestoreRecyclerOptions with query to fetch messages, ordered by timestamp
        FirestoreRecyclerOptions<Message> options = new FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(messageRef.orderBy("timestamp", Query.Direction.ASCENDING), Message.class)
                .build();

        // Initialize the adapter with FirestoreRecyclerOptions and currentUserId
        adapter = new ChatAdapter(options, currentUserId);
        recyclerView.setAdapter(adapter);

        // Scroll to the last item when the adapter's data set is changed
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                scrollToBottom();
            }
        });

        // Set click listener for the send button
        buttonSend.setOnClickListener(v -> sendMessage());

        return root;
    }

    // Method to generate conversation ID based on current user ID and friend's email
    private String generateConversationId(String currentUserId, String friendEmail) {
        if (currentUserId != null && friendEmail != null) {
            // Assuming conversation ID is a combination of current user ID and friend's email
            return currentUserId + "_" + friendEmail;
        } else {
            return null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inflate the custom title layout
        View titleView = LayoutInflater.from(requireContext()).inflate(R.layout.title_chat, null);

        // Find views in the custom title layout
        ImageView profileImageView = titleView.findViewById(R.id.image_profile);
        TextView receiverNameTextView = titleView.findViewById(R.id.text_receiver_name);

        // Retrieve receiver's name and profile picture URL from arguments
        Bundle args = getArguments();
        if (args != null) {
            String friendName = args.getString("friendName");
            String profilePicUrl = args.getString("profilePicUrl");

            // Set receiver's name
            receiverNameTextView.setText(friendName);

            // Load profile picture using Glide
            Glide.with(requireContext())
                    .load(profilePicUrl)
                    .placeholder(R.drawable.img) // Placeholder image while loading
                    .error(R.drawable.img) // Image to display in case of error
                    .into(profileImageView); // ImageView to load the image into
        }

        // Set the custom view for the ActionBar
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
            actionBar.setCustomView(titleView);
        }
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the bottom navigation bar when ChatFragment is created
        ((MainActivity) requireActivity()).hideBottomNavBar();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Show the bottom navigation bar when ChatFragment is destroyed
        ((MainActivity) requireActivity()).showBottomNavBar();

        // Reset the action bar to its default state
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_USE_LOGO);
        }
    }



    @Override
    public void onStart() {
        super.onStart();
        // Start listening for changes in the Firestore database
        adapter.startListening();

    }

    private void scrollToBottom() {
        // Check if the adapter is not null and there are items in the adapter
        if (adapter != null && adapter.getItemCount() > 0) {
            // Get the position of the last item
            int lastItemPosition = adapter.getItemCount() - 1;

            // Scroll to the last item position
            recyclerView.scrollToPosition(lastItemPosition);
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        // Stop listening for changes in the Firestore database
        adapter.stopListening();
    }


    private void sendMessage() {
        // Retrieve the message content from the EditText
        String messageContent = editTextMessage.getText().toString().trim();

        // Check if the message content is not empty and currentUserId is not null
        if (!messageContent.isEmpty() && currentUserId != null) {
            // Get the current timestamp
            String timestamp = getCurrentTimestamp();

            // Retrieve sender's name from Firestore based on currentUserId
            db.collection("UserAccount").document(currentUserId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Document exists, retrieve sender's name
                            String senderName = documentSnapshot.getString("UserName");

                            // Create a new Message object with the current user's ID, message content, sender's name, and timestamp
                            Message message = new Message(currentUserId, senderName, messageContent, "text", timestamp);

                            // Add the message to the Message subcollection under Conversations
                            DocumentReference conversationDocRef = db.collection("Conversations").document(conversationId);
                            conversationDocRef.get().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        // Document exists, update existing document fields
                                        conversationDocRef.update("lastMessageContent", messageContent, "lastMessageTimeStamp", timestamp);
                                    } else {
                                        // Document does not exist, create a new document with fields
                                        Map<String, Object> data = new HashMap<>();
                                        data.put("UserIDs", Arrays.asList(currentUserId, friendEmail));
                                        data.put("lastMessageContent", messageContent);
                                        data.put("lastMessageTimeStamp", timestamp);
                                        db.collection("Conversations").document(conversationId).set(data);
                                    }
                                    // Add the message to the Message subcollection under Conversations
                                    db.collection("Conversations").document(conversationId).collection("Message")
                                            .add(message)
                                            .addOnSuccessListener(documentReference -> {
                                                // Message sent successfully
                                                Log.d(TAG, "Message sent successfully");

                                                // Clear the EditText after sending the message
                                                editTextMessage.getText().clear();
                                            })
                                            .addOnFailureListener(e -> {
                                                // Failed to send message
                                                Log.e(TAG, "Error sending message", e);
                                                // You can show an error message to the user if needed
                                            });
                                } else {
                                    Log.d(TAG, "get failed with ", task.getException());
                                }
                            });
                        } else {
                            // Document does not exist for the currentUserId
                            Log.d(TAG, "User document does not exist for currentUserId: " + currentUserId);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Failed to retrieve sender's name
                        Log.e(TAG, "Error retrieving sender's name", e);
                    });
        } else {
            // Log a message or handle the situation if currentUserId is null or message content is empty
            Log.d(TAG, "Message content is empty or currentUserId is null");
        }
    }

    // Method to get current timestamp
    private String getCurrentTimestamp() {
        // Implement your logic to get the current timestamp
        // For example, you can use SimpleDateFormat to format the current date and time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void updateConversationInfo(String conversationId, String messageContent, String timestamp) {
        // Construct the reference to the Conversations document
        DocumentReference conversationDocRef = db.collection("Conversations").document(conversationId);

        // Update the lastMessageContent and lastMessageTimeStamp fields
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessageContent", messageContent);
        updates.put("lastMessageTimeStamp", timestamp);

        // Update the document in Firestore
        conversationDocRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Document updated successfully
                    Log.d(TAG, "Conversation document updated with latest message info");
                })
                .addOnFailureListener(e -> {
                    // Failed to update document
                    Log.e(TAG, "Error updating conversation document", e);
                });
    }


}
