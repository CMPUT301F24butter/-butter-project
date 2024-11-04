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

public class EditEventActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private CollectionReference eventRef;

    TextView eventNameText;
    EditText registrationOpenText;
    EditText registrationCloseText;
    EditText dateText;
    EditText descriptionText;
    EditText capacityText;
    SwitchCompat geolocationSwitch;

    String eventName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_event);

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("event"); // event collection

        String deviceID = getIntent().getExtras().getString("deviceID"); // logged in deviceID
        String eventID = getIntent().getExtras().getString("eventID"); // eventID

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
                            capacityText.setText("");
                        }

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
                finish();
            }
        });

        // setting click listener for the save button
        Button saveButton = findViewById(R.id.edit_event_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean validDetails = true;
                // retrieving inputted event details
                String registrationOpenDate = registrationOpenText.getText().toString();
                String registrationCloseDate = registrationCloseText.getText().toString();
                String date = dateText.getText().toString();
                String eventDescription = descriptionText.getText().toString();
                String maxCapacityString = capacityText.getText().toString();
                Boolean geolocation = geolocationSwitch.isChecked();

                int maxCapacity = -1; // default capacity if capacity isn't set
                if (!maxCapacityString.isEmpty()) { // if a max capacity was inputted
                    maxCapacity = Integer.parseInt(maxCapacityString);
                    if (maxCapacity < 1) { // max capacity cannot be 0
                        validDetails = false;
                        Toast toast = Toast.makeText(getApplicationContext(), "Capacity cannot be 0.", Toast.LENGTH_LONG);
                        toast.show();
                    }
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

                    // default capacity if capacity isn't set
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

                    finish();
                }
            }
        });

    }
}
