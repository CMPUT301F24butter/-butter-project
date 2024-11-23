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
 * This is an array adapter for User objects
 * This is used to display the users / or the users facilities in "Browse Profiles" and
 * "Browse Facilities"
 *
 * @author Angela Dakay (angelcache)
 */
public class UserArrayAdapter extends ArrayAdapter<User> {
    private ArrayList<User> users;
    private final Context context;
    private boolean isFacility;

    /**
     * Constructor for UserArrayAdapter. Initializes users, context, and isFacility variables.
     * @param context activity or fragment adapter is being used in
     * @param users list os users
     * @param isFacility boolean that confirms whether we're dealing with "Browse Facilities"
     */
    public UserArrayAdapter(Context context, ArrayList<User> users, Boolean isFacility) {
        super(context, 0, users);
        this.users = users;
        this.context = context;
        this.isFacility = isFacility;
    }

    /**
     * Finds the TextViews and sets the text to the right values (eg. facility or name)
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
            userInitial.setText(user.getName().substring(0,1));
        } else {
            userTitle.setText(user.getName());
            userInfo.setText(user.getRole());
            userInitial.setText(user.getName().substring(0,1));
        }

        return view;
    }
}
