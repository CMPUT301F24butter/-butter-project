package com.example.butter;

import android.app.AlertDialog;
import android.content.Intent;
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
 * A simple activity called from {@link SplashActivity}
 * Creates a new user in the database, given that this user does not exist.
 * The user is established to have not existed in SplashActivity.
 * Thus, is called to from {@link SplashActivity} when user does not exist on boot.
 * @author Soopyman
 */
public class CreateProfileActivity extends AppCompatActivity {

    /**
     * User database object.
     * Used for adding the user to the database once validity checks are passed.
     */
    private UserDB users; // interact with userDB

    /**
     * onCreate contains all of the guts for this activity.
     * Sets up all editable on screen, and uses onClickListeners to make changes:
     * roleSpinner: this will hide/un-hide the facility depending on what privilege level the user selects.
     * createButton: this will grab all of the inputted data from the user,
     * and will then run verification checks to make sure that the data is valid.
     * If so, get into a user object and add to the database, which we can then proceed to {@link MainActivity} as normal.
     * If not valid, then throw an error message box with the corresponding invalidity, forcing the user to try again.
     * This Activity will not change until a valid user has been inputted.
     * @param savedInstanceState
     * The last saved state of the activity (if exists)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        users = new UserDB(); // init the userDB object

        setContentView(R.layout.create_profile);    // set view to create profile screen

        // must setup role spinner
        Spinner roleSpinner = findViewById(R.id.role_spinner);
        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(
                this, R.array.roles_array, android.R.layout.simple_spinner_item);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        Button createButton = findViewById(R.id.sign_up_button);    // create button on profile screen

        TextView facilityLabel = findViewById(R.id.facility_label); // view to show facility, used to hide/unhide text

        EditText editUsername = findViewById(R.id.username);
        EditText editEmail = findViewById(R.id.email);
        EditText editPhone = findViewById(R.id.phone);
        EditText editFacility = findViewById(R.id.facility_name);

        // on click listener for the create button
        // must perform validity checks first
        // if checks pass, add user to database and go to MainActivity
        createButton.setOnClickListener(new View.OnClickListener() {
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
                    if (role.equals("Entrant")) privileges = 100;
                    if (role.equals("Organizer")) privileges = 200;
                    if (role.equals("Both")) privileges = 300;

                    // now we just simply create a user object, and pass it to our database
                    User user = new User(getIntent().getExtras().getString("deviceID"), username, privileges, facility, email, phone);
                    users.add(user);

                    // now our new user should be in the database, and go to MainActivity

                    Intent toMainActivity = new Intent(CreateProfileActivity.this, MainActivity.class);
                    startActivity(toMainActivity);
                    finish();
                } else {    // else then show dialogue message and continue
                    AlertDialog.Builder builder = new AlertDialog.Builder(CreateProfileActivity.this);
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

    /**
     * validityCheck performs basic checks to ensure the user has inputted valid data for each attribute.
     * @param username
     * If username is empty OR greater than 30 characters, invalid
     * @param email
     * If email is empty OR is not a valid email address (formatting wise), invalid
     * @param phone
     * If phone is not empty AND is not a valid phone number, invalid
     * @param facility
     * If role is not 'Entrant' AND (facility is empty OR more than 20 characters), invalid
     * @param role
     * Role cannot be invalid. Must be passed using "user.getRole()" or a valid role string.
     * @return
     * Returns "true" if all data is valid
     * Else returns an error string with the corresponding invalidity, later to be printed.
     */
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