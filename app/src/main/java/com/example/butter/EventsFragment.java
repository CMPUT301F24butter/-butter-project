package com.example.butter;

import android.content.ContentResolver;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
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

    String deviceID;

    public EventsFragment() {
        userEvents = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("event"); // event collection
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
                            String eventName = doc.getString("eventInfo.name");
                            String eventDate = doc.getString("eventInfo.date");
                            int eventCapacity = Integer.parseInt(doc.getString("eventInfo.capacityString"));

                            Event event = new Event(eventName, eventDate, eventCapacity);
                            userEvents.add(event);
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

        return view;

        //return inflater.inflate(R.layout.fragment_events, container, false);
    }
}
