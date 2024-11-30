package com.example.butter;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;



import java.util.HashMap;
import java.util.Objects;

/**
 * This activity shows the details of an event when it is clicked on from the "Events" screen
 * It also includes buttons for more organizer options (e.g. edit event, see QR Code, etc.), as well as a button to delete the event
 *
 * Current outstanding issues: need to implement poster images
 *
 * @author Nate Pane (natepane) and Angela Dakay (angelcache)
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
    private Boolean adminBrowsing;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private Button eventButton;
    private Button declineButton;

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
        adminBrowsing = getIntent().getExtras().getBoolean("adminBrowsing", Boolean.FALSE); // Default to false if not found

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
                    setUpEntrantActions();
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
        if (adminBrowsing) {
            eventButton.setVisibility(View.INVISIBLE);
        } else {
            setUpEntrantActions();
        }
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
        eventButton.setVisibility(View.VISIBLE);
        String userListID  = eventID + "-" + listType;
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
                            String leaveText ="";
                            if (listType.equals("wait")) {
                                leaveText = "Leave Waiting List";
                                eventButton.setBackgroundColor(ContextCompat.getColor(EventDetailsActivity.this, R.color.primaryGreyColor));

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
                }
                else if (eventButton.getText() == "Leave Event") {
                    leaveRegisteredList();
                    joinCancelList();

                } else {
                    leaveWaitingList();
                }



            }

        });

    }



    private void joinCancelList() {
        String userListID  = eventID + "-cancelled";
        // Generate UserListDB to add new user to the specific user list event
        UserListDB userList = new UserListDB();
        userList.addToList(userListID, deviceID);

    }



    private void leaveRegisteredList() {
        String userListID  = eventID + "-registered";
        // Generate UserListDB to add new user to the specific user list event
        UserListDB userList = new UserListDB();
        userList.removeFromList(userListID, deviceID);
        // Turn it into Leave Waiting List
        String joinText = "Event Left Successfully";
        eventButton.setText(joinText);
        eventButton.setBackgroundColor(ContextCompat.getColor(EventDetailsActivity.this, R.color.primaryGreyColor));
        eventButton.setEnabled(Boolean.FALSE);
    }



    private void leaveDrawList() {
        String userListID  = eventID + "-draw";
        // Generate UserListDB to add new user to the specific user list event
        UserListDB userList = new UserListDB();
        userList.removeFromList(userListID, deviceID);

        declineButton.setBackgroundColor(ContextCompat.getColor(EventDetailsActivity.this, R.color.primaryGreyColor));
        eventButton.setBackgroundColor(ContextCompat.getColor(EventDetailsActivity.this, R.color.primaryGreyColor));
        eventButton.setEnabled(Boolean.FALSE);
        declineButton.setEnabled(Boolean.FALSE);
    }


    private void joinRegisteredList() {
        String userListID  = eventID + "-registered";
        // Generate UserListDB to add new user to the specific user list event
        UserListDB userList = new UserListDB();
        userList.addToList(userListID, deviceID);
        String joinText = "Invitation Accepted Successfully";
        eventButton.setText(joinText);
    }

    private void leaveWaitingList() {
        String userListID  = eventID + "-wait";

        // Generate UserListDB to add new user to the specific user list event
        UserListDB userList = new UserListDB();
        userList.removeFromList(userListID, deviceID);

        // Turn it into Leave Waiting List
        String joinText = "Successfully Left Waitlist";
        eventButton.setText(joinText);
        eventButton.setBackgroundColor(ContextCompat.getColor(EventDetailsActivity.this, R.color.primaryGreyColor));
        eventButton.setEnabled(Boolean.FALSE);
    }

    @Override
    public void onJoinEventConfirmed(boolean confirmJoin) {
        if (confirmJoin) {
            // add geolocation to
            // Global var for eventID, and for deviceID
//            float lat =
//            Location location getLocation();
//            location.getLatitude()
            // get location
            // init a map database object
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            // Check and request location permissions
            checkPermissionsAndGetLocation();
            MapDB mapDB = new MapDB();

            // now add to database
            // (eventID, userID, Lat, Long)
//            mapDB.addLocation();










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
        eventButton.setVisibility(View.VISIBLE);
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
    private void checkPermissionsAndGetLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, proceed to get location
            getLatLong();
        } else {
            // Request permission if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed to get location
                getLatLong();
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Permission denied. Unable to get location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Get Lat Long
    private void getLatLong() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // Location successfully retrieved
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            MapDB mapDB = new MapDB();
                            mapDB.addLocation(eventID, deviceID, latitude, longitude);
                            // Create a GeoPoint from the location
//                            GeoPoint geoPoint = new GeoPoint(latitude, longitude);

                            // Create a marker
//                            Marker userMarker = new Marker(map);
//                            userMarker.setPosition(geoPoint);
//                            userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//                            IMapController mapController = map.getController();
//                            map.getOverlays().add(userMarker);
//                            userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//                            mapController.animateTo(geoPoint);
//                            mapController.setZoom((long) 17);


                        }
                    }
                });
    }
}

