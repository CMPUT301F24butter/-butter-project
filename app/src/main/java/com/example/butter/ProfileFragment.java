package com.example.butter;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment} factory method to
 * create an instance of this fragment.
 * This subclass contains information for the users profile, and the ability to edit said info.
 * Upon clicking to edit profile, this will go to {@link EditProfileActivity}
 * @author Soopyman
 */
public class ProfileFragment extends Fragment {
    /**
     * User object containing all the user data corresponding to deviceID (in onCreate)
     * Is updated everytime it is changed in the database (addSnapshotListener)
     */
    private User user;
    /**
     * View object corresponding to the profile_fragment.xml view we are working with.
     * This object is necessary in order to later update the view after this is passed as a fragment to MainActivity.
     */
    private View view;
    /**
     * A Listener object for the addSnapshotListener checking for updated user data in database.
     * Used for solving a bug with onDestroyView.
     */
    private ListenerRegistration userListener;
    /**
     * A Listener object for the addSnapshotListener checking for updated image data in database.
     * Used for solving a bug with onDestroyView, and listening for changes to the image
     */
    private ListenerRegistration imageListener;
    /**
     * An imageDB object used for parsing string to bitmap
     */
    private ImageDB imageDB;
    /**
     * A base64Image string to be displayed as pfp
     */
    private String base64Image;

    public ProfileFragment() {
        // empty constructor
    }

    /**
     * onCreate simply grabs available user data in the database using the deviceID
     * And will send to updateUserDataInView() in order to update the views with the data.
     * The addSnapshotListener will also continue to listen for if the user is updated in the database.
     * If so, repeat the process above.
     * @param savedInstanceState
     * The last saved state of the fragment (if exists)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get user data from the db, and apply the attributes to our view
        // our user must exist by this point
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("user").document(getArguments().getString("deviceID"));

        // setup a listener to keep updating user class each time it changes (asynchronous)
        userListener = userRef.addSnapshotListener((doc, __) -> {
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

        // setup another listener for getting the profile picture
        DocumentReference imageRef = db.collection("image").document(getArguments().getString("deviceID"));
        // init our imageDB object
        imageDB = new ImageDB();
        imageListener = imageRef.addSnapshotListener((doc, __) -> {
            if (doc != null && doc.exists()) { // if we have an image
                base64Image = doc.getString("imageData");
                // update the views with the new image attribute
            } else {    // else set to null
                base64Image = null;
            }
            // finally update our view with new image
            updateProfilePicInView();
        });
    }

    /**
     * onCreateView simply gets the view for fragment_profile,
     * sets this view as a variable in the class to be used later,
     * and returns the inflated view.
     * @param inflater
     * Simply inflates any views given
     * @param container
     * Contains parent view that the fragment should be attached to
     * @param savedInstanceState
     * Previously saved instance of the fragment (if any)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // detach active listeners
        if (userListener != null) {
            userListener.remove();
            userListener = null;
        }
        if (imageListener != null) {
            imageListener.remove();
            imageListener = null;
        }
    }

    /**
     * updateProfilePicInView() updates the image if a new one has been found
     * If there is no image, set to unique pfp with first initial
     */
    private void updateProfilePicInView() {
        // get our views
        TextView profileInitial = view.findViewById(R.id.profileText);
        CircleImageView profileImage = view.findViewById(R.id.profileImage); // profile image

        // now to update profile picture if none shown
        if (base64Image != null) {  // if our string for our image is not null, set image & hide initial
            profileImage.setImageBitmap(imageDB.stringToBitmap(base64Image));
            profileInitial.setVisibility(View.INVISIBLE);
        } else {    // else show initial and update
            profileImage.setImageResource(R.drawable.profile_circle);
            profileInitial.setText(user.getName().substring(0, 1));
            profileInitial.setVisibility(View.VISIBLE);
        }
    }

    /**
     * updateUserDataInView() uses the private user variable to update the private view object.
     * Runs some checks to decide what will and will not be visible dependent on user data and roles.
     * This method is called only from the addSnapshotListener,
     * which will only be called upon updating the user in the database.
     */
    private void updateUserDataInView() {
        // setup all of the TextViews in view
        TextView name = view.findViewById(R.id.username_text);
        TextView email = view.findViewById(R.id.email_text);
        TextView facility = view.findViewById(R.id.facility_name_text);
        TextView facilityLabel = view.findViewById(R.id.facility_label);
        TextView phone = view.findViewById(R.id.password_text);
        TextView role = view.findViewById(R.id.role_text);
        TextView profileInitial = view.findViewById(R.id.profileText);
        CircleImageView profileImage = view.findViewById(R.id.profileImage); // profile image

        // first lets check if we are not entrant, if so, unhide facility
        if (user.getPrivileges() > 100 && user.getPrivileges() < 400) { // if we are org/both
            facility.setVisibility(View.VISIBLE);
            facility.setText(user.getFacility());   // set our facility as well
            facilityLabel.setVisibility(View.VISIBLE);
        } else if (user.getPrivileges() > 500) {    // if we are admin with org privs
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

        // now to update profile picture if none shown
        if (base64Image == null) {  // set initial and image to unique if null
            profileImage.setImageResource(R.drawable.profile_circle);
            profileInitial.setText(user.getName().substring(0, 1));
            profileInitial.setVisibility(View.VISIBLE);
        }

        // now to update the text with ours given from database
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
                toEditProfile.putExtra("base64Image", base64Image); // pass pfp as an arg
                startActivity(toEditProfile);
            }
        });


    }
}