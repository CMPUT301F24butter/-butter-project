package com.example.butter;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * This fragment is used to set up the home screen on the entrants / entrants + organizers side. As
 * an entrant, the user only has access to the entrants screen and won't be able to see the admins
 * spinner.
 * Outstanding Issue: Only shows three events at a time in waiting list and upcoming list.
 * @author DbugDiver
 */

public class HomeEntrantFragment extends Fragment {
    private FirebaseFirestore db;

    // Entrant waiting and upcoming recycler view
    private RecyclerView eventsRecyclerView;
    private RecyclerView waitingListRecyclerView;
    private HomeAdapter upcomingAdapter;
    private HomeAdapter waitingListAdapter;
    private ListenerRegistration upcomingListener;
    private ListenerRegistration waitingListener;
    private final String deviceID;

    public HomeEntrantFragment(String deviceID) {
        this.deviceID = deviceID;
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_entrant, container, false);

        // Setup RecyclerView for upcoming events
        eventsRecyclerView = view.findViewById(R.id.eventsRecyclerView);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        upcomingAdapter = new HomeAdapter(new ArrayList<>());
        eventsRecyclerView.setAdapter(upcomingAdapter);

        // Setup RecyclerView for waiting list
        waitingListRecyclerView = view.findViewById(R.id.waitingListRecyclerView);
        waitingListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        waitingListAdapter = new HomeAdapter(new ArrayList<>());
        waitingListRecyclerView.setAdapter(waitingListAdapter);

        fetchUpcomingEvents();
        fetchWaitingList();

        return view;
    }

    private void fetchUpcomingEvents() {
        upcomingListener = db.collection("userList")
                .whereEqualTo("type", "registered")
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        System.err.println("Listen failed: " + error);
                        return;
                    }

                    List<Event> upcomingEvents = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        int size = document.contains("size") ? Integer.parseInt(document.getString("size")) : 0;

                        if (size > 0) {
                            for (int i = 0; i < size; i++) {
                                String userKey = "user" + i;
                                String userId = document.getString(userKey);

                                if (deviceID.equals(userId)) {
                                    String fullDocumentId = document.getId();
                                    String eventId = fullDocumentId.contains("-")
                                            ? fullDocumentId.substring(0, fullDocumentId.lastIndexOf("-"))
                                            : fullDocumentId;

                                    db.collection("event").document(eventId)
                                            .get()
                                            .addOnSuccessListener(eventDocument -> {
                                                if (eventDocument.exists()) {
                                                    String name = eventDocument.getString("eventInfo.name");
                                                    String date = eventDocument.getString("eventInfo.date");
                                                    int capacity = 0;
                                                    String capacityString = eventDocument.getString("eventInfo.capacityString");
                                                    if (capacityString != null) {
                                                        capacity = Integer.parseInt(capacityString);
                                                    }

                                                    Event event = new Event(eventId, name, date, capacity);
                                                    upcomingEvents.add(event);
                                                    Log.d("HomeFragment", "Upcoming events list size: " + upcomingEvents.size());

                                                    upcomingAdapter.setItemList(upcomingEvents);
                                                    upcomingAdapter.notifyDataSetChanged();
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                System.err.println("Error fetching event details: " + e.getMessage());
                                            });
                                }
                            }
                        }
                    }
                });
    }

    private void fetchWaitingList() {
        waitingListener = db.collection("userList")
                .whereEqualTo("type", "waitlist")
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        System.err.println("Listen failed: " + error);
                        return;
                    }

                    List<Event> waitingEvents = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        int size = document.contains("size") ? Integer.parseInt(document.getString("size")) : 0;

                        if (size > 0) {
                            for (int i = 0; i < size; i++) {
                                String userKey = "user" + i;
                                String userId = document.getString(userKey);

                                if (deviceID.equals(userId)) {
                                    String fullDocumentId = document.getId();
                                    String eventId = fullDocumentId.contains("-")
                                            ? fullDocumentId.substring(0, fullDocumentId.lastIndexOf("-"))
                                            : fullDocumentId;

                                    db.collection("event").document(eventId)
                                            .get()
                                            .addOnSuccessListener(eventDocument -> {
                                                if (eventDocument.exists()) {
                                                    String name = eventDocument.getString("eventInfo.name");
                                                    String date = eventDocument.getString("eventInfo.date");
                                                    int capacity = 0;
                                                    String capacityString = eventDocument.getString("eventInfo.capacityString");
                                                    if (capacityString != null) {
                                                        capacity = Integer.parseInt(capacityString);
                                                    }

                                                    Event event = new Event(eventId, name, date, capacity);
                                                    waitingEvents.add(event);
                                                    Log.d("HomeFragment", "Waiting events list size: " + waitingEvents.size());


                                                    waitingListAdapter.setItemList(waitingEvents);
                                                    waitingListAdapter.notifyDataSetChanged();
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                System.err.println("Error fetching event details: " + e.getMessage());
                                            });
                                }
                            }
                        }
                    }
                });

    }
}