package com.example.fyp_ilikethatcoffee;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class StoreAdapter extends ArrayAdapter<Store> {
    private Context mContext;
    private int mResource;

    public StoreAdapter(Context context, int resource, List<Store> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(mContext).inflate(mResource, parent, false);
        }

        // Get current store from the position
        Store store = getItem(position);

        // Set store details to TextViews
        TextView textViewStoreName = itemView.findViewById(R.id.textViewStoreName);
        TextView textViewStoreDesc = itemView.findViewById(R.id.textViewStoreDesc);
        TextView textViewStoreAddress = itemView.findViewById(R.id.textViewStoreAddress);
        TextView textViewReviewCount = itemView.findViewById(R.id.textViewReviewCount);
        TextView textViewReadMore = itemView.findViewById(R.id.textViewReadMore);
        TextView textViewAverageRating = itemView.findViewById(R.id.textViewAverageRating);

        // Set the store description and manage its length
        String storeDesc = store.getStoreDesc();
        int maxLength = 100; // Maximum length of store description to display initially
        if (storeDesc.length() > maxLength) {
            textViewReadMore.setVisibility(View.VISIBLE);
            textViewStoreDesc.setText(storeDesc.substring(0, maxLength) + "...");
            textViewReadMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    textViewStoreDesc.setText(storeDesc);
                    textViewReadMore.setVisibility(View.GONE);
                }
            });
        } else {
            textViewStoreDesc.setText(storeDesc);
            textViewReadMore.setVisibility(View.GONE);
        }

        textViewStoreName.setText(store.getStoreName());
        textViewStoreAddress.setText(store.getStoreAddress());
        textViewReviewCount.setText("Reviews: " + store.getReviewCount());
        textViewAverageRating.setText(String.format("Average Rating: %.1f", store.getAverageRating()));

        return itemView;
    }
}
