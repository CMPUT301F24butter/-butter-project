package com.example.butter;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.RegisteredViewHolder>{
    private List<Event> itemList;
    private OnItemClickListener itemClickListener;

    public HomeAdapter(List<Event> itemList) {
        this.itemList = itemList;
    }

    public interface OnItemClickListener {
        void onItemClick(Event event, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
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
        // Decode Base64 image string to Bitmap
        if (item.getImageString() != null) {
            byte[] decodedString = Base64.decode(item.getImageString(), Base64.DEFAULT);
            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.eventImage.setImageBitmap(decodedBitmap);
        }
        else{
            holder.eventImage.setImageResource(R.drawable.splash_gradient);
        }


        // Set up click listener for the item
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(item, position);
            }
        });
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
        ImageView eventImage;

        public RegisteredViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.entrant_event_title);
            dateTextView = itemView.findViewById(R.id.entrant_event_date);
            eventImage = itemView.findViewById(R.id.entrant_event_poster);
        }
    }
}
