package com.example.butter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * This activity is used to edit the details of an event
 * Called from {@link OrganizerOptions}, which passes the eventID as an argument.
 * {@link OrganizerOptions} is traversed to from the {@link EventDetailsActivity} with the corresponding event being viewed.
 * Very similar to {@link CreateEventFragment}, but instead provides options to edit an existing event.
 * Once again, all event details are validated before officially updating
 * When complete, the event details are updated in firebase
 * Organizer's can not change the name of an event
 *
 * Current outstanding issues: need to implement poster images
 *
 * @author Nate Pane (natepane)
 */
public class EditEventActivity extends AppCompatActivity {

    /**
     * Database object for access to the database
     */
    private FirebaseFirestore db;
    /**
     * Collection reference from the database for events.
     */
    private CollectionReference eventRef;
    /**
     * TextView for event name (since this cannot be changed)
     */
    TextView eventNameText;
    /**
     * EditText for opening reg date
     */
    EditText registrationOpenText;
    /**
     * EditText for closing reg date
     */
    EditText registrationCloseText;
    /**
     * EditText for date of event
     */
    EditText dateText;
    /**
     * EditText for description of event
     */
    EditText descriptionText;
    /**
     * EditText for capacity of event
     */
    TextView capacityText;
    /**
     * SwitchCompat for the geolocation switch
     */
    SwitchCompat geolocationSwitch;
    /**
     * A string for the event name itself
     */
    String eventName;

    /**
     * onCreate method performs everything here.
     * We simply grab the eventID from the args passed over,
     * which we then grab all of the attributes for the event from the database,
     * and update these attributes for our EditTexts/Geo-switch.
     * Two onClickListeners:
     * saveButton: Once clicked, perform all validity checks on the newly updated event info,
     * which if valid, update the event in the event database.
     * (Note: no new QRCode is needed since it is linked to the eventID itself)
     * backButton: simply sends the user back to the previous screen {@link EventDetailsActivity},
     * and no changes are made to the event.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_event);

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("event"); // event collection

        String deviceID = getIntent().getExtras().getString("deviceID"); // logged in deviceID
        String eventID = getIntent().getExtras().getString("eventID"); // clicked eventID

        // getting all input boxes
        eventNameText = findViewById(R.id.event_name);
        registrationOpenText = findViewById(R.id.event_start_date);
        registrationCloseText = findViewById(R.id.end_date);
        dateText = findViewById(R.id.event_date);
        descriptionText = findViewById(R.id.event_description);
        capacityText = findViewById(R.id.max_entrants);
        geolocationSwitch = findViewById(R.id.location_switch);

        // retrieving data for this event
        eventRef.document(eventID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        // retrieving current event details and setting the input boxes with these details
                        eventNameText.setText(doc.getString("eventInfo.name"));
                        eventName = doc.getString("eventInfo.name");
                        registrationOpenText.setText(doc.getString("eventInfo.registrationOpenDate"));
                        registrationCloseText.setText(doc.getString("eventInfo.registrationCloseDate"));
                        dateText.setText(doc.getString("eventInfo.date"));
                        descriptionText.setText(doc.getString("eventInfo.description"));

                        String capacity = doc.getString("eventInfo.capacityString");
                        if (capacity != null) {
                            capacityText.setText(capacity);
                        } else {
                            capacityText.setText("N/A");
                        }

                        // setting the geolocation switch
                        String geolocation = doc.getString("eventInfo.geolocationString");
                        if (Objects.equals(geolocation, "true")) {
                            geolocationSwitch.setChecked(true);
                        }
                    }
                }
            }
        });

        // setting click listener for the back button
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // returning to the previous page
            }
        });

        // setting click listener for the save button
        Button saveButton = findViewById(R.id.edit_event_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean validDetails = true; // represents if the event details are valid or not
                // retrieving inputted event details
                String registrationOpenDate = registrationOpenText.getText().toString();
                String registrationCloseDate = registrationCloseText.getText().toString();
                String date = dateText.getText().toString();
                String eventDescription = descriptionText.getText().toString();
                String maxCapacityString = capacityText.getText().toString();
                Boolean geolocation = geolocationSwitch.isChecked();

                int maxCapacity = -1; // default capacity if capacity isn't set
                if (!maxCapacityString.equals("N/A")) { // if a max capacity was inputted
                    maxCapacity = Integer.parseInt(maxCapacityString);
                }

                if (eventDescription.isEmpty()) { // event description cannot be empty
                    validDetails = false;
                    Toast toast = Toast.makeText(getApplicationContext(), "Must provide an event description.", Toast.LENGTH_LONG);
                    toast.show();
                }

                // format for date input
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date todaysDate = new Date();
                String todayString = formatter.format(todaysDate);
                Date date1, date2, date3, today;
                try {
                    date1 = formatter.parse(registrationOpenDate);
                    date2 = formatter.parse(registrationCloseDate);
                    date3 = formatter.parse(date);
                    today = formatter.parse(todayString);

                    Boolean bool1 = date2.after(date1);
                    Boolean bool2 = date3.after(date2);
                    Boolean bool3 = date1.after(today);

                    // confirming that all dates are valid, i.e. event date isn't before registration date
                    if (!bool1 || !bool2 || !bool3) {
                        validDetails = false;
                        Toast toast = Toast.makeText(getApplicationContext(), "Invalid dates.", Toast.LENGTH_LONG);
                        toast.show();
                    }

                } catch (ParseException e) {
                    validDetails = false;
                    Toast toast = Toast.makeText(getApplicationContext(), "Invalid dates.", Toast.LENGTH_LONG);
                    toast.show();
                }

                if (validDetails) { // if the event details are valid, update the event in the database
                    Event event = new Event(eventName, deviceID, registrationOpenDate, registrationCloseDate, date, maxCapacity, geolocation, eventDescription);

                    EventDB eventDB = new EventDB();
                    eventDB.update(event);

                    finish(); // returning to the previous screen
                }
            }
        });

    }
}
