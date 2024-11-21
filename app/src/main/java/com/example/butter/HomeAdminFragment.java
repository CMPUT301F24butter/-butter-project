package com.example.butter;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
 * @author Angela Dakay (angelcache)
 */

public class HomeAdminFragment extends Fragment {
    // Need access to all events, users, posters + device ID
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference userRef;

    // Lists of events, users, and posters
    private ArrayList<Event> allEvents;
    private ArrayList<User> allUsers;
    private ArrayList<User> allFacilities;
    private ListView adminListView;
    private EventArrayAdapter eventArrayAdapter;
    private UserArrayAdapter profileArrayAdapter;
    private UserArrayAdapter facilitiesArrayAdapter;
    private Boolean isFacility;
    private String browse;

    public HomeAdminFragment(String browse) {
        allEvents = new ArrayList<>();
        allUsers = new ArrayList<>();
        allFacilities = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("event"); // event collection
        userRef = db.collection("user"); // user collection
        this.browse = browse;
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

        eventArrayAdapter = new EventArrayAdapter(getContext(), allEvents);
        profileArrayAdapter = new UserArrayAdapter(getContext(), allUsers, Boolean.FALSE);
        facilitiesArrayAdapter = new UserArrayAdapter(getContext(), allFacilities, Boolean.TRUE);

        switch (browse) {
            case "Browse Events":
                adminListView.setAdapter(eventArrayAdapter);
                break;
            case "Browse Facilities":
                adminListView.setAdapter(facilitiesArrayAdapter);
                break;
            case "Browse Profiles":
                adminListView.setAdapter(profileArrayAdapter);
        }

        return view;
    }

    /**
     * Show Events List method: populates the admin list with all events.
     */
    public void showEventsList() {
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
     * Show Images List method: populates the admin list with all user facilities
     */
    public void showImagesList() {
        adminListView.setAdapter(null);
    }

    /**
     * Changes the spinner depending on the changes of the spinner in Home Fragment
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
            case "Browse Images":
                showImagesList();
        }
    }
}
