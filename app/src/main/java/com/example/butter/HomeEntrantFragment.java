package com.example.butter;

import android.content.Intent;
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
    private RecyclerView drawListRecyclerView;

    private HomeAdapter upcomingAdapter;
    private HomeAdapter waitingListAdapter;
    private HomeAdapter drawListAdapter;

    private ListenerRegistration upcomingListener;
    private ListenerRegistration waitingListener;
    private ListenerRegistration drawListener;

    private final String deviceID;
    private CollectionReference imageRef;


    public HomeEntrantFragment(String deviceID) {
        this.deviceID = deviceID;
        db = FirebaseFirestore.getInstance();
        imageRef = db.collection("image");
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
        // Setup on click listener for upcoming list
        upcomingAdapter.setOnItemClickListener((event, position) -> {

            Log.d("HomeEntrantFragment", "Clicked Upcoming Event: " + event.getName() + " at position " + position);

            handleEventClick(event, "registered");

        });

        // Setup RecyclerView for waiting list
        waitingListRecyclerView = view.findViewById(R.id.waitingListRecyclerView);
        waitingListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        waitingListAdapter = new HomeAdapter(new ArrayList<>());
        waitingListRecyclerView.setAdapter(waitingListAdapter);
        waitingListAdapter.setOnItemClickListener((event, position) -> {

            Log.d("HomeEntrantFragment", "Clicked Waiting List Event: " + event.getName() + " at position " + position);

            handleEventClick(event, "wait");

        });

        // setup RecyclerView for invited events
        drawListRecyclerView = view.findViewById(R.id.drawListRecyclerView);
        drawListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        drawListAdapter = new HomeAdapter(new ArrayList<>());
        drawListRecyclerView.setAdapter(drawListAdapter);
        // Set item click listener for upcoming events
        drawListAdapter.setOnItemClickListener((event, position) -> {

            Log.d("HomeEntrantFragment", "Clicked Draw Event: " + event.getName() + " at position " + position);

            handleEventClick(event, "draw");

        });
        imageRef = db.collection("image");
        fetchEvents(upcomingAdapter, upcomingListener, "registered");
        fetchEvents(waitingListAdapter, waitingListener, "waitlist");
        fetchEvents(drawListAdapter, drawListener, "draw");



        return view;
    }

    private void fetchEvents(HomeAdapter adapter, ListenerRegistration listener, String listtype) {
        listener = db.collection("userList")
                .whereEqualTo("type", listtype)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        System.err.println("Listen failed: " + error);
                        return;
                    }

                    // Clear the adapter if there are no documents
                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        adapter.clearItems(); // Clear the RecyclerView
                        return;
                    }

                    List<Event> listEvents = new ArrayList<>();

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
                                                    fetchImage(event, eventId, listEvents, adapter);
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                System.err.println("Error fetching event details: " + e.getMessage());
                                            });
                                }
                            }
                        }
                    }

                    // Final check: If no events were added to the list, clear the adapter
                    if (listEvents.isEmpty()) {
                        adapter.clearItems();
                    }
                });
    }


    private void fetchImage(Event event, String eventId, List<Event> listEvents, HomeAdapter adapter) {
        db.collection("image").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve the image string from the document
                        String imageString = documentSnapshot.getString("imageData");

                        // Set the image string to the Event object
                        if (imageString != null) {
                            event.setImageString(imageString);
                        } else {
                            Log.e("Firestore", "No image data found for eventId: " + eventId);
                        }
                    } else {
                        Log.e("Firestore", "Document does not exist for eventId: " + eventId);
                    }
                    // Add event to the list and notify adapter only after the image is fetched
                    listEvents.add(event);
                    adapter.setItemList(listEvents);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching image for eventId: " + eventId + " - " + e.getMessage());
                });
    }



    private void handleEventClick(Event event, String listType) {
        // Handle the clicked event
        Log.d("HomeEntrantFragment", "Handling " + listType + " Event: " + event.getName());

        // Example: Navigate to an event details screen
        Intent intent = new Intent(getContext(), EventDetailsActivity.class);
        intent.putExtra("deviceID", deviceID);
        intent.putExtra("eventID", event.getEventID());
        intent.putExtra("listType", listType);
        startActivity(intent);
    }
}
