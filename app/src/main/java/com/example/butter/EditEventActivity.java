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
        String eventID = getIntent().getExtras().getString("eventID"); // logged in deviceID

        eventNameText = findViewById(R.id.event_name);
        registrationOpenText = findViewById(R.id.event_start_date);
        registrationCloseText = findViewById(R.id.end_date);
        dateText = findViewById(R.id.event_date);
        descriptionText = findViewById(R.id.event_description);
        capacityText = findViewById(R.id.max_entrants);
        geolocationSwitch = findViewById(R.id.location_switch);

        eventRef.document(eventID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
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

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button saveButton = findViewById(R.id.edit_event_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean validDetails = true;
                String registrationOpenDate = registrationOpenText.getText().toString();
                String registrationCloseDate = registrationCloseText.getText().toString();
                String date = dateText.getText().toString();
                String eventDescription = descriptionText.getText().toString();
                String maxCapacityString = capacityText.getText().toString();
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

                if (validDetails) {
                    Event event = new Event(eventName, deviceID, registrationOpenDate, registrationCloseDate, date, maxCapacity, geolocation, eventDescription);

                    EventDB eventDB = new EventDB();
                    eventDB.update(event);

                    finish();
                }
            }
        });

    }
}
