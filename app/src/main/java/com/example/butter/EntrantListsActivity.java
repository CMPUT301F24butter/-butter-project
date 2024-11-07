package com.example.butter;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * This activity is used to display the users in any event's waitlist/draw list/registered list/cancelled list
 * Only the organizer of the event can see this
 * The organizer currently has the ability to move a single (random) user from the waitlist to the draw list
 *
 * Current outstanding issues: more functionality needs to be added for moving users between lists
 *
 * @author Nate Pane (natepane)
 */
public class EntrantListsActivity extends AppCompatActivity {

    String listSelected; // defines which user list has been chosen (waitlist, draw list, etc.)
    ListView entrantList;
    ArrayList<User> entrantsData;
    EntrantsArrayAdapter adapter;

    String eventID;
    String waitlistID;
    String drawlistID;
    String registeredListID;
    String cancelledListID;

    private FirebaseFirestore db;
    private CollectionReference userRef;
    private CollectionReference userListRef;
    private CollectionReference eventRef;

    Button generateEntrants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrants_list);

        String deviceID = getIntent().getExtras().getString("deviceID"); // logged in deviceID
        eventID = getIntent().getExtras().getString("eventID"); // clicked eventID

        // declaring the list IDs for all user lists associated with this event
        waitlistID = eventID + "-wait";
        drawlistID = eventID + "-draw";
        registeredListID = eventID + "-registered";
        cancelledListID = eventID + "-cancelled";

        db = FirebaseFirestore.getInstance();
        userRef = db.collection("user"); // user collection
        userListRef = db.collection("userList"); // userList collection
        eventRef = db.collection("event"); // event collection

        generateEntrants = findViewById(R.id.generate_entrants_button);

        entrantsData = new ArrayList<>();
        entrantList = findViewById(R.id.entrants_list);
        adapter = new EntrantsArrayAdapter(getApplicationContext(), entrantsData);
        entrantList.setAdapter(adapter);

        Spinner spinner = findViewById(R.id.entrants_spinner);

        // setting spinner options
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("Waitlist");
        arrayList.add("Draw");
        arrayList.add("Registered");
        arrayList.add("Cancelled");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrayList);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setAdapter(adapter);

        // setting a click listener for the spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                String list = adapterView.getItemAtPosition(position).toString(); // selected list
                listSelected = list; // setting the selected list

                displayEntrants(); // displaying entrants in this list
                generateButtons(); // displaying correct buttons for the chosen list
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // setting a click listener for the back button
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // setting a click listener for the generate entrants button
        generateEntrants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sampleEntrants();
            }
        });

    }

    // function to display entrants in the chosen list
    private void displayEntrants() {
        String userListID; // generating the userListID for the given list
        if (Objects.equals(listSelected, "Waitlist")) {
            userListID = waitlistID;
        } else if (Objects.equals(listSelected, "Draw")) {
            userListID = drawlistID;
        } else if (Objects.equals(listSelected, "Registered")) {
            userListID = registeredListID;
        } else {
            userListID = cancelledListID;
        }

        entrantsData.clear(); // clearing current data in the entrants list
        adapter.notifyDataSetChanged();
        // getting data from this userList document

        // retrieving data for this list
        userListRef.document(userListID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        String listSizeString = doc.getString("size");
                        int listSize = Integer.parseInt(listSizeString); // # of entrants in the list

                        for (int i = 0; i < listSize; i++) { // iterating over all entrants in the list
                            String deviceID = doc.getString("user" + i);

                            // getting the user's info
                            userRef.document(deviceID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> userTask) {
                                    if (userTask.isSuccessful()) {
                                        DocumentSnapshot userDoc = userTask.getResult();
                                        if (userDoc.exists()) {
                                            // extracting user details
                                            String deviceID = userDoc.getId();
                                            String name = userDoc.getString("userInfo.name");
                                            String privilegesString = userDoc.getString("userInfo.privilegesString");
                                            int privileges = Integer.parseInt(privilegesString);
                                            String facility = userDoc.getString("userInfo.facility");
                                            String email = userDoc.getString("userInfo.email");
                                            String phone = userDoc.getString("userInfo.phoneNumber");

                                            // creating User object
                                            User user = new User(deviceID, name, privileges, facility, email, phone);

                                            entrantsData.add(user); // adding the user to the list
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    // displays the correct buttons based on the list you have selected
    private void generateButtons() {
        generateEntrants.setVisibility(View.GONE);

        if (Objects.equals(listSelected, "Waitlist")) {
            generateEntrants.setVisibility(View.VISIBLE);
        }
    }

    // randomly moves users from the waitlist to the draw list
    private void sampleEntrants() {
        int sampleSize = 1; // currently only works for 1 user at a time

        ArrayList<User> shuffledUsers = new ArrayList<>();
        shuffledUsers.addAll(entrantsData);

        Collections.shuffle(shuffledUsers, new Random()); // shuffling the users in the list

        // creating a sublist of selected users
        List<User> selectedList;
        if (shuffledUsers.size() > sampleSize) {
            selectedList = shuffledUsers.subList(0, sampleSize);
        } else {
            selectedList = shuffledUsers.subList(0, shuffledUsers.size());
        }

        UserListDB userListDB = new UserListDB();

        for (User user : selectedList) {
            String deviceID = user.getDeviceID(); // selected user's deviceID
            userListDB.removeFromList(waitlistID, deviceID); // removing the user from the waitlist
            userListDB.addToList(drawlistID, deviceID); // adding the user to the draw list
        }

        try { // sleeping before re-printing the list
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        displayEntrants(); // re-printing the updated list
    }
}
