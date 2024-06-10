package com.example.fyp_ilikethatcoffee;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends Fragment {

    private ListView listView;
    private ChatListAdapter adapter;
    private List<Conversations> conversationList;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chat_list, container, false);
        listView = rootView.findViewById(R.id.list_view_chat);
        conversationList = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        adapter = new ChatListAdapter(requireContext(), conversationList);
        listView.setAdapter(adapter);
        setHasOptionsMenu(true); // Indicate that the fragment has an options menu
        fetchConversations();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View rootView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);

        // Obtain the NavController associated with the FragmentActivity
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            // Retrieve the clicked conversation
            Conversations conversation = conversationList.get(position);

            // Create a bundle to pass data to the ChatFragment
            Bundle args = new Bundle();
            args.putString("currentUserId", FirebaseAuth.getInstance().getCurrentUser().getEmail());
            args.putString("conversationId", conversation.getId());

            // Find friend's email (user ID) that is not the current user's email
            String friendUserId = "";
            for (String userId : conversation.getUserIDs()) {
                if (!userId.equals(args.getString("currentUserId"))) {
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

                            // Add friend's data to the bundle
                            args.putString("friendName", friendName);
                            args.putString("profilePicUrl", friendProfilePicUrl);

                            // Navigate to ChatFragment using NavController
                            navController.navigate(R.id.navigation_chat, args);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                        Log.e(TAG, "Error fetching friend's data", e);
                    });
        });

    }

    private void fetchConversations() {
        String currentUserEmail = mAuth.getCurrentUser().getEmail();

        // Fetch conversations where the current user is one of the participants
        db.collection("Conversations")
                .whereArrayContains("UserIDs", currentUserEmail)
                .orderBy("lastMessageTimeStamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Conversations> conversations = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        // Assuming each document represents a conversation
                        Conversations conversation = document.toObject(Conversations.class);
                        // Store the document ID along with the conversation object
                        conversation.setId(document.getId());
                        conversations.add(conversation);
                    }
                    // Add fetched conversations to the list
                    conversationList.addAll(conversations);
                    // Notify the adapter about the dataset change
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.e(TAG, "Error fetching conversations: ", e);
                });
    }
}
