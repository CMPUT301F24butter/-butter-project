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

public class UserArrayAdapter extends ArrayAdapter<User> {
    private ArrayList<User> users;
    private final Context context;
    private boolean isFacility;

    public UserArrayAdapter(Context context, ArrayList<User> users, Boolean isFacility) {
        super(context, 0, users);
        this.users = users;
        this.context = context;
        this.isFacility = isFacility;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.users_content, parent,false);
        }

        User user = users.get(position);
        TextView userInitial = view.findViewById(R.id.user_initial);
        TextView userTitle = view.findViewById(R.id.user_title);
        TextView userInfo = view.findViewById(R.id.user_info);

        if (isFacility) {
            userTitle.setText(user.getFacility());
            userInfo.setText(user.getName());
        } else {
            userTitle.setText(user.getName());
            userInfo.setText(user.getRole());
        }


        return view;
    }
}
