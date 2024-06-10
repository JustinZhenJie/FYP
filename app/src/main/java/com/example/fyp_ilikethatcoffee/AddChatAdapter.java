package com.example.fyp_ilikethatcoffee;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class AddChatAdapter extends RecyclerView.Adapter<AddChatAdapter.ViewHolder> {

    private Context context;
    private List<UserAccount> friendList;
    private OnFriendClickListener onFriendClickListener;

    public interface OnFriendClickListener {
        void onFriendClick(UserAccount friend);
    }

    public AddChatAdapter(Context context, List<UserAccount> friendList, OnFriendClickListener listener) {
        this.context = context;
        this.friendList = friendList;
        this.onFriendClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserAccount friend = friendList.get(position);
        holder.bind(friend);
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewProfilePic;
        private TextView textViewFriendName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfilePic = itemView.findViewById(R.id.imageViewProfilePic);
            textViewFriendName = itemView.findViewById(R.id.textViewFriendName);

            itemView.setOnClickListener(view -> {
                if (onFriendClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onFriendClickListener.onFriendClick(friendList.get(position));
                    }
                }
            });
        }

        public void bind(UserAccount friend) {
            textViewFriendName.setText(friend.getUserName());
            Glide.with(itemView.getContext())
                    .load(friend.getImage())
                    .placeholder(R.drawable.img) // Placeholder drawable while loading
                    .error(R.drawable.img) // Error drawable if loading fails
                    .centerCrop()
                    .into(imageViewProfilePic);
        }
    }
}
