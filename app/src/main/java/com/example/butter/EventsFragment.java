package com.example.butter;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

/**
 * This is the main "Events" screen
 * On this screen, all the events published by the logged in user will be displayed
 * This fragment is called to from {@link MainActivity} as one of the options on the bottom bar.
 * Users have the ability to click on these events, as well as add new events by clicking the add button in the corner
 * Current outstanding issues: need to implement poster images
 *
 * @author Nate Pane (natepane)
 */
public class EventsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * A ListView object containing all of the events for the corresponding user as views.
     */
    private ListView eventsList;

    /**
     * An ArrayList containing all of the user events.
     */
    private ArrayList<Event> userEvents;
    /**
     * An ArrayAdapter for managing the userEvents ArrayList.
     */
    private EventArrayAdapter eventArrayAdapter;
    /**
     * Database object for references to the database.
     */
    private FirebaseFirestore db;
    /**
     * Reference to the event collection in the database.
     */
    private CollectionReference eventRef;
    /**
     * Reference to the user collection in the database.
     */
    private CollectionReference userRef;
    /**
     * Adding a new event button.
     */
    private FloatingActionButton fab;
    /**
     * deviceID for the corresponding user. For querying the database.
     */
    String deviceID;

    /**
     * Constructor to setup database and collection references from the database.
     */
    public EventsFragment() {
        userEvents = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("event"); // event collection
        userRef = db.collection("user"); // user collection
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EventsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EventsFragment newInstance(String param1, String param2) {
        EventsFragment fragment = new EventsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * onCreate sets up the eventArrayAdapter by grabbing each event corresponding to the deviceID of the user.
     * Simply grabs the events from the database and updates for new ones using the addSnapshotListener.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        Bundle args = getArguments();
        deviceID = args.getString("deviceID"); // logged in deviceID

        eventArrayAdapter = new EventArrayAdapter(getContext(), userEvents);

        // retrieving all event info from the database
        eventRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (querySnapshots != null) {
                    userEvents.clear(); // clearing the userEvents array
                    for (QueryDocumentSnapshot doc : querySnapshots) { // iterating over all events in the database
                        String organizerID = doc.getString("eventInfo.organizerID");

                        if (Objects.equals(organizerID, deviceID)) { // if this event is published by the logged in organizer
                            // retrieving event details
                            String eventID = doc.getId();
                            String eventName = doc.getString("eventInfo.name");
                            String eventDate = doc.getString("eventInfo.date");
                            String eventCapacityString = doc.getString("eventInfo.capacityString");

                            if (eventCapacityString != null) {
                                int eventCapacity = Integer.parseInt(eventCapacityString);
                                Event event = new Event(eventID, eventName, eventDate, eventCapacity);
                                userEvents.add(event); // adding the event to the list

                            } else {
                                Event event = new Event(eventID, eventName, eventDate, -1);
                                userEvents.add(event); // adding the event to the list
                            }
                        }
                    }
                    eventArrayAdapter.notifyDataSetChanged(); // notifying a change in the list
                }
            }
        });
    }

    /**
     * onCreateView simply sets up the view for all of the events in eventsList.
     * More specifically, an onClickListener is setup for each of the events in the list,
     * so once a specific event is clicked on, details will be shown for said event.
     * Once all listeners are setup for each event AND for the create a new event button,
     * then the view is returned as a fragment for MainActivity to show.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        eventsList = (ListView) view.findViewById(R.id.events_list);
        eventsList.setAdapter(eventArrayAdapter);

        // setting a click listener for each event in the list
        eventsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Event event = userEvents.get(position);
                String eventID = event.getEventID();
                Intent intent = new Intent(getContext(), EventDetailsActivity.class);
                intent.putExtra("deviceID", deviceID);
                intent.putExtra("eventID", eventID);
                startActivity(intent);
            }
        });

        // setting an event listener for the add event button
        fab = (FloatingActionButton) view.findViewById(R.id.addButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // first, retrieving user info for the logged in user
                userRef.document(deviceID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            if (doc.exists()) {
                                String privileges = doc.getString("userInfo.privilegesString");
                                String facility = doc.getString("userInfo.facility");

                                // checking that the user has organizational privileges before allowing them to add an event
                                if (Objects.equals(privileges, "200") || Objects.equals(privileges, "300") || Objects.equals(privileges, "600") || Objects.equals(privileges, "700")) {
                                    if (facility != null) {
                                        Intent intent = new Intent(getContext(), CreateEventFragment.class);
                                        intent.putExtra("deviceID", deviceID);
                                        startActivity(intent);
                                    }
                                }
                            }
                        }
                    }
                });
            }
        });

        return view;
    }
}
