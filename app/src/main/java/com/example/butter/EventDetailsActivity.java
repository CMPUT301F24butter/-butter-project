package com.example.butter;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
 * @author Nate Pane (natepane) and Angela Dakay (angelcache)
 */
public class EventDetailsActivity extends AppCompatActivity implements GeolocationDialog.GeolocationDialogListener {

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
    private String geolocation;
    private int capacity;
    private Boolean adminPrivilege;

    private Button eventButton;

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
        eventButton = findViewById(R.id.waiting_list_button);

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

                geolocation = doc.getString("eventInfo.geolocationString");

                if (doc.getString("eventInfo.capacityString") == null) {
                    capacity = -1; // there is no capacity limit
                } else {
                    capacity = Integer.parseInt(doc.getString("eventInfo.capacityString"));
                }

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
        setUpEntrantActions();
        addAdminDeleteButton();
    }

    private void setUpEntrantActions() {
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
                        
                        if (listSize == capacity) {
                            String fullText = "Waiting List Full";
                            eventButton.setText(fullText);
                            eventButton.setEnabled(Boolean.FALSE);
                            eventButton.setBackgroundColor(ContextCompat.getColor(EventDetailsActivity.this, R.color.primaryGreyColor));
                        } else if (userAlreadyAdded) {
                            String leaveText = "Leave Waiting List";
                            eventButton.setBackgroundColor(ContextCompat.getColor(EventDetailsActivity.this, R.color.secondaryPurpleColor));
                            eventButton.setText(leaveText);
                        } else {
                            String joinText = "Join Waiting List";
                            eventButton.setText(joinText);
                            eventButton.setBackgroundColor(ContextCompat.getColor(EventDetailsActivity.this, R.color.primaryPurpleColor));        
                        }
                    } else {
                        Log.d("Firebase", "User list doesn't exists.");
                    }
                }
            }
        });

        // adding click listener for waiting list button
        eventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (eventButton.getText() == "Join Waiting List") {
                    // If there is geolocation, they will confirm whether they join or not
                    if (geolocation.equals("true")) {
                        GeolocationDialog locationDialog = new GeolocationDialog(EventDetailsActivity.this, EventDetailsActivity.this);
                        locationDialog.showDialog();
                    } else {
                        joinWaitingList();
                    }
                } else {
                    leaveWaitingList();
                }
                
            }
        });
    }

    private void leaveWaitingList() {
        String userListID  = eventID + "-wait";

        // Generate UserListDB to add new user to the specific user list event
        UserListDB userList = new UserListDB();
        userList.removeFromList(userListID, deviceID);

        // Turn it into Leave Waiting List
        String joinText = "Join Waiting List";
        eventButton.setText(joinText);
        eventButton.setBackgroundColor(ContextCompat.getColor(EventDetailsActivity.this, R.color.primaryPurpleColor));
    }

    @Override
    public void onJoinEventConfirmed(boolean confirmJoin) {
        if (confirmJoin) {
            joinWaitingList();
        } else {
            Toast.makeText(EventDetailsActivity.this, "Join waiting list cancelled.", Toast.LENGTH_SHORT).show();
        }
    }

    private void joinWaitingList() {
        String userListID  = eventID + "-wait";

        // Generate UserListDB to add new user to the specific user list event
        UserListDB userList = new UserListDB();
        userList.addToList(userListID, deviceID);

        // Turn it into Leave Waiting List
        String leftText = "Leave Waiting List";
        eventButton.setText(leftText);
        eventButton.setBackgroundColor(ContextCompat.getColor(EventDetailsActivity.this, R.color.secondaryPurpleColor));
    }

    private void setUpOrganizerOptions() {
        System.out.println("Hey I made it here");
        ImageButton orgOptions = findViewById(R.id.organizer_opt_button);
        orgOptions.setVisibility(View.VISIBLE); // making the organizer options button visible to the organizer

        // adding click listener for delete button
        addOrganizerDeleteButton();

        // adding on click listener for the settings button
        orgOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // open the organizer options dialog box
                new OrganizerOptions(eventID, deviceID).show(getSupportFragmentManager(), "Organizer Settings");
            }
        });
    }

    private void addOrganizerDeleteButton() {
        eventButton.setText("Delete Event");
        eventButton.setOnClickListener(new View.OnClickListener() {
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

    private void addAdminDeleteButton() {
        RelativeLayout privilegesButtons = findViewById(R.id.privileges_layout);

        ImageButton adminButton = privilegesButtons.findViewById(R.id.admin_delete_button);
        adminButton.setVisibility(View.VISIBLE);

        adminButton.setOnClickListener(new View.OnClickListener() {
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
