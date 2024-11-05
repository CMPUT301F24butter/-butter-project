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
 * A simple {@link Fragment} subclass.
 * Use the {@link EventsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ListView eventsList;

    private ArrayList<Event> userEvents;
    private EventArrayAdapter eventArrayAdapter;

    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference userRef;

    private FloatingActionButton fab;

    String deviceID;

    public EventsFragment() {
        userEvents = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("event"); // event collection
        userRef = db.collection("user");
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        Bundle args = getArguments();
        deviceID = args.getString("deviceID");

        eventArrayAdapter = new EventArrayAdapter(getContext(), userEvents);

        eventRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (querySnapshots != null) {
                    userEvents.clear();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        String organizerID = doc.getString("eventInfo.organizerID");

                        if (Objects.equals(organizerID, deviceID)) {
                            String eventID = doc.getId();
                            String eventName = doc.getString("eventInfo.name");
                            String eventDate = doc.getString("eventInfo.date");
                            String eventCapacityString = doc.getString("eventInfo.capacityString");

                            if (eventCapacityString != null) {
                                int eventCapacity = Integer.parseInt(eventCapacityString);
                                Event event = new Event(eventID, eventName, eventDate, eventCapacity);
                                userEvents.add(event);

                            } else {
                                Event event = new Event(eventID, eventName, eventDate, -1);
                                userEvents.add(event);
                            }
                        }
                    }
                    eventArrayAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        eventsList = (ListView) view.findViewById(R.id.events_list);
        eventsList.setAdapter(eventArrayAdapter);

        eventsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Event event = userEvents.get(position);
                //System.out.println(event.getEventID());

                String eventID = event.getEventID();
                Intent intent = new Intent(getContext(), EventDetailsActivity.class);
                intent.putExtra("deviceID", deviceID);
                intent.putExtra("eventID", eventID);
                startActivity(intent);
            }
        });

        fab = (FloatingActionButton) view.findViewById(R.id.addButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userRef.document(deviceID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            if (doc.exists()) {
                                String privileges = doc.getString("userInfo.privilegesString");
                                String facility = doc.getString("userInfo.facility");

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