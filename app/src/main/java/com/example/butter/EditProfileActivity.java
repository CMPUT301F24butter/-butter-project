package com.example.butter;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple activity called from {@link ProfileFragment}
 * To edit/update an existing user in the database
 * Is called to from {@link ProfileFragment} when the user clicks the button to edit their profile.
 * @author Soopyman
 */
public class EditProfileActivity extends AppCompatActivity {

    /**
     * User Database object.
     * Used specifically for updating the user in the database after editing and verifying for validity.
     */
    private UserDB users; // interact with userDB

    /**
     * onCreate contains firstly setting up the views and editable options on screen for specific user,
     * filling it in with already existing user data.
     * This also requires managing the view dependent on which role (i.e. if role is entrant, do not show facility)
     * Contains many different onClickListeners for each of the buttons/spinners:
     * saveButton: performs checks for valid data, and updates user in database if valid.
     * roleSpinner: onClick of a specific role, hide/un-hide facility attributes.
     * There will be more buttons added later.
     * @param savedInstanceState
     * The last saved state of the activity (if exists)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        users = new UserDB(); // init the userDB object
        User user = (User) getIntent().getSerializableExtra("user");

        setContentView(R.layout.edit_profile);    // set view to create profile screen

        // setup role spinner
        Spinner roleSpinner = findViewById(R.id.edit_role_spinner);
        // convert roles array to modifiable list
        List<String> rolesList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.roles_array)));
        // create an array adapter for the list
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rolesList);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        Button saveButton = findViewById(R.id.save_changes_button);    // create button on profile screen

        TextView facilityLabel = findViewById(R.id.facility_label); // view to show facility, used to hide/unhide text

        TextView editInitial = findViewById(R.id.profileText);
        EditText editUsername = findViewById(R.id.username);
        EditText editEmail = findViewById(R.id.email);
        EditText editPhone = findViewById(R.id.create_number_text);
        EditText editFacility = findViewById(R.id.facility_name);

        // now we want to set all of our text and such to be what was included in user object passed
        editInitial.setText(user.getName().substring(0,1));
        editUsername.setText(user.getName());
        editEmail.setText(user.getEmail());
        editPhone.setText(user.getPhoneNumber());

        // now lets change our selection for spinner depending on privileges
        if (user.getPrivileges() < 200) { // then we are entrant
            roleSpinner.setSelection(0); // set spinner to be selected to entrant
        } else if (user.getPrivileges() < 300) { // then we are organizer
            roleSpinner.setSelection(1);
        } else if (user.getPrivileges() < 400) { // then we are both
            roleSpinner.setSelection(2);
        } else {    // else are admin, and should update spinner based on which admin role we are
            rolesList.add("Admin"); // add all possible roles for admin
            rolesList.add("Admin & Entrant");
            rolesList.add("Admin & Organizer");
            rolesList.add("Admin, Organizer, & Entrant");
            roleAdapter.notifyDataSetChanged();
            for (int i = 3; i < roleAdapter.getCount(); i++) { // grab index of our role and set to it
                if (roleAdapter.getItem(i).equals(user.getRole())) {
                    roleSpinner.setSelection(i);
                }
            }
        }

        if (user.getPrivileges() > 100 && user.getPrivileges() != 400 && user.getPrivileges() != 500) { // if we are not admin and/or entrant, show facility
            facilityLabel.setVisibility(View.VISIBLE);
            editFacility.setVisibility(View.VISIBLE);
            editFacility.setText(user.getFacility());
        }

        // now that we are here, we should have all data put in.
        // simply set an onClickListener for the save changes button.


        // on click listener for the create button
        // must perform validity checks first
        // if checks pass, add user to database and go to MainActivity
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if clicked, test if results are valid

                // get the placed text into strings
                String username = editUsername.getText().toString();
                String email = editEmail.getText().toString();
                String phone = editPhone.getText().toString();
                String facility = editFacility.getText().toString(); // will be empty if entrant
                String role = roleSpinner.getSelectedItem().toString();

                String validRet = validityCheck(username, email, phone, facility, role);

                if (validRet.equals("true")) {
                    // if valid, add to database
                    int privileges = 0;  // init privileges number
                    // if phone is empty, set to null
                    if (phone.isEmpty()) phone = null;
                    // if facility is empty (i.e, we are entrant), set to null
                    if (facility.isEmpty()) facility = null;

                    // now to set privilege values:
                    // Entrant = 100, Organizer = 200, both = 300, admin = 400, admin & entrant = 500, admin & org = 600, all = 700
                    switch (role) {
                        case "Entrant":
                            privileges = 100;
                            facility = null;
                            break;
                        case "Organizer":
                            privileges = 200;
                            break;
                        case "Both":
                            privileges = 300;
                            break;
                        case "Admin":
                            privileges = 400;
                            facility = null;
                            break;
                        case "Admin & Entrant":
                            privileges = 500;
                            facility = null;
                            break;
                        case "Admin & Organizer":
                            privileges = 600;
                            break;
                        case "Admin, Organizer, & Entrant":
                            privileges = 700;
                            break;
                    }

                    // now we just simply update the user object with the new one in our database
                    User userUpdate = new User(user.getDeviceID(), username, privileges, facility, email, phone);
                    users.update(userUpdate);

                    // now our updated user should be in the database, and we can return
                    finish();
                } else {    // else then show dialogue message and continue
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                    builder.setTitle("Invalid Signup");
                    builder.setMessage(validRet);
                    builder.setPositiveButton("OK", null);
                    builder.show();
                }
            }
        });

        // OnItemSelected listener for the role spinner
        // if selected to organizer or both, show the facility TextView & EditText
        // else, hide it since we are selected to entrant.
        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedRole = adapterView.getItemAtPosition(i).toString();
                if (!selectedRole.equals("Entrant") && !selectedRole.equals("Admin") && !selectedRole.equals("Admin & Entrant")) {  // if not entrant, add facility
                    // show facility options
                    facilityLabel.setVisibility(View.VISIBLE);
                    editFacility.setVisibility(View.VISIBLE);
                } else {    // else we are entrant and should remove facility if added
                    facilityLabel.setVisibility(View.INVISIBLE);
                    editFacility.setVisibility(View.INVISIBLE);
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // do nothing, default is set to hidden (and entrant)
            }
        });

    }

    /**
     * Takes in all data for a user object, and validates all attributes.
     * This runs many various methods for checking if valid data.
     * @param username
     * If username is empty OR greater than 30 characters, invalid
     * @param email
     * If email is empty OR is not a valid email address (formatting wise), invalid
     * @param phone
     * If phone is not empty AND is not a valid phone number, invalid
     * @param facility
     * If role is not 'Entrant', 'Admin', or 'Admin & Entrant' AND (facility is empty OR more than 20 characters), invalid
     * @param role
     * Role cannot be invalid. Must be passed using "user.getRole()" or a valid role string.
     * @return
     * Returns a string with either "true" if valid,
     * Or will instead return the corresponding error message to be printed later.
     */
    // check for valid info
    private String validityCheck(String username, String email, String phone, String facility, String role) {

        // returns a string
        // string is true or an error message
        String returnString = "true";

        if (username.isEmpty()) {
            returnString = "Username box is empty.";
        } else if (username.length() > 30) {    // 30 character cap for username
            returnString = "Username is too long. Max of 30 characters.";
        } else if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            returnString = "Invalid Email Address.";
        } else if (!phone.isEmpty() && !Patterns.PHONE.matcher(phone).matches()) {
            returnString = "Invalid Phone Number.";
        } else if (!role.equals("Entrant") && !role.equals("Admin") && !role.equals("Admin & Entrant")) { // else if we have facility name to eval
            if (facility.isEmpty()) {
                returnString = "Invalid Facility.";
            } else if (facility.length() > 20) {    // 20 char cap for facility
                returnString = "Facility is too long. Max of 20 characters.";
            }
        }
        if (!returnString.equals("true")) { // if we have invalid, add "please try again" text
            returnString += "\nPlease try again.";
        }
        // else our info is valid
        return returnString;
    }


}