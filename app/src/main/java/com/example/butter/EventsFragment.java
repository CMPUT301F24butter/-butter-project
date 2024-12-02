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
     * Reference to the image collection in the database.
     */
    private CollectionReference imageRef;
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
        imageRef = db.collection("image"); // image collection
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
    }

    /**
     * Updates the displayed list of events
     */
    // method to update the displayed list of events
    private void updateEventList(QuerySnapshot querySnapshots) {
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

                    Event event;
                    if (eventCapacityString != null) {
                        int eventCapacity = Integer.parseInt(eventCapacityString);
                        event = new Event(eventID, eventName, eventDate, eventCapacity);

                    } else {
                        event = new Event(eventID, eventName, eventDate, -1);
                    }

                    // fetching a potential image associated with this event
                    imageRef.document(eventID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> imageTask) {
                            if (imageTask.isSuccessful()) {
                                DocumentSnapshot imageDoc = imageTask.getResult();
                                if (imageDoc.exists()) { // if there is an image for this event
                                    String base64string = imageDoc.getString("imageData");
                                    event.setImageString(base64string); // setting this attribute for the Event object
                                }
                                userEvents.add(event);
                            }
                            eventArrayAdapter.notifyDataSetChanged(); // notifying a change in the list
                        }
                    });
                }
            }
        }
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

                Intent intent = new Intent(getContext(), CreateEventFragment.class);
                intent.putExtra("deviceID", deviceID);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        userEvents.clear(); // clearing the list
        eventArrayAdapter.notifyDataSetChanged();

        eventRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                updateEventList(querySnapshot); // updating the displayed event list everytime this screen enters the foreground
            }
        });
    }
}
