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
 * This is the array adapter for User objects
 * This is used to display users in user lists
 *
 * Current outstanding issues: need to implement poster images
 *
 * @author Nate Pane (natepane)
 */
public class EntrantsArrayAdapter extends ArrayAdapter<User> {
    private ArrayList<User> users;
    private Context context;

    public EntrantsArrayAdapter(Context context, ArrayList<User> users) {
        super(context, 0, users);
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // return super.getView(position, convertView, parent);
        View view = null;

        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.users_content, parent,false);
        }

        User user = users.get(position);

        TextView username = view.findViewById(R.id.user_title);
        username.setText(user.getName());

        TextView email = view.findViewById(R.id.user_info);
        email.setText(user.getEmail());

        ImageView profileImage = view.findViewById(R.id.profileImage);
        if (user.getProfilePicString() != null) { // if this user object has profile pic string data
            ImageDB imageDB = new ImageDB();
            Bitmap bitmap = imageDB.stringToBitmap(user.getProfilePicString()); // convert the string to a bitmap
            profileImage.setImageBitmap(bitmap); // displaying the profile picture
        } else { // otherwise
            TextView initial = view.findViewById(R.id.user_initial);
            initial.setText(user.getName().substring(0, 1)); // set the profile picture to be the first initial of the username
        }

        return view;
    }
}
