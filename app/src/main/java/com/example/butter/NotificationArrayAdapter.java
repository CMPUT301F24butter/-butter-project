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

import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

/**
 * This is the array adapter for notifications on the {@link NotificationsFragment} screen.
 * Is used to display the singular list of notifications.
 * @author Ahmer
 */

public class NotificationArrayAdapter extends ArrayAdapter<Notification> {

    Context context;
    private ArrayList<Notification> notifications;

    public NotificationArrayAdapter(Context context, ArrayList<Notification> notifications) {
        super(context, 0, notifications);
        this.context = context;
        this.notifications = notifications;
    }

    /**
     * Gets the view for each notification
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.poster_content, parent,false);

        TextView message = view.findViewById(R.id.poster_name);
        TextView eventName = view.findViewById(R.id.poster_detail);
        RoundedImageView eventImage = view.findViewById(R.id.poster_image);

        Notification notification = notifications.get(position);

        message.setText(notification.getMessage());
        eventName.setText(notification.getEventSender());

        if (notification.getEventImage() != null) {
            ImageDB imageDB = new ImageDB();
            Bitmap bitmap = imageDB.stringToBitmap(notification.getEventImage());
            eventImage.setImageBitmap(bitmap);
        } else {
            eventImage.setImageResource(R.drawable.splash_gradient);
        }

        return view;

    }
}