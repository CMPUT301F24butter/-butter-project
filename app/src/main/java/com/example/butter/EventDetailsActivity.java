package com.example.butter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * This activity shows the details of an event when it is clicked on from the "Events" screen
 * It also includes buttons for more organizer options (e.g. edit event, see QR Code, etc.), as well as a button to delete the event
 *
 * Current outstanding issues: need to implement poster images
 *
 * @author Nate Pane (natepane) and Angela Dakay (angelcache) and Bir Parkash(bparkash)
 */
public class EventDetailsActivity extends AppCompatActivity implements GeolocationDialog.GeolocationDialogListener, ConfirmationDialog.ConfirmationDialogListener {

    TextView eventNameText;
    TextView registrationOpenText;
    TextView registrationCloseText;
    TextView eventDateText;
    TextView eventDescriptionText;
    ImageView posterImage;

    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference userListRef;
    private CollectionReference imageRef;
    private CollectionReference notificationRef;

    private String organizerID;
    private String eventID;
    private String deviceID;
    private String geolocation;
    private int capacity;
    private Boolean adminPrivilege;
    private String listType;
    private Boolean valid_date;

    private Button eventButton;
    private Button declineButton;
    public interface OnDateValidationListener {
        void onResult(boolean isValid);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_screen);

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("event"); // event collection
        userListRef = db.collection("userList");
        imageRef = db.collection("image");
        notificationRef = db.collection("notification");

        deviceID = getIntent().getExtras().getString("deviceID"); // logged in deviceID
        eventID = getIntent().getExtras().getString("eventID"); // clicked eventID
        adminPrivilege = getIntent().getExtras().getBoolean("adminPrivilege", Boolean.FALSE); // Default to false if not found
        listType = getIntent().getExtras().getString("listType"); // clicked even from register or event list


        // getting all text boxes
        eventNameText = findViewById(R.id.event_title);
        registrationOpenText = findViewById(R.id.register_opens);
        registrationCloseText = findViewById(R.id.register_closes);
        eventDateText = findViewById(R.id.event_date);
        eventDescriptionText = findViewById(R.id.event_description);
        eventButton = findViewById(R.id.waiting_list_button);
        declineButton = findViewById(R.id.decline_invitation_button);
        declineButton.setVisibility(View.INVISIBLE);
        posterImage = findViewById(R.id.event_screen_image);

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
                    confirmlistType();
                    //setUpEntrantActions();
                }
            }
        });

        // retrieving image data for this eventID
        DocumentReference imageDocRef = imageRef.document(eventID);
        imageDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot doc, @Nullable FirebaseFirestoreException error) {
                if (doc.exists()) { // if there is image data associated with this event
                    String base64string = doc.getString("imageData"); // retrieving the image's string data
                    ImageDB imageDB = new ImageDB();
                    Bitmap bitmap = imageDB.stringToBitmap(base64string); // converting string data into a bitmap
                    int color = ContextCompat.getColor(getApplicationContext(), R.color.white);
                    posterImage.setBackgroundColor(color);
                    posterImage.setImageBitmap(bitmap); // displaying the image
                } else {
                    int color = ContextCompat.getColor(getApplicationContext(), R.color.secondaryGreyColor);
                    posterImage.setBackgroundColor(color);
                    posterImage.setImageDrawable(null);
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
        RelativeLayout privilegesButtons = findViewById(R.id.privileges_layout);

        ImageButton adminButton = privilegesButtons.findViewById(R.id.admin_delete_button);
        adminButton.setVisibility(View.VISIBLE);

        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConfirmationDialog dialog = new ConfirmationDialog(EventDetailsActivity.this, EventDetailsActivity.this, "Event");
                dialog.showDialog();
            }
        });
    }

    private void setUpEntrantActions() {
        Log.d("CheckingListType2", "Starting to confirm list type for eventID: " + listType);
        String userListID = eventID + "-" + listType;
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

                        if ((listSize == capacity) && (listType.equals("wait"))) {
                            String fullText = "Waiting List Full";
                            eventButton.setText(fullText);
                            eventButton.setEnabled(Boolean.FALSE);
                            eventButton.setBackgroundColor(ContextCompat.getColor(EventDetailsActivity.this, R.color.primaryGreyColor));
                        } else if (userAlreadyAdded) {
                            String leaveText = "";
                            if (listType.equals("wait")) {
                                leaveText = "Leave Waiting List";
                                eventButton.setBackgroundColor(ContextCompat.getColor(EventDetailsActivity.this, R.color.primaryPurpleColor));

                            } else if (listType.equals("registered")) {
                                leaveText = "Leave Event";
                                eventButton.setBackgroundColor(ContextCompat.getColor(EventDetailsActivity.this, R.color.primaryPurpleColor));

                            } else if (listType.equals("draw")) {
                                leaveText = "Accept Invitation";
                                eventButton.setBackgroundColor(ContextCompat.getColor(EventDetailsActivity.this, R.color.primaryPurpleColor));
                                declineButton.setVisibility(View.VISIBLE);
                            }
                            eventButton.setText(leaveText);


                        } else {
                            isValidDate(eventID, isValid -> {
                                if (isValid) {
                                    Log.d("ValidDate", "Confirm valid boolean: " + isValid);
                                    String joinText = "Registration Date Passed";
                                    eventButton.setText(joinText);
                                    eventButton.setBackgroundColor(ContextCompat.getColor(EventDetailsActivity.this, R.color.primaryGreyColor));
                                    eventButton.setEnabled(false);
                                } else {
                                    Log.d("ValidDate", "Confirm false valid boolean: " + isValid);
                                    String joinText = "Join Waiting List";
                                    eventButton.setText(joinText);
                                    eventButton.setBackgroundColor(ContextCompat.getColor(EventDetailsActivity.this, R.color.primaryPurpleColor));
                                    eventButton.setEnabled(true);
                                }
                            });

                        }
                    } else {
                        Log.d("Firebase", "User list doesn't exists.");
                    }
                }
            }
        });
        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveDrawList();
                joinCancelList();
                String joinText = "Invitation Declined Successfully";
                declineButton.setText(joinText);
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

                } else if (eventButton.getText() == "Accept Invitation") {
                    leaveDrawList();
                    joinRegisteredList();
                } else if (eventButton.getText() == "Leave Event") {
                    leaveRegisteredList();
                    joinCancelList();

                } else {
                    leaveWaitingList();
                }


            }

        });

    }


    /**
     * Verify if list is correct type, check if user try to scan qr code to join but already in register or draw lsit
     */
    private void confirmlistType() {
        // Define the possible list types
        if (listType.equals("wait")) {
            String[] listTypes = {"wait", "registered", "draw"};
            final boolean[] found = {false}; // Shared flag to indicate a match

            for (String type : listTypes) {
                if (found[0]) break; // Stop initiating new queries if a match is found

                String userListID = eventID + "-" + type;

                db.collection("userList").document(userListID)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists() && !found[0]) { // Check flag before processing
                                String listSizeString = documentSnapshot.getString("size");
                                int listSize = listSizeString != null ? Integer.parseInt(listSizeString) : 0;

                                for (int i = 0; i < listSize; i++) {
                                    //if (found[0]) return; // Exit the listener if a match is already found

                                    String userKey = "user" + i;
                                    String userId = documentSnapshot.getString(userKey);

                                    if (deviceID.equals(userId)) {
                                        // Match found
                                        listType = type; // Update listType
                                        found[0] = true; // Set the flag to prevent further updates
                                        Log.d("ConfirmListType", "List type confirmed: " + listType);

                                        // Call setUpEntrantActions after confirming the list type
                                        setUpEntrantActions();
                                        return; // Exit the listener immediately
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(e -> Log.e("ConfirmListType", "Error checking list type: " + e.getMessage()));
            }
        }
        setUpEntrantActions();

    }

    /**
     * Verify if registerationdate has passed already
     *
     * @param eventId
     * @return bool
     */

    public void isValidDate(String eventId, OnDateValidationListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("event").document(eventId);

        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    String closedate = document.getString("eventInfo.registrationCloseDate");
                    String date = document.getString("eventInfo.date");
                    if (closedate == null) {
                        Log.e("ValidDate", "Date is null for eventId: " + eventId);
                        listener.onResult(false); // Notify invalid result
                        return;
                    }

                    try {
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                        Date todaysDate = formatter.parse(formatter.format(new Date()));
                        Date eventcloseDate = formatter.parse(closedate);
                        Date eventregDate = formatter.parse(date);

                        boolean isAfter1 = todaysDate.after(eventcloseDate);
                        boolean isAfter2 = todaysDate.after(eventregDate);
                        Log.d("ValidDate", "Today's date: " + todaysDate + ", Event date: " + eventcloseDate);
                        //Log.d("ValidDate", "Is today's date after event date: " + isAfter);
                        boolean isAfter = isAfter1 || isAfter2;
                        Log.d("ValidDate", "Is today's date after event date: " + isAfter);
                        listener.onResult(isAfter); // Notify result of comparison
                    } catch (ParseException e) {
                        Log.e("ValidDate", "Error parsing date: " + e.getMessage());
                        listener.onResult(false); // Notify failure
                    }
                } else {
                    Log.e("ValidDate", "Document does not exist for eventId: " + eventId);
                    listener.onResult(false); // Notify failure
                }
            } else {
                Log.e("ValidDate", "Error fetching document: " + task.getException());
                listener.onResult(false); // Notify failure
            }
        });
    }




    /**
     * Fetch closing date from db related with registeration event id
     *
     * @param eventId
     * @return String closing_date
     */
//    private String fetchRegistrationClosingDate(String eventId) {
//
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        DocumentReference docRef = db.collection("event").document(eventId);
//
//        docRef.get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                DocumentSnapshot document = task.getResult();
//                if (document != null && document.exists()) {
//                    return document.getString("eventInfo.registrationCloseDate");
//
//                } else {
//                    Log.e("FetchDate", "Document does not exist for eventId: " + eventId);
//                }
//            } else {
//                Log.e("FetchDate", "Error fetching document: " + task.getException());
//            }
//        });
//    }



    /**
     * Add user to canceleled list
     */
    private void joinCancelList() {
        String userListID = eventID + "-cancelled";
        // Generate UserListDB to add new user to the specific user list event
        UserListDB userList = new UserListDB();
        userList.addToList(userListID, deviceID);

    }

    /**
     * Remove user from registered list
     */
    private void leaveRegisteredList() {
        String userListID = eventID + "-registered";
        // Generate UserListDB to add new user to the specific user list event
        UserListDB userList = new UserListDB();
        userList.removeFromList(userListID, deviceID);
        // Turn it into Leave Waiting List
        String joinText = "Event Left Successfully";
        eventButton.setText(joinText);
        eventButton.setBackgroundColor(ContextCompat.getColor(EventDetailsActivity.this, R.color.primaryGreyColor));
        eventButton.setEnabled(Boolean.FALSE);
    }

    /**
     * Remove user from draw list
     */
    private void leaveDrawList() {
        String userListID = eventID + "-draw";
        // Generate UserListDB to add new user to the specific user list event
        UserListDB userList = new UserListDB();
        userList.removeFromList(userListID, deviceID);

        declineButton.setBackgroundColor(ContextCompat.getColor(EventDetailsActivity.this, R.color.primaryGreyColor));
        eventButton.setBackgroundColor(ContextCompat.getColor(EventDetailsActivity.this, R.color.primaryGreyColor));
        eventButton.setEnabled(Boolean.FALSE);
        declineButton.setEnabled(Boolean.FALSE);
    }

    /**
     * Add user to registered list
     */
    private void joinRegisteredList() {
        String userListID = eventID + "-registered";
        // Generate UserListDB to add new user to the specific user list event
        UserListDB userList = new UserListDB();
        userList.addToList(userListID, deviceID);
        String joinText = "Invitation Accepted Successfully";
        eventButton.setText(joinText);
    }

    /**
     * Remove user from wait list
     */
    private void leaveWaitingList() {
        String userListID = eventID + "-wait";

        // Generate UserListDB to add new user to the specific user list event
        UserListDB userList = new UserListDB();
        userList.removeFromList(userListID, deviceID);

        // Turn it into Leave Waiting List
        String joinText = "Sucessfuly Left Waitlist";
        eventButton.setText(joinText);
        eventButton.setBackgroundColor(ContextCompat.getColor(EventDetailsActivity.this, R.color.primaryGreyColor));
        eventButton.setEnabled(Boolean.FALSE);
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
        String userListID = eventID + "-wait";

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
                ConfirmationDialog dialog = new ConfirmationDialog(EventDetailsActivity.this, EventDetailsActivity.this, "Event");
                dialog.showDialog();
            }
        });
    }

    private void deleteEvent() {
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

        ImageDB imageDB = new ImageDB();
        imageDB.delete(eventID);

        MapDB mapDB = new MapDB();
        mapDB.deleteMap(eventID);

        NotificationDB notificationDB = new NotificationDB();
        notificationDB.deleteNotificationsFromEvent(eventID);

        // deleting the event itself
        EventDB eventDB = new EventDB();
        eventDB.delete(eventID);
        Toast.makeText(EventDetailsActivity.this, "Event successfully deleted.", Toast.LENGTH_SHORT).show();

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        finish(); // returning to the previous screen
    }

    @Override
    public void deleteConfirmation(boolean confirmDelete, String deletedItem) {
        if (confirmDelete) {
            deleteEvent();
        } else {
            Toast.makeText(this, deletedItem + " deletion cancelled.", Toast.LENGTH_SHORT).show();
        }
    }
}