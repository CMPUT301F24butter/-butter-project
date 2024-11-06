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
        View view = convertView;

        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.users_content, parent,false);
        }

        User user = users.get(position);

        TextView username = view.findViewById(R.id.user_title);
        username.setText(user.getName());

        TextView email = view.findViewById(R.id.user_info);
        email.setText(user.getEmail());

        return view;
    }
}
