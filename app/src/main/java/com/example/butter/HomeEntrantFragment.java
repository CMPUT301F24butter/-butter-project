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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This fragment is used to set up the home screen on the entrants / entrants + organizers side. As
 * an entrant, the user only has access to the entrants screen and won't be able to see the admins
 * spinner.
 * Outstanding Issue: Only shows three events at a time in waiting list and upcoming list.
 * @author DbugDiver(bparkash)
 */

public class HomeEntrantFragment extends Fragment {
    private FirebaseFirestore db;
    private Event event;
    private String userId;

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
    @Override
    public void onResume() {
        super.onResume();

        // Log to confirm the method is called
        Log.d("HomeEntrantFragment", "onResume: Clearing all lists and refreshing data");

        // Clear all RecyclerView adapters
        if (upcomingAdapter != null) {
            upcomingAdapter.clearItems();
            upcomingAdapter.notifyDataSetChanged();
        }

        if (waitingListAdapter != null) {
            waitingListAdapter.clearItems();
            waitingListAdapter.notifyDataSetChanged();
        }

        if (drawListAdapter != null) {
            drawListAdapter.clearItems();
            drawListAdapter.notifyDataSetChanged();
        }

        // Re-fetch the events to refresh the lists
        fetchEvents(upcomingAdapter, upcomingListener, "registered");
        fetchEvents(waitingListAdapter, waitingListener, "waitlist");
        fetchEvents(drawListAdapter, drawListener, "draw");
    }


    /**
     *Fetch all events for device id and add them to upcoming, registration or invitation list
     * @param adapter, listener, listtype
     *      New Event object to be added to the event collection
     */
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
                                userId = document.getString(userKey);

                                if (deviceID.equals(userId)) {

                                    String fullDocumentId = document.getId();
                                    String eventId = fullDocumentId.contains("-")
                                            ? fullDocumentId.substring(0, fullDocumentId.lastIndexOf("-"))
                                            : fullDocumentId;

                                    // Add a listener on the event document
                                    db.collection("event").document(eventId)
                                            .addSnapshotListener((eventDocument, eventError) -> {
                                                if (eventError != null) {
                                                    Log.e("FetchEvents", "Error listening to event: " + eventError.getMessage());
                                                    return;
                                                }
                                                if (eventDocument.exists()) {
                                                    String name = eventDocument.getString("eventInfo.name");
                                                    String date = eventDocument.getString("eventInfo.date");
                                                    String closedate = eventDocument.getString("eventInfo.registrationCloseDate");
                                                    int capacity = 0;
                                                    String capacityString = eventDocument.getString("eventInfo.capacityString");
                                                    if (capacityString != null) {
                                                        capacity = Integer.parseInt(capacityString);
                                                    }

                                                    event = new Event(eventId, name, date, capacity);
                                                    event.setRegistrationCloseDate(closedate);
                                                    if (isValidDate(event,listtype)) {
                                                        // Add valid events to the list
                                                        String userListID = eventId + "-" + (listtype.equals("waitlist") ? "wait" : listtype);
                                                        Log.d("UserListID", "Processing userListID: " + userListID);
                                                        //removeUser(userListID,adapter);



                                                    } else {
                                                        fetchImage(event, eventId, listEvents, adapter, listtype);
                                                    }
                                                }

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
    /**
     *Remove the device id from userList
     * @param userListID, adapter
     *
     */
    private void removeUser(String userListID, HomeAdapter adapter) {
        UserListDB userList = new UserListDB();
        userList.removeFromList(userListID, deviceID);
        adapter.notifyDataSetChanged();
    }

    /**
     *CHeck if date has passed for each event in registration, draw, waitlist
     * @param event, listtype
     *      New Event object to be added to the event collection
     * @return bool
     */
    private boolean isValidDate(Event event, String listtype) {
        try {
            // Event date string from the event object
            String List;
            String date;
            if (listtype.equals("waitlist") || listtype.equals("draw")){
                date = event.getRegistrationCloseDate();
            }else{
                date = event.getDate();
            }
            // Format for parsing and comparing dates
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

            // Parse today's date and the event date
            Date todaysDate = formatter.parse(formatter.format(new Date())); // Parse today's date in the same format
            Date eventDate = formatter.parse(date); // Parse the event date

            // Compare dates
            return todaysDate.after(eventDate); // Return true if today's date is after the event date
        } catch (ParseException e) {
            Log.e("ValidDate", "Error parsing date: " + e.getMessage());
            return false;
        } catch (NullPointerException e) {
            Log.e("ValidDate", "Null date encountered: " + e.getMessage());
            return false;
        }
    }



    /**
     * CHeck which event got clicked and pass to new screen with params
     * @param event , listType
     *      Navigate to new screent
     */
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
    /**
     *Fetch image from database and set to EVent to be displayed on screen and notfify adapter
     * @param event, eventId, listEvents, adapter, listype
     *      New Event object to be added to the event collection
     */
    private void fetchImage(Event event, String eventId, List<Event> listEvents, HomeAdapter adapter, String listtype) {
        // Check if the deviceID exists in the user list for this event
        //String userListID = eventId + "-wait"; // Adjust the suffix based on your list type logic
        String userListID = eventId + "-" + (listtype.equals("waitlist") ? "wait" : listtype);
        db.collection("userList").document(userListID)
                .get()
                .addOnSuccessListener(userListDoc -> {
                    if (userListDoc.exists()) {
                        int size = userListDoc.contains("size") ? Integer.parseInt(userListDoc.getString("size")) : 0;
                        boolean deviceIdExists = false;

                        for (int i = 0; i < size; i++) {
                            String userKey = "user" + i;
                            String userId = userListDoc.getString(userKey);

                            if (deviceID.equals(userId)) {
                                deviceIdExists = true;
                                break;
                            }
                        }

                        if (!deviceIdExists) {
                            Log.d("FetchImage", "Device ID not found in user list for event: " + eventId);
                            return; // Do not proceed to add the event
                        }

                        // If the deviceID exists, fetch the image
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


                                    listEvents.add(event);
                                    adapter.setItemList(listEvents);
                                    adapter.notifyDataSetChanged();


                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Error fetching image for eventId: " + eventId + " - " + e.getMessage());
                                });
                    } else {
                        Log.e("FetchImage", "User list does not exist for event: " + eventId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FetchImage", "Error fetching user list for eventId: " + eventId + " - " + e.getMessage());
                });
    }

}
