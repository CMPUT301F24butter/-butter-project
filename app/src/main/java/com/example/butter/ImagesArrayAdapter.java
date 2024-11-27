package com.example.butter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;

import java.util.ArrayList;

/**
 * This is an array adapter for QR codes and Poster objects
 * This is used to display the posters and QR objects in "Browse QR Codes" and "Browse Event Posters".
 *
 * @author Angela Dakay (angelcache)
 */
public class ImagesArrayAdapter extends ArrayAdapter<String> {
    private ArrayList<String> images;
    private final Context context;
    private ArrayList<String> events;
    private FirebaseFirestore db;
    private CollectionReference usersref;
    String username;

    /**
     * Constructor for ImagesArrayAdapter, instantiates images, context, and events variables.
     * @param context activity or fragment adapter is being used in
     * @param images list of image strings to be converted into bitmap
     * @param events list of event names associated with the images
     */
    public ImagesArrayAdapter(Context context, ArrayList<String> images, ArrayList<String> events) {
        super(context, 0, images);
        this.images = images;
        this.context = context;
        this.events = events;
        db = FirebaseFirestore.getInstance();
        usersref = db.collection("user");
    }

    /**
     * Finds the ImageView and TextView and gives them an image (Poster or QRCode) and event name
     * @param position The position of the item within the adapter's data set of the item whose view
     *        we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *        is non-null and of an appropriate type before using. If it is not possible to convert
     *        this view to display the correct data, this method can create a new view.
     *        Heterogeneous lists can specify their number of view types, so that this View is
     *        always of the right type (see {@link #getViewTypeCount()} and
     *        {@link #getItemViewType(int)}).
     * @param parent The parent that this view will eventually be attached to
     * @return View
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = null;
        if(view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.poster_content, parent,false);
        }

        System.out.println("Image: " + images.get(position));
        System.out.println("Event: " + events.get(position));

        String image = images.get(position);
        String sourceID = events.get(position); // source could either be event or user profile
        String name = getName(sourceID);

        Bitmap bitmap = stringToBitmap(image); // turning the string into a bitmap

        ImageView bitmapImage = view.findViewById(R.id.poster_image);
        bitmapImage.setImageBitmap(bitmap); // displaying the bitmap

        TextView imageInfo = view.findViewById(R.id.poster_name);

        // EventIds always have - to seperate the event name + ID
        if (sourceID.contains("-")) {
            imageInfo.setText(String.format("From Event: %s", name)); // tells us which event image is from
        } else { // if it doesn't have this then it is a profile picture
            usersref.document(sourceID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Get username from document
                            username = document.getString("userInfo.name");
                        }
                    }
                    // Sets text after username retrieved
                    imageInfo.setText(String.format("Profile From: %s", username));
                }
            });
        }

        return view;
    }

    /**
     * Gets the event name from eventID by removing everything after the dash and replacing
     * underscores with spaces.
     * @param eventID the event ID that wil be cleaned up to get the event name
     * @return Event Name
     */
    private String getName(String eventID) {
        int dashIndex = eventID.indexOf("-");
        if (dashIndex != -1) {
            eventID = eventID.substring(0, dashIndex);
        }

        // Getting Rid of the _ in the eventID so we get just the eventName
        return eventID.replace("_", " ");
    }

    /**
     * Converts a string into a bitmap.
     * @param base64String the string version of the image that will be converted into bitmap
     * @return Bitmap
     */
    private Bitmap stringToBitmap(String base64String) {
        byte[] imageBytes = Base64.decode(base64String, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        return bitmap;
    }
}
