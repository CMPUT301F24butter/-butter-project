package com.example.butter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * This is an array adapter for Event objects
 * This is used to display the events an organizer has published on the "Events" screen
 *
 * Current outstanding issues: need to implement poster images
 *
 * @author Nate Pane (natepane)
 */
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
        View view = LayoutInflater.from(context).inflate(R.layout.event_content, parent,false);

        Event event = events.get(position);

        TextView eventName = view.findViewById(R.id.event_name);
        TextView eventDate = view.findViewById(R.id.event_date);
        TextView eventCapacity = view.findViewById(R.id.event_capacity);
        ImageView eventImage = view.findViewById(R.id.event_image);

        eventName.setText(event.getName());
        eventDate.setText(event.getDate());
        if (event.getCapacity() != -1) { // if the event has a waitlist capacity
            eventCapacity.setText(String.format("%d", event.getCapacity()));
        } else { // otherwise, display "N/A"
            eventCapacity.setText("N/A");
        }

        if (event.getImageString() != null) { // if the image string attribute is set
            ImageDB imageDB = new ImageDB();
            Bitmap bitmap = imageDB.stringToBitmap(event.getImageString()); // turn the string data into a bitmap

            eventImage.setImageBitmap(bitmap); // display the image
        }

        return view;
    }
}
