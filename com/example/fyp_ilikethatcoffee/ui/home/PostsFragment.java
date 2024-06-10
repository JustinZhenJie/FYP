package com.example.fyp_ilikethatcoffee.ui.home;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.fyp_ilikethatcoffee.Posts.Post;
import com.example.fyp_ilikethatcoffee.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PostsFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostsAdapter postsAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference postRef;
    private List<Post> postsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_posts, container, false);
        recyclerView = view.findViewById(R.id.posts_RV); // Ensure you have a RecyclerView in your fragment_post.xml
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Set up FirestoreRecyclerOptions
        FirestoreRecyclerOptions<Post> options =
                new FirestoreRecyclerOptions.Builder<Post>()
                        .setQuery(postRef, Post.class)
                        .build();

        // Initialize adapter
        postsAdapter = new PostsAdapter(postsList);
        recyclerView.setAdapter(postsAdapter);

        fetchCurrentUserEmail();
        return view;
    }

    private void fetchCurrentUserEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userEmail = user.getEmail();
            fetchFavoriteStores(userEmail);
        } else {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchFavoriteStores(String userEmail) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("UserAccount")
                .document(userEmail)
                .collection("Favourite")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> favoriteStores = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            favoriteStores.add(document.getString("StoreName"));
                        }
                        Log.d("PostsFragment", "Favorite Stores: " + favoriteStores);
                        if (favoriteStores.isEmpty()) {
                            Toast.makeText(getContext(), "No favorites added", Toast.LENGTH_SHORT).show();
                            return; // Avoid querying with an empty list
                        }
                        fetchPosts(favoriteStores);
                    } else {
                        Toast.makeText(getContext(), "Failed to load favorites", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void fetchPosts(List<String> storeNames) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (storeNames.isEmpty()) {
            Toast.makeText(getContext(), "No favorite stores found", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("Post")
                .whereIn("StoreName", storeNames)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        postsList.clear();  // Clear existing data
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Post post = document.toObject(Post.class);
                            postsList.add(post);
                        }
                        postsAdapter.notifyDataSetChanged();  // Notify adapter of data change
                    } else {
                        Toast.makeText(getContext(), "Failed to load posts", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
