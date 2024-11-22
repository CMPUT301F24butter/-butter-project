package com.example.butter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;

/**
 * This activity shows the details of an event when it is clicked on from the "Events" screen
 * It also includes buttons for more organizer options (e.g. edit event, see QR Code, etc.), as well as a button to delete the event
 *
 * Current outstanding issues: need to implement poster images
 *
 * @author Nate Pane (natepane)
 */
public class EventDetailsActivity extends AppCompatActivity {

    TextView eventNameText;
    TextView registrationOpenText;
    TextView registrationCloseText;
    TextView eventDateText;
    TextView eventDescriptionText;

    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference userListRef;

    private String organizerID;
    private String eventID;
    private String deviceID;
    private Boolean adminPrivilege;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_screen);

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("event"); // event collection
        userListRef = db.collection("userList");

        deviceID = getIntent().getExtras().getString("deviceID"); // logged in deviceID
        eventID = getIntent().getExtras().getString("eventID"); // clicked eventID
        adminPrivilege = getIntent().getExtras().getBoolean("adminPrivilege", Boolean.FALSE); // Default to false if not found

        // getting all text boxes
        eventNameText = findViewById(R.id.event_title);
        registrationOpenText = findViewById(R.id.register_opens);
        registrationCloseText = findViewById(R.id.register_closes);
        eventDateText = findViewById(R.id.event_date);
        eventDescriptionText = findViewById(R.id.event_description);

        // retrieving event info for this eventID
        DocumentReference docRef = eventRef.document(eventID);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot doc, @Nullable FirebaseFirestoreException error) {
                // setting text boxes with corresponding event info pulled from the database
                eventNameText.setText(doc.getString("eventInfo.name"));
                String registrationOpenDate = doc.getString("eventInfo.registrationOpenDate");
                registrationOpenText.setText(String.format("Registration Opens: %s", registrationOpenDate));
                String registrationCloseDate = doc.getString("eventInfo.registrationCloseDate");
                registrationCloseText.setText(String.format("Registration Closes: %s", registrationCloseDate));
                String eventDate = doc.getString("eventInfo.date");
                eventDateText.setText(String.format("Event Date: %s", eventDate));
                eventDescriptionText.setText(doc.getString("eventInfo.description"));

                // get the organizers ID to see what the user has access to
                organizerID = doc.getString("eventInfo.organizerID");
                System.out.println("Organizer: " + organizerID);
                System.out.println("Entrant: " + deviceID);

                // If the user is the event's organizer, they will see organizer options
                if (deviceID.equals(organizerID)) {
                    setUpOrganizerOptions();
                } else if (adminPrivilege) { // user has admin privileges, can see delete button
                    setupAdminOptions();
                } else {
                    setUpEntrantActions();
                }
            }
        });

        // adding on click listener for the back button
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void setupAdminOptions() {
        addDeleteButtonActions();
    }

    private void setUpEntrantActions() {
        // adding click listener for waiting list button
        Button entrantEventButton = findViewById(R.id.waiting_list_button);
        entrantEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userListID  = eventID + "-wait";

                userListRef.document(userListID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            if (doc.exists()) {
                                HashMap<String, Object> updates = new HashMap<>();

                                String listSizeString = doc.getString("size");
                                int listSize = Integer.parseInt(listSizeString); // # of entrants in the list

                                // Check if user is already in waiting list
                                boolean userAlreadyAdded = false;
                                for (int i = 0; i < listSize; i++) {
                                    if (deviceID.equals(doc.getString("user" + i))) {
                                        userAlreadyAdded = true;
                                        break;
                                    }
                                }

                                if (!userAlreadyAdded) {
                                    // Putting the user into the waiting list for the event
                                    updates.put("size", String.valueOf(listSize + 1));
                                    updates.put("type", "waitlist");
                                    updates.put("user" + listSize, deviceID);

                                    // Update the userlist
                                    userListRef.document(userListID).update(updates).addOnSuccessListener(aVoid -> {
                                        Log.d("Firebase", "User added to waiting list!");
                                        entrantEventButton.setEnabled(false);
                                        String joinedText = "Joined Waiting List";
                                        entrantEventButton.setText(joinedText); // temporary until we implement leave waiting list
                                    });
                                } else {
                                    entrantEventButton.setEnabled(false);
                                    String alreadyJoinedText = "Already Joined Waiting List"; // temporary until we implement leave waiting list
                                    entrantEventButton.setText(alreadyJoinedText);
                                }
                            } else {
                                Log.d("Firebase", "User list doesn't exists.");
                            }
                        }
                    }
                });
            }
        });
    }

    private void setUpOrganizerOptions() {
        System.out.println("Hey I made it here");
        ImageButton orgOptions = findViewById(R.id.organizer_opt_button);
        orgOptions.setVisibility(View.VISIBLE); // making the organizer options button visible to the organizer

        // adding click listener for delete button
        addDeleteButtonActions();

        // adding on click listener for the settings button
        orgOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // open the organizer options dialog box
                new OrganizerOptions(eventID, deviceID).show(getSupportFragmentManager(), "Organizer Settings");
            }
        });
    }

    private void addDeleteButtonActions() {
        Button deleteEventButton = findViewById(R.id.waiting_list_button);
        deleteEventButton.setText("Delete Event");
        deleteEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String waitlistID = eventID + "-wait";
                String drawlistID = eventID + "-draw";
                String registerListID = eventID + "-registered";
                String cancelledListID = eventID + "-cancelled";

                // deleting all user lists associated with this event
                UserListDB userListDB = new UserListDB();
                userListDB.deleteList(waitlistID);
                userListDB.deleteList(drawlistID);
                userListDB.deleteList(registerListID);
                userListDB.deleteList(cancelledListID);

                // deleting the QR code associated with this event
                QRCodeDB qrCodeDB = new QRCodeDB();
                qrCodeDB.delete(eventID);

                // deleting the event itself
                EventDB eventDB = new EventDB();
                eventDB.delete(eventID);
                Toast.makeText(EventDetailsActivity.this, "Event successfully deleted.", Toast.LENGTH_SHORT).show();

                finish(); // returning to the previous screen
            }
        });
    }
}
