package com.example.butter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Uses {@link HomeFragment#newInstance} to create an instance of this fragment.
 *
 * This fragment is used to set up the home screen on the admins side and the entrants (or both
 * entrant and organizers) side. The admins have access to a spinner and the entrants will have
 * access to only the upcoming and waiting list events.
 * Outstanding Issue: When I move to another screen and come back to the admin screen,
 *               the spinner goes back to default option (browse events) instead of the
 *               option user last clicked on
 * @author Angela
 */
public class HomeFragment extends Fragment {

    // the fragment initialization parameters
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    // Need access to all events, users, and posters, deviceID
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference userRef;
    private String deviceID;

    // Lists of events, users, and posters
    private ArrayList<Event> allEvents;
    private ArrayList<User> allUsers;
    private ArrayList<User> allFacilities;
    private ListView adminListView;
    private EventArrayAdapter eventArrayAdapter;
    private ArrayAdapter<User> userArrayAdapter;
    private Boolean isFacility;
    ActivityResultLauncher<ScanOptions> barLauncher;

    // Entrant waiting and upcoming recycler view
    private RecyclerView eventsRecyclerView;
    private RecyclerView waitingListRecyclerView;
    private HomeAdapter upcomingAdapter;
    private HomeAdapter waitingListAdapter;
    private ListenerRegistration upcomingListener;
    private ListenerRegistration waitingListener;

    Button qrScan;

    /**
     * Constructor for Home Fragment.
     */
    public HomeFragment() {
        allEvents = new ArrayList<>();
        allUsers = new ArrayList<>();
        allFacilities = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("event"); // event collection
        userRef = db.collection("user"); // user collection
    }

    /**
     * Create a new instance of home fragment.
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * onCreate method
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Bundle args = getArguments();
        deviceID = args.getString("deviceID");
        eventArrayAdapter = new EventArrayAdapter(getContext(), allEvents);

        barLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                Intent intent = new Intent(requireContext(), EventDetailsActivity.class);
                intent.putExtra("deviceID", deviceID);
                intent.putExtra("eventID", result.getContents());
                startActivity(intent);
            }
        });
    }

    /**
     * Inflates view and sets up qrScan, admin and entrants XML.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //----------------------------------------------------------------------------
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
        //---------------------------------------------------------------------------------

        qrScan = view.findViewById(R.id.qrScannerButton);
        qrScan.setOnClickListener(v -> {
            scanCode();
        });

        adminListView = (ListView) view.findViewById(R.id.admin_list_view);
        adminListView.setAdapter(eventArrayAdapter);

        // get access to elements in the xml
        TextView greetingText = view.findViewById(R.id.greetingText);
        TextView qrCodeText = view.findViewById(R.id.qrCodeText);
        TextView upcomingText = view.findViewById(R.id.upcomingText);
        HorizontalScrollView upcomingScrollView = view.findViewById(R.id.horizontalScrollView);
        TextView waitingText = view.findViewById(R.id.waiting_list_label);
        HorizontalScrollView waitingScrollView = view.findViewById(R.id.waitingListScrollView);

        RelativeLayout adminSpinnerLayout = view.findViewById(R.id.admin_spinner_layout);
        Spinner adminSpinner = view.findViewById(R.id.entrants_spinner);

        // Populating the spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, new String[]{"Browse Events", "Browse Profiles", "Browse Facilities", "Browse Images", "Entrant's Page"});
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adminSpinner.setAdapter(spinnerAdapter);

        // depending on users privileges, user sees either browsing or upcoming/waiting list page
        userRef.document(deviceID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        String privileges = doc.getString("userInfo.privilegesString");

                        // If a user is an admin + organizer, they get browse abilities + upcoming / waiting list
                        if (Objects.equals(privileges, "400") || Objects.equals(privileges, "500") || Objects.equals(privileges, "600") || Objects.equals(privileges, "700")) {
                            adminSpinnerLayout.setVisibility(View.VISIBLE);
                            adminListView.setVisibility(View.VISIBLE);

                        // If user is an entrant / entrant + organizer, only has upcoming / waiting list
                        } else if (Objects.equals(privileges, "100") || Objects.equals(privileges, "300")) {
                            String user = doc.getString("userInfo.name");
                            greetingText.setText(String.format("Hey %s!", user));
                            greetingText.setVisibility(View.VISIBLE);
                            qrCodeText.setVisibility(View.VISIBLE);
                            switchToEntrant(adminListView, upcomingText, upcomingScrollView, waitingText, waitingScrollView);
                        }
                    }
                }
            }
        });

        adminSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                // If user selects a new option in spinner, it will show the right list / text
                switch (selection) {
                    case "Browse Events":
                        adminListView.setAdapter(eventArrayAdapter);
                        switchToAdmin(adminListView, upcomingText, upcomingScrollView, waitingText, waitingScrollView);
                        showEventsList();
                        break;
                    case "Browse Profiles":
                        isFacility = Boolean.FALSE;
                        userArrayAdapter = new UserArrayAdapter(getContext(), allUsers, isFacility);
                        adminListView.setAdapter(userArrayAdapter);
                        switchToAdmin(adminListView, upcomingText, upcomingScrollView, waitingText, waitingScrollView);
                        showProfilesList();
                        break;
                    case "Browse Facilities":
                        isFacility = Boolean.TRUE;
                        userArrayAdapter = new UserArrayAdapter(getContext(), allFacilities, isFacility);
                        adminListView.setAdapter(userArrayAdapter);
                        switchToAdmin(adminListView, upcomingText, upcomingScrollView, waitingText, waitingScrollView);
                        showProfilesList();
                        break;
                    case "Browse Images":
                        adminListView.setAdapter(null);
                        switchToAdmin(adminListView, upcomingText, upcomingScrollView, waitingText, waitingScrollView);
                        //showImagesList();
                        break;
                    case "Entrant's Page":
                        eventArrayAdapter.notifyDataSetChanged();
                        // removes admin list view and adds upcoming and waiting list events
                        switchToEntrant(adminListView, upcomingText, upcomingScrollView, waitingText, waitingScrollView);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return view;
    }

    /**
     * Sets up the QR code scanner.
     */
    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    /**
     * Sets the xml for entrant actions visible.
     * @param adminListView - admins list view, will be set to gone
     * @param upcomingText - entrant textview, will be set to visible
     * @param upcomingScrollView - entrants upcoming events, will be set visible
     * @param waitingText - entrant textview, will be set to visible
     * @param waitingScrollView - entrants waiting list events, will be set visible
     */
    private void switchToEntrant(ListView adminListView, TextView upcomingText, HorizontalScrollView upcomingScrollView, TextView waitingText, HorizontalScrollView waitingScrollView) {
        adminListView.setVisibility(View.GONE);
        upcomingText.setVisibility(View.VISIBLE);
        upcomingScrollView.setVisibility(View.VISIBLE);
        waitingText.setVisibility(View.VISIBLE);
        waitingScrollView.setVisibility(View.VISIBLE);

        // Sets up the waiting and upcoming events recycler view
        fetchUpcomingEvents();
        fetchWaitingList();
    }

    /**
     * Sets the xml for admin actions visible.
     * @param adminListView - admins list view, will be set visible
     * @param upcomingText - entrant textview, will be set to gone
     * @param upcomingScrollView - entrants upcoming events, will be set gone
     * @param waitingText - entrant textview, will be set to gone
     * @param waitingScrollView - entrants waiting list events, will be set gone
     *
     */
    private void switchToAdmin(ListView adminListView, TextView upcomingText, HorizontalScrollView upcomingScrollView, TextView waitingText, HorizontalScrollView waitingScrollView) {
        adminListView.setVisibility(View.VISIBLE);
        upcomingText.setVisibility(View.GONE);
        upcomingScrollView.setVisibility(View.GONE);
        waitingText.setVisibility(View.GONE);
        waitingScrollView.setVisibility(View.GONE);
    }

    /**
     * Populates the admin list with all events.
     */
    private void showEventsList() {
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
    private void showProfilesList() {
        userRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    allUsers.clear();
                    allFacilities.clear();
                    for (DocumentSnapshot doc : task.getResult()) {
                        String deviceID = doc.getString("userInfo.deviceID");
                        String email = doc.getString("userInfo.email");
                        String facility = doc.getString("userInfo.facility");
                        String name = doc.getString("userInfo.name");
                        String phone = doc.getString("userInfo.phoneNumber");
                        int privileges = Integer.parseInt(doc.getString("userInfo.privilegesString"));

                        User user = new User(deviceID, name, privileges, facility, email, phone);
                        if (isFacility) {
                            if (facility != null) {
                                allFacilities.add(user);
                            }
                        } else {
                            allUsers.add(user);
                        }
                    }
                    userArrayAdapter.notifyDataSetChanged();
                } else {
                    Log.d("Firebase", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    //------------------------------------------------------------------------------------------
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