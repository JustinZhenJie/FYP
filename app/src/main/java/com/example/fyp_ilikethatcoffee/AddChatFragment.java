package com.example.fyp_ilikethatcoffee;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AddChatFragment extends Fragment implements AddChatAdapter.OnFriendClickListener {


    private RecyclerView recyclerView;
    private List<UserAccount> friendList;
    private AddChatAdapter adapter;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_chat, container, false);

        recyclerView = rootView.findViewById(R.id.recycler_friends);
        friendList = new ArrayList<>();
        adapter = new AddChatAdapter(requireContext(), friendList, this); // Pass fragment as listener
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Fetch list of friends from Firestore
        fetchFriends();

        return rootView;
    }

    private void fetchFriends() {
        // Get the current user's email
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Query the Firestore collection "Friend" to get friends where the current user is the sender and the status is "accepted"
        db.collection("Friend")
                .whereEqualTo("Status", "accepted")
                .whereEqualTo("senderId", currentUserEmail)
                .get()
                .addOnSuccessListener(senderQuerySnapshot -> {
                    // Process results where the current user is the sender
                    processFriendQueryResults(senderQuerySnapshot, currentUserEmail);
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    Toast.makeText(requireContext(), "Failed to fetch friends: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void processFriendQueryResults(QuerySnapshot senderQuerySnapshot, String currentUserEmail) {
        // Create a list to store friends with ongoing friendships
        List<String> friendsWithFriendships = new ArrayList<>();

        // Add friends with ongoing friendships to the list
        for (DocumentSnapshot document : senderQuerySnapshot) {
            // Get the receiver's email from the document
            String receiverEmail = document.getString("receiverId");
            // Add the receiver to the list of friends with ongoing friendships
            friendsWithFriendships.add(receiverEmail);
        }

        // Query the Firestore collection "Friend" to get friends where the current user is the receiver and the status is "accepted"
        db.collection("Friend")
                .whereEqualTo("Status", "accepted")
                .whereEqualTo("receiverId", currentUserEmail)
                .get()
                .addOnSuccessListener(receiverQuerySnapshot -> {
                    // Process results where the current user is the receiver
                    processReceiverFriendQueryResults(receiverQuerySnapshot, currentUserEmail, friendsWithFriendships);
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    Toast.makeText(requireContext(), "Failed to fetch friends: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void processReceiverFriendQueryResults(QuerySnapshot receiverQuerySnapshot, String currentUserEmail, List<String> friendsWithFriendships) {
        // Add friends with ongoing friendships to the list
        for (DocumentSnapshot document : receiverQuerySnapshot) {
            // Get the sender's email from the document
            String senderEmail = document.getString("senderId");
            // Add the sender to the list of friends with ongoing friendships
            friendsWithFriendships.add(senderEmail);
        }

        // Query the Firestore collection "Conversations" to get conversations where the current user is a participant
        db.collection("Conversations")
                .whereArrayContains("UserIDs", currentUserEmail)
                .get()
                .addOnSuccessListener(conversationsQuerySnapshot -> {
                    // Create a list to store friends with ongoing conversations
                    List<String> friendsWithConversations = new ArrayList<>();

                    // Add friends with ongoing conversations to the list
                    for (DocumentSnapshot document : conversationsQuerySnapshot) {
                        // Get the participant emails from the conversation
                        List<String> participants = (List<String>) document.get("UserIDs");
                        // Exclude the current user's email
                        participants.remove(currentUserEmail);
                        // Add the other participant to the list of friends with conversations
                        friendsWithConversations.addAll(participants);
                    }

                    // Query the Firestore collection "UserAccounts" to get details of all friends
                    db.collection("UserAccount")
                            .get()
                            .addOnSuccessListener(userQuerySnapshot -> {
                                for (DocumentSnapshot userDocument : userQuerySnapshot) {
                                    String friendEmail = userDocument.getString("Email");
                                    String friendName = userDocument.getString("UserName");
                                    String friendImage = userDocument.getString("Image"); // Fetch the Image field

                                    if (friendsWithFriendships.contains(friendEmail)
                                            && !isFriendAlreadyAdded(friendEmail)
                                            && !friendsWithConversations.contains(friendEmail)) {
                                        // Add the friend with all details to the friendList
                                        friendList.add(new UserAccount(friendEmail, friendName, friendImage));
                                        Log.d("AddChatFragment", "Added friend: " + friendEmail + " - " + friendName + " - " + friendImage);
                                    }
                                }
                                // Notify the adapter about the data change after processing the query
                                adapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                // Handle errors
                                Toast.makeText(requireContext(), "Failed to fetch friends: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    Toast.makeText(requireContext(), "Failed to fetch friends: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isFriendAlreadyAdded(String friendEmail) {
        // Check if the friend's email is already in the friendList
        for (UserAccount friend : friendList) {
            if (friend.getEmail().equals(friendEmail)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onFriendClick(UserAccount friend) {
        // Obtain the NavController associated with the FragmentActivity
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

        // Get the email, name, and image URL of the clicked friend
        String friendEmail = friend.getEmail();
        String friendName = friend.getUserName();
        String friendImage = friend.getImage();

        // Navigate to ChatFragment using NavController
        Bundle args = new Bundle();
        args.putString("currentUserId", FirebaseAuth.getInstance().getCurrentUser().getEmail());
        args.putString("friendEmail", friendEmail); // Pass the friend's email as an argument
        args.putString("friendName", friendName); // Pass the friend's name as an argument
        args.putString("profilePicUrl", friendImage); // Pass the friend's image URL as an argument

        // Replace current fragment with ChatFragment
        navController.navigate(R.id.navigation_chat, args);
    }




}
