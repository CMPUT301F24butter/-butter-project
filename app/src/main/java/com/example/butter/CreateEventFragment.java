package com.example.butter;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class CreateEventFragment extends AppCompatActivity {

    EditText eventName;
    EditText openDate;
    EditText closeDate;
    EditText eventDate;
    EditText description;
    EditText capacity;
    SwitchCompat geolocationSwitch;

    private FirebaseFirestore db;
    private CollectionReference eventRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_event);

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("event");

        String deviceID = getIntent().getExtras().getString("deviceID"); // logged in deviceID

        Button createButton = findViewById(R.id.create_event_button);
        ImageButton backButton = findViewById(R.id.back_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // go back when back button is clicked
            }
        });

        eventName = findViewById(R.id.name_event);
        openDate = findViewById(R.id.start_date);
        closeDate = findViewById(R.id.end_date);
        eventDate = findViewById(R.id.event_date);
        description = findViewById(R.id.event_description);
        capacity = findViewById(R.id.max_entrants);
        geolocationSwitch = findViewById(R.id.location_switch);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // extracting input
                boolean validDetails = true;
                String name = eventName.getText().toString();
                String registrationOpenDate = openDate.getText().toString();
                String registrationCloseDate = closeDate.getText().toString();
                String date = eventDate.getText().toString();
                String eventDescription = description.getText().toString();
                String maxCapacityString = capacity.getText().toString();
                Boolean geolocation = geolocationSwitch.isChecked();

                int maxCapacity = -1;
                if (!maxCapacityString.isEmpty()) {
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

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date date1, date2, date3;
                try {
                    date1 = formatter.parse(registrationOpenDate);
                    date2 = formatter.parse(registrationCloseDate);
                    date3 = formatter.parse(date);

                    Boolean bool1 = date2.after(date1);
                    Boolean bool2 = date3.after(date2);

                    if (!bool1 || !bool2) { // confirming that all dates are valid, i.e. event date isn't before registration date
                        validDetails = false;
                        Toast toast = Toast.makeText(getApplicationContext(), "Invalid dates.", Toast.LENGTH_LONG);
                        toast.show();
                    }

                } catch (ParseException e) {
                    validDetails = false;
                    Toast toast = Toast.makeText(getApplicationContext(), "Invalid dates.", Toast.LENGTH_LONG);
                    toast.show();
                }


                if (validDetails) { // if event details are valid to this point
                    // create the Event object
                    Event event = new Event(name, deviceID, registrationOpenDate, registrationCloseDate, date, maxCapacity, geolocation, eventDescription);

                    String eventID = name.replace(" ", "_") + "-" + deviceID; // eventID of this potential new event
                    // checking if an event with this ID already exists
                    eventRef.document(eventID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot doc = task.getResult();
                                if (doc.exists()) { // if an event with this ID already exists, don't add the event
                                    Toast toast = Toast.makeText(getApplicationContext(), "Invalid event name.", Toast.LENGTH_LONG);
                                    toast.show();
                                }
                                else { // otherwise, add the event to firebase and return to the previous page
                                    EventDB eventDB = new EventDB();
                                    eventDB.add(event);

                                    finish();
                                }
                            }
                        }
                    });
                }
            }
        });

    }
}
