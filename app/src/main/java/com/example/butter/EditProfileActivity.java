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

/**
 * A simple {@link AppCompatActivity} subclass.
 * Uses {@link EditProfileActivity} to edit an existing user in the database.
 * Is called to from {@link ProfileFragment} when the user clicks the button to edit their profile.
 */
public class EditProfileActivity extends AppCompatActivity {

    private UserDB users; // interact with userDB

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        users = new UserDB(); // init the userDB object
        User user = (User) getIntent().getSerializableExtra("user");

        setContentView(R.layout.edit_profile);    // set view to create profile screen

        // setup role spinner
        Spinner roleSpinner = findViewById(R.id.edit_role_spinner);
        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(
                this, R.array.roles_array, android.R.layout.simple_spinner_item);
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
        } else {    // else we are either both, or admin
            roleSpinner.setSelection(2);
        }

        if (user.getPrivileges() > 100) { // if we are higher than entrant, show facility
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
                    // Entrant = 100, Organizer = 200, both = 300
                    if (role.equals("Entrant")) {
                        privileges = 100;
                        facility = null;    // set facility to null
                    }
                    if (role.equals("Organizer")) privileges = 200;
                    if (role.equals("Both")) privileges = 300;

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
                if (selectedRole.equals("Organizer") || selectedRole.equals("Both")) {  // if organizer, add facility
                    // show facility options
                    facilityLabel.setVisibility(View.VISIBLE);
                    editFacility.setVisibility(View.VISIBLE);
                } else {    // we are entrant and should remove facility if added
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
        } else if (!role.equals("Entrant")) { // else if we have facility name to eval
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