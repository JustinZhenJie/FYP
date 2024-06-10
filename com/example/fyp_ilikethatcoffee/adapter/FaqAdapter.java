package com.example.fyp_ilikethatcoffee.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fyp_ilikethatcoffee.Faq;
import com.example.fyp_ilikethatcoffee.R;

import java.util.List;

public class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.MyViewHolder> {
    List<Faq> list;
    Context context;

    public FaqAdapter(List<Faq> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.faq_layot, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.qestionTv.setText("Question:\n" + list.get(position).getQuestion());
        holder.answerTv.setText("Answer:\n" + list.get(position).getAnswer());
        holder.categoryTv.setText(list.get(position).getCategory());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView qestionTv;
        TextView answerTv;
        TextView categoryTv;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            qestionTv = itemView.findViewById(R.id.qestionTv);
            answerTv = itemView.findViewById(R.id.answerTv);
            categoryTv = itemView.findViewById(R.id.categoryTv);

        }
    }

}
