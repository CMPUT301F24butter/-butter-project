package com.example.butter;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.RegisteredViewHolder>{
    private List<Event> itemList;

    public HomeAdapter(List<Event> itemList) {
        this.itemList = itemList;
    }

    public void setItemList(List<Event> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RegisteredViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.entrant_event_content, parent, false);
        return new RegisteredViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RegisteredViewHolder holder, int position) {
        Event item = itemList.get(position);
        holder.nameTextView.setText(item.getName());
        holder.dateTextView.setText(item.getDate());
    }

    @Override
    public int getItemCount() {
        //return itemList == null ? 0 : itemList.size();
        int count = itemList == null ? 0 : itemList.size();
        Log.d("HomeAdapter", "getItemCount: " + count);
        return count;
    }

    static class RegisteredViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView dateTextView;

        public RegisteredViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.entrant_event_title);
            dateTextView = itemView.findViewById(R.id.entrant_event_date);
        }
    }
}
