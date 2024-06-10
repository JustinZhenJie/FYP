package com.example.fyp_ilikethatcoffee.Dialog;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.fyp_ilikethatcoffee.R;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;

public class ConsumerPostDialogueFragment extends BottomSheetDialogFragment {

    // Keys for the data to pass
    private String storeEmail, postCaption, postImage;

    public static ConsumerPostDialogueFragment newInstance(String storeEmail, String postCaption, String postImage) {
        ConsumerPostDialogueFragment fragment = new ConsumerPostDialogueFragment();
        Bundle args = new Bundle();
        args.putString("storeEmail", storeEmail);
        args.putString("postCaption", postCaption);
        args.putString("postImage", postImage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            storeEmail = getArguments().getString("storeEmail");
            postCaption = getArguments().getString("postCaption");
            postImage = getArguments().getString("postImage");


        }


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_consumer_post_dialogue, container, false);
        LinearLayout shareLayout = view.findViewById(R.id.share_post_click);
        shareLayout.setOnClickListener(v -> sharePostToChat());
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
}