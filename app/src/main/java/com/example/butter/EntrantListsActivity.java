package com.example.butter;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
    String selectedUserID = null;

    String eventID;
    String waitlistID;
    String drawlistID;
    String registeredListID;
    String cancelledListID;

    private FirebaseFirestore db;
    private CollectionReference userRef;
    private CollectionReference userListRef;
    private CollectionReference imageRef;

    Button generateEntrants;
    Button drawReplacement;
    FloatingActionButton deleteEntrant;
    EditText sampleSize;

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
        imageRef = db.collection("image"); // image collection

        generateEntrants = findViewById(R.id.generate_entrants_button);
        drawReplacement = findViewById(R.id.draw_replacements_button);
        deleteEntrant = findViewById(R.id.delete_entrant_button);
        sampleSize =findViewById(R.id.sample_size);

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

        // setting a click listener for elements in the list
        entrantList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (Objects.equals(listSelected, "Draw")) {
                    entrantList.setItemChecked(position, true);
                    selectedUserID = entrantsData.get(position).getDeviceID();
                }
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
                String sampleSizeString = sampleSize.getText().toString();
                if (sampleSizeString.isEmpty()) { // if no sample size was specified
                    Toast toast = Toast.makeText(getApplicationContext(), "Choose the number of entrants to sample.", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                int sampleSizeInt = Integer.parseInt(sampleSizeString);

                if (sampleSizeInt > entrantsData.size()) { // if the sample size is greater than the number of users in the waitlist
                    Toast toast = Toast.makeText(getApplicationContext(), "Sample size too large.", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
                
                if (sampleSizeInt == 0) { // if the sample size is 0
                    return;
                }
                
                sampleEntrants(sampleSizeInt); // if valid, run the lottery
                sampleSize.setText("");
            }
        });

        // setting a click listener for the draw replacement button
        drawReplacement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawReplacementEntrant();
            }
        });

        // setting a click listener for the cancel entrant button
        deleteEntrant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedUserID != null) { // if a user is selected
                    UserListDB userListDB = new UserListDB();

                    userListDB.removeFromList(drawlistID, selectedUserID); // remove them from the draw list
                    userListDB.addToList(cancelledListID, selectedUserID); // add them to the cancelled list

                    try { // sleeping before re-printing the list
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    selectedUserID = null;

                    displayEntrants(); // re-printing the updated list
                }
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

                                            // retrieving image data for this user
                                            imageRef.document(user.getDeviceID()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> imageTask) {
                                                    if (imageTask.isSuccessful()) {
                                                        DocumentSnapshot imageDoc = imageTask.getResult();
                                                        if (imageDoc.exists()) { // if there is image data for this user
                                                            String base64string = imageDoc.getString("imageData"); // fetching image string data
                                                            user.setProfilePicString(base64string);
                                                        }
                                                        entrantsData.add(user); // adding the user to the list
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                }
                                            });
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
        drawReplacement.setVisibility(View.GONE);
        deleteEntrant.setVisibility(View.GONE);
        sampleSize.setVisibility(View.GONE);

        if (Objects.equals(listSelected, "Waitlist")) {
            generateEntrants.setVisibility(View.VISIBLE);
            sampleSize.setVisibility(View.VISIBLE);
        }
        else if (Objects.equals(listSelected, "Cancelled")) {
            drawReplacement.setVisibility(View.VISIBLE);
        }
        else if (Objects.equals(listSelected, "Draw")) {
            deleteEntrant.setVisibility(View.VISIBLE);
        }
    }

    // randomly moves users from the waitlist to the draw list
    private void sampleEntrants(int sampleSize) {

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
        ArrayList<String> selectedIDList = new ArrayList<>(); // storing the deviceIDs of selected users
        for (User user : selectedList) {
            selectedIDList.add(user.getDeviceID());
        }

        // fetching waitlist data for this event
        userListRef.document(waitlistID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                int size = Integer.parseInt(doc.getString("size"));

                ArrayList<String> stillInWaitlist = new ArrayList<>();
                HashMap<String, Object> updates = new HashMap<>();

                for (int i = 0; i < size; i++) { // storing the deviceIDs of users who will remain in the waitlist
                    String deviceID = doc.getString("user" + i);
                    if (!selectedIDList.contains(deviceID)) {
                        stillInWaitlist.add(deviceID);
                    }

                    updates.put("user" + i, FieldValue.delete()); // removing all user fields from the document
                }

                int new_size = size - sampleSize;
                updates.put("size", String.valueOf(new_size)); // updating the list size

                for (int i = 0; i < stillInWaitlist.size(); i++) { // putting the remaining deviceIDs back in the document
                    updates.put("user" + i, stillInWaitlist.get(i));
                }

                userListRef.document(waitlistID).update(updates);
            }
        });

        // fetching draw list data for this event
        userListRef.document(drawlistID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                int size = Integer.parseInt(doc.getString("size"));

                HashMap<String, Object> updates = new HashMap<>();

                for (int i = size; i < size + sampleSize; i++) { // adding deviceIDs of lottery winners to the document
                    updates.put("user" + i, selectedIDList.get(i - size));
                }

                int new_size = size + sampleSize;
                updates.put("size", String.valueOf(new_size)); // updating the list size

                userListRef.document(drawlistID).update(updates);
            }
        });

        try { // sleeping before re-printing the list
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        displayEntrants(); // re-printing the updated list
    }

    private void drawReplacementEntrant() {

        if (entrantsData.size() == 0) { // if there are no cancelled entrants
            Toast toast = Toast.makeText(getApplicationContext(), "There are no cancelled entrants.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        // retrieving waitlist data
        userListRef.document(waitlistID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        String listSizeString = doc.getString("size");
                        int listSize = Integer.parseInt(listSizeString); // # of entrants in the list

                        if (listSize == 0) { // if there are no users in the waitlist
                            Toast toast = Toast.makeText(getApplicationContext(), "No entrants in the waitlist.", Toast.LENGTH_LONG);
                            toast.show();
                            return;
                        }

                        Random random = new Random();
                        int randomNumber = random.nextInt(listSize); // picking a random number between 0 and listSize - 1

                        String deviceID = doc.getString("user" + randomNumber); // deviceID of the randomly chosen user

                        UserListDB userListDB = new UserListDB();
                        userListDB.removeFromList(waitlistID, deviceID); // removing the user from the waitlist
                        userListDB.addToList(drawlistID, deviceID); // adding the user to the draw list
                    }
                }
            }
        });
    }
}
