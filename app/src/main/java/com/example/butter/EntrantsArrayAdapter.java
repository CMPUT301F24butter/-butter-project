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
            view = LayoutInflater.from(context).inflate(R.layout.entrant_content, parent,false);
        }

        User user = users.get(position);

        System.out.println(user.getName());

        TextView username = view.findViewById(R.id.entrant_name);
        username.setText(user.getName());

        return view;
    }
}
