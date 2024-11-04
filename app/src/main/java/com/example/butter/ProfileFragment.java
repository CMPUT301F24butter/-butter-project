package com.example.butter;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    private User user;
    private View view;

    public ProfileFragment() {
        // empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get user data from the db, and apply the attributes to our view
        // our user must exist by this point
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("user").document(getArguments().getString("deviceID"));

        // setup a listener to keep updating user class each time it changes (asynchronous)
        userRef.addSnapshotListener((doc, __) -> {
            if (doc != null && doc.exists()) {
                // get data from userData
                String email = doc.getString("userInfo.email");
                String facility = doc.getString("userInfo.facility");
                String name = doc.getString("userInfo.name");
                String phone = doc.getString("userInfo.phoneNumber");
                int privileges = Integer.parseInt(doc.getString("userInfo.privilegesString")); // parse to int

                // now we should have all data. lets put into user obj
                user = new User(getArguments().getString("deviceID"), name, privileges, facility, email, phone);

                // now update the UI with the user (or new) user data
                updateUserDataInView();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        return view;
    }

    private void updateUserDataInView() {
        // setup all of the TextViews in view
        TextView name = view.findViewById(R.id.username_text);
        TextView email = view.findViewById(R.id.email_text);
        TextView facility = view.findViewById(R.id.facility_name_text);
        TextView facilityLabel = view.findViewById(R.id.facility_label);
        TextView phone = view.findViewById(R.id.password_text);
        TextView role = view.findViewById(R.id.role_text);
        TextView profileInitial = view.findViewById(R.id.profileText);

        // first lets check if we are higher than entrant, if so, unhide facility
        if (user.getPrivileges() > 100) { // if we are higher than entrant
            facility.setVisibility(View.VISIBLE);
            facility.setText(user.getFacility());   // set our facility as well
            facilityLabel.setVisibility(View.VISIBLE);
        } else {    // else we want to hide this
            facilityLabel.setVisibility(View.INVISIBLE);
            facility.setVisibility(View.INVISIBLE);
        }
        // if we do not have a phone #
        if (user.getPhoneNumber() == null) {    // if our phone # is empty
            phone.setText(getString(R.string.empty_phone));
        } else {
            phone.setText(user.getPhoneNumber());
        }

        // now to update the text with ours given from database
        profileInitial.setText(user.getName().substring(0, 1));
        name.setText(user.getName());
        email.setText(user.getEmail());
        role.setText(user.getRole());

        // finally, lets set up an onClickListener for the edit profile button
        Button editProfile = view.findViewById(R.id.edit_profile_button);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // simply pass the user object to our EditProfile activity
                Intent toEditProfile = new Intent(getContext(), EditProfileActivity.class);
                toEditProfile.putExtra("user", user);
                startActivity(toEditProfile);
            }
        });


    }
}