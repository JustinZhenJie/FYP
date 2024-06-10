package com.example.fyp_ilikethatcoffee;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<UserAccount> userList;
    private OnItemClickListener onItemClickListener;

    public UserAdapter() {
        this.userList = new ArrayList<>();
    }

    public interface OnItemClickListener {
        void onItemClick(UserAccount user);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setUserList(List<UserAccount> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserAccount user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView imageViewProfilePic;
        private TextView textViewUserName;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfilePic = itemView.findViewById(R.id.imageViewProfilePic);
            textViewUserName = itemView.findViewById(R.id.textViewUserName);

            itemView.setOnClickListener(view -> {
                if (onItemClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClickListener.onItemClick(userList.get(position));
                    }
                }
            });
        }

        public void bind(UserAccount user) {
            textViewUserName.setText(user.getUserName());
            Glide.with(itemView.getContext())
                    .load(user.getImage()) // Assuming `user.getImage()` returns the URL of the profile picture
                    .placeholder(R.drawable.img) // Placeholder image
                    .error(R.drawable.img) // Error image
                    .into(imageViewProfilePic);
        }
    }
}
