package com.example.butter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class EventArrayAdapter extends ArrayAdapter<Event> {
    private ArrayList<Event> events;
    private Context context;

    public EventArrayAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
        this.events = events;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // return super.getView(position, convertView, parent);
        View view = convertView;

        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.event_content, parent,false);
        }

        Event event = events.get(position);

        TextView eventName = view.findViewById(R.id.event_name);
        TextView eventDate = view.findViewById(R.id.event_date);
        TextView eventCapacity = view.findViewById(R.id.event_capacity);

        eventName.setText(event.getName());
        eventDate.setText(event.getDate());
        if (event.getCapacity() != -1) {
            eventCapacity.setText(String.format("Capacity: %d", event.getCapacity()));
        } else {
            eventCapacity.setText("Capacity: N/A");
        }

        return view;
    }
}
