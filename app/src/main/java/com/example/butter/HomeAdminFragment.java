package com.example.butter;

import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * This fragment is used to set up the home screen on the admins side. Admins have access to a spinner
 * and can also access entrants page.
 * Outstanding Issue: When I move to another screen and come back to the admin screen,
 *               the spinner goes back to default option (browse events) instead of the
 *               option user last clicked on
 *
 * @author Angela Dakay (angelcache)
 */

public class HomeAdminFragment extends Fragment implements ConfirmationDialog.ConfirmationDialogListener {
    // Need access to all events, users, posters + device ID
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference userRef;
    private CollectionReference QRCodeRef;

    // Lists of events, users, and posters
    private ArrayList<Event> allEvents;
    private ArrayList<User> allUsers;
    private ArrayList<User> allFacilities;
    private ArrayList<String> allImages;
    private ArrayList<String> allImagesEventID; // References the event ID image is attached to
    private ListView adminListView;
    private EventArrayAdapter eventArrayAdapter;
    private UserArrayAdapter profileArrayAdapter;
    private UserArrayAdapter facilitiesArrayAdapter;
    private ImagesArrayAdapter QRCodeArrayAdapter;
    private Boolean isFacility;
    private String browse;
    private String deviceID;
    private FloatingActionButton deleteButton;
    User selectedOrganizer;
    String selectedImageEvent;

    /**
     * Constructor for HomeAdminFragment, initializes array lists and reference to database
     * @param browse from HomeFragment, tells us what the spinner is currently on
     * @param deviceID the ID of the user
     */
    public HomeAdminFragment(String browse, String deviceID) {
        allEvents = new ArrayList<>();
        allUsers = new ArrayList<>();
        allFacilities = new ArrayList<>();
        allImages = new ArrayList<>();
        allImagesEventID = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("event"); // event collection
        userRef = db.collection("user"); // user collection
        QRCodeRef = db.collection("QRCode");
        this.browse = browse;
        this.deviceID = deviceID;
    }

    /**
     * In the OnCreateView, the adapters for events, profile, facilities, and images are initialized
     * and set up. Admin List View is set up with the initial adapter, among the four depending on
     * the browse variable (this is determined by the spinner).
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     */
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the fragment's layout
        View view = inflater.inflate(R.layout.fragment_home_admin, container, false);
        adminListView = (ListView) view.findViewById(R.id.admin_list_view);
        deleteButton = view.findViewById(R.id.delete_admin_button);

        eventArrayAdapter = new EventArrayAdapter(getContext(), allEvents);
        profileArrayAdapter = new UserArrayAdapter(getContext(), allUsers, Boolean.FALSE);
        facilitiesArrayAdapter = new UserArrayAdapter(getContext(), allFacilities, Boolean.TRUE);
        QRCodeArrayAdapter = new ImagesArrayAdapter(getContext(), allImages, allImagesEventID);

        // Used to set the right adapter when the user changes the spinner option
        switch (browse) {
            case "Browse Events":
                adminListView.setAdapter(eventArrayAdapter);
                break;
            case "Browse Facilities":
                adminListView.setAdapter(facilitiesArrayAdapter);
                deleteButton.setVisibility(VISIBLE);
                break;
            case "Browse Profiles":
                adminListView.setAdapter(profileArrayAdapter);
                break;
            case "Browse Event Posters":
                adminListView.setAdapter(null);
                break;
            case "Browse QR Codes":
                adminListView.setAdapter(QRCodeArrayAdapter);
                deleteButton.setVisibility(VISIBLE);
                break;
        }

        // Used to delete Organizer, QR Code or Posters
        adminListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                adminListView.setItemChecked(position, true);

                if (browse.equals("Browse Events")) {
                    String selectedEventID = allEvents.get(position).getEventID();
                    Intent intent = new Intent(getContext(), EventDetailsActivity.class);
                    intent.putExtra("deviceID", deviceID);
                    intent.putExtra("eventID", selectedEventID);
                    intent.putExtra("adminPrivilege", Boolean.TRUE); // User has admin priviliges, used in eventDetailsActivity for special priviliges
                    startActivity(intent);
                } else if (browse.equals("Browse Facilities")) {

                    selectedOrganizer = allFacilities.get(position);
                } else if (browse.equals("Browse QR Codes")) {
                    selectedImageEvent = allImagesEventID.get(position);
                }
            }
        });

        // Sets up the delete facility button
        deleteButton();

        return view;
    }

    /**
     * Deletes Selected QR Code
     */
    private void deleteSelectedQRCode() {
        if (selectedImageEvent != null) {

            int QRIndex = allImagesEventID.indexOf(selectedImageEvent);

            QRCodeDB QRCode = new QRCodeDB();
            QRCode.delete(selectedImageEvent);

            // Remove the QR Code from the list and notifies adapter
            allImages.remove(QRIndex);
            allImagesEventID.remove(QRIndex);
            QRCodeArrayAdapter.notifyDataSetChanged();
            selectedImageEvent = null;

            Toast.makeText(getContext(), "The QR code has been successfully deleted.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Deletes Selected Facility -- takes into account the users privileges
     * If a user is an admin and organizer, they will become only an admin
     * IF a user is an organizer only or both organizer and entrant, they will become only an entrant
     */
    private void deleteSelectedFacility() {
        if (selectedOrganizer != null) { // Check if a user is selected
            UserDB userDB = new UserDB();

            // Removes the facility from the list and notifies adapter
            allFacilities.remove(selectedOrganizer);
            facilitiesArrayAdapter.notifyDataSetChanged();

            // keep deleted facility to use in a toast
            String deletedFacility = selectedOrganizer.getFacility();

            // Nullify the user's facility
            selectedOrganizer.setFacility(null);

            int privilege = selectedOrganizer.getPrivileges();

            if (privilege == 600 || privilege == 700) { // If they are an admin + organizer, they will become admin only
                selectedOrganizer.setPrivileges(400);
            } else {
                selectedOrganizer.setPrivileges(100); // Organizer + entrant / organizer only will become entrant only
            }

            // Update the user in the database
            try {
                userDB.update(selectedOrganizer);
            } catch (Exception e) {
                Log.e("DatabaseError", "Failed to update user: " + selectedOrganizer.getDeviceID(), e);
                return; // Exit if the update fails
            }

            // Clear selection and refresh the list
            selectedOrganizer = null;

            Toast.makeText(getContext(), deletedFacility + " deleted.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * When a user comes back from entrants detail activity, it ensures that the events list will be
     * refreshed, showing up to date data in case the user deleted the event.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (browse.equals("Browse Events")) {
            showEventsList(); // Refresh the event list whenever the fragment is resumed
        }
    }

    /**
     * When the delete button is clicked, it sets deleteButtonClicked to be true, which will then
     * set up the user's ability to delete either a facility, an event poster, or a QR Code they
     * click on.
     */
    private void deleteButton() {
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (browse.equals("Browse Facilities")) {
                    ConfirmationDialog dialog = new ConfirmationDialog(getContext(), HomeAdminFragment.this, "Facility");
                    dialog.showDialog();
                } else if (browse.equals("Browse QR Codes")) {
                    ConfirmationDialog dialog = new ConfirmationDialog(getContext(), HomeAdminFragment.this, "QR Code");
                    dialog.showDialog();
                }
            }
        });
    }

    /**
     * Show Events List method: populates the admin list with all events.
     */
    public void showEventsList() {
        deleteButton.setVisibility(View.INVISIBLE);
        adminListView.setAdapter(eventArrayAdapter);
        eventRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    allEvents.clear();
                    for (DocumentSnapshot doc : task.getResult()) {
                        String eventID = doc.getId();
                        String eventName = doc.getString("eventInfo.name");
                        String eventDate = doc.getString("eventInfo.date");
                        String eventCapacityString = doc.getString("eventInfo.capacityString");

                        if (eventCapacityString != null) {
                            int eventCapacity = Integer.parseInt(eventCapacityString);
                            Event event = new Event(eventID, eventName, eventDate, eventCapacity);
                            allEvents.add(event);

                        } else {
                            Event event = new Event(eventID, eventName, eventDate, -1);
                            allEvents.add(event);
                        }
                    }
                    eventArrayAdapter.notifyDataSetChanged();
                } else {
                    Log.d("Firebase", "Error getting documents: ", task.getException());
                }
            }

        });
    }

    /**
     * Show Profiles List method: populates the admin list with all user profiles
     */
    public void showProfilesList() {
        deleteButton.setVisibility(View.INVISIBLE);
        adminListView.setAdapter(profileArrayAdapter);
        userRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    allUsers.clear();
                    for (DocumentSnapshot doc : task.getResult()) {
                        String deviceID = doc.getString("userInfo.deviceID");
                        String email = doc.getString("userInfo.email");
                        String facility = doc.getString("userInfo.facility");
                        String name = doc.getString("userInfo.name");
                        String phone = doc.getString("userInfo.phoneNumber");
                        int privileges = Integer.parseInt(doc.getString("userInfo.privilegesString"));

                        User user = new User(deviceID, name, privileges, facility, email, phone);
                        allUsers.add(user);
                    }
                    profileArrayAdapter.notifyDataSetChanged();
                } else {
                    Log.d("Firebase", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    /**
     * Show Facilities List method: populates the admin list with all user facilities
     */
    public void showFacilitiesList() {
        deleteButton.setVisibility(VISIBLE);
        adminListView.setAdapter(facilitiesArrayAdapter);
        userRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    allFacilities.clear();
                    for (DocumentSnapshot doc : task.getResult()) {
                        String deviceID = doc.getString("userInfo.deviceID");
                        String email = doc.getString("userInfo.email");
                        String facility = doc.getString("userInfo.facility");
                        String name = doc.getString("userInfo.name");
                        String phone = doc.getString("userInfo.phoneNumber");
                        int privileges = Integer.parseInt(doc.getString("userInfo.privilegesString"));

                        User user = new User(deviceID, name, privileges, facility, email, phone);

                        if (facility != null) {
                            allFacilities.add(user);
                        }
                    }
                    facilitiesArrayAdapter.notifyDataSetChanged();
                } else {
                    Log.d("Firebase", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    /**
     * Show Posters List method: populates the admin list with event posters
     */
    private void showPostersList() {
        adminListView.setAdapter(null);
        deleteButton.setVisibility(View.INVISIBLE);
    }

    /**
     * Show QR Codes List method: populates the admin list with all event QR Codes
     */
    public void showQRCodesList() {
        deleteButton.setVisibility(VISIBLE);
        adminListView.setAdapter(QRCodeArrayAdapter);

        // fetching the QR Code associated to this eventID from firebase
        QRCodeRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    allImages.clear();
                    for (DocumentSnapshot doc : task.getResult()) {
                        String QRCodeString = doc.getString("QRCodeString");
                        String QRCodeEvent = doc.getId(); // Gets the name of doc which is the event id
                        if (QRCodeString != null) {
                            allImages.add(QRCodeString);
                            allImagesEventID.add(QRCodeEvent);
                        }
                    }
                    QRCodeArrayAdapter.notifyDataSetChanged();
                } else {
                    Log.d("Firebase", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    /**
     * Used by HomeFragment to notify HomeAdminFragment about changes in the spinner the browse
     * variable keeps track of changes.
     * @param browse taken from spinner in Home Adapter, depending on changes in browse, admins list
     *               view adapter and items will change.
     */
    public void spinnerBrowseChange(String browse) {
        this.browse = browse;
        switch (browse) {
            case "Browse Events":
                showEventsList();
                break;
            case "Browse Facilities":
                showFacilitiesList();
                break;
            case "Browse Profiles":
                showProfilesList();
                break;
            case "Browse Event Posters":
                showPostersList();
                break;
            case "Browse QR Codes":
                showQRCodesList();;
                break;
        }
    }

    /**
     * Confirming if the user wants to continue with the deletion, communication between
     * HomeAdminFragment and ConfirmationDialog
     * @param confirmDelete boolean confirms if user wants to delete
     * @param deletedItem has value "Event"
     */
    @Override
    public void deleteConfirmation(boolean confirmDelete, String deletedItem) {
        if (confirmDelete) {
            switch (deletedItem){
                case "Facility":
                    deleteSelectedFacility();
                    break;
                case "QR Code":
                    deleteSelectedQRCode();
                    break;
                case "Event Poster":
                    break;
            }


        } else {
            Toast.makeText(getContext(), deletedItem + " deletion cancelled.", Toast.LENGTH_SHORT).show();
        }
    }
}