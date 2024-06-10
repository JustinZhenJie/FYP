package com.example.fyp_ilikethatcoffee.Dialog;

import androidx.fragment.app.DialogFragment;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.fyp_ilikethatcoffee.ConsumerStoreProfileActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.fyp_ilikethatcoffee.R;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.firestore.FirebaseFirestore;

public class StoreOwnerPostDialogueFragment extends BottomSheetDialogFragment {
    // Keys for the data to pass

    private String storeEmail, postCaption, postImage;
    private String postId;
    private FirebaseFirestore db; // Declare Firestore instance
    LinearLayout shareLayout, editLayout, deleteLayout;

    public static StoreOwnerPostDialogueFragment newInstance(String storeEmail, String postCaption, String postImage, String postId) {
        StoreOwnerPostDialogueFragment fragment = new StoreOwnerPostDialogueFragment();
        Bundle args = new Bundle();
        args.putString("storeEmail", storeEmail);
        args.putString("postCaption", postCaption);
        args.putString("postImage", postImage);
        args.putString("postId", postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance(); // Initialize Firestore instance

        if (getArguments() != null) {
            storeEmail = getArguments().getString("storeEmail");
            postCaption = getArguments().getString("postCaption");
            postImage = getArguments().getString("postImage");
            postId = getArguments().getString("postId");


        }


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        db = FirebaseFirestore.getInstance(); // Initialize Firestore instance
        Log.d("StoreOwnerDialogueFragment", "Current postId Retrieved: " + postId);
        View view = inflater.inflate(R.layout.fragment_storeowner_post_dialogue, container, false);
        shareLayout = view.findViewById(R.id.share_post_click);
        editLayout = view.findViewById(R.id.edit_post_click);
        deleteLayout = view.findViewById(R.id.delete_post_click);
        shareLayout.setOnClickListener(v -> sharePostToChat());
        editLayout.setOnClickListener(v -> editMyPost());
        deleteLayout.setOnClickListener(v -> deleteMyPost());
        return view;

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // You can customize your dialog here if needed
        return super.onCreateDialog(savedInstanceState);
    }

    private void sharePostToChat() {
        String deepLinkUrl = "https://ilikethatcoffee.com/post?storeEmail=" + storeEmail + "&postCaption=" + postCaption + "&postImage=" + postImage;
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(deepLinkUrl))
                .setDomainUriPrefix("https://ilikethatcoffee.page.link")
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                .buildShortDynamicLink()
                .addOnSuccessListener(shortDynamicLink -> {
                    Uri shortLink = shortDynamicLink.getShortLink();

                    // Copy the link to clipboard
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Post Link", shortLink.toString());
                    clipboard.setPrimaryClip(clip);

                    // Open share dialog
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shortLink.toString());
                    startActivity(Intent.createChooser(shareIntent, "Share Post Link"));

                    Toast.makeText(getContext(), "Link copied to clipboard", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to create dynamic link", Toast.LENGTH_SHORT).show();
                });
    }

    private void editMyPost() {
        if (postId != null) {
            Intent intent = new Intent(getContext(), EditStorePostActivity.class);
            intent.putExtra("storeEmail", storeEmail);
            intent.putExtra("postId", postId);
            intent.putExtra("postCaption", postCaption);
            intent.putExtra("postImage", postImage);
            startActivity(intent);
        } else {
            // Handle the case where postId is null
            Toast.makeText(getContext(), "Post ID is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteMyPost(){if (postId != null) {
        db.collection("Post")
                .document(postId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Handle successful deletion
                    Toast.makeText(getContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show();
                    // Dismiss the dialog
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    // Handle failure to delete
                    Toast.makeText(getContext(), "Failed to delete post", Toast.LENGTH_SHORT).show();
                    Log.e("StoreOwnerPostDialogueFragment", "Error deleting post", e);
                });
    } else {
        // Handle the case where postId is null
        Toast.makeText(getContext(), "Post ID is null", Toast.LENGTH_SHORT).show();
    }
    }


}