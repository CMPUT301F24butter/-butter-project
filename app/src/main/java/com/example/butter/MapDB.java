package com.example.butter;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * This class can be used to interact with the "map" collection in Firebase
 *
 * author: Nate Pane (natepane)
 */
public class MapDB {
    private FirebaseFirestore db;
    private CollectionReference mapRef;

    public MapDB() {
        db = FirebaseFirestore.getInstance();
        mapRef = db.collection("map"); // userList collection
    }

    /**
     * Creates a new document in the map collection for an event with geolocation enabled
     * @param eventID
     *      eventID of the event
     */
    public void createMap(String eventID) {
        DocumentReference docRef = mapRef.document(eventID);

        // fetching data associated with this eventID
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) { // if a document already exits, do nothing
                        Log.d("Firebase", "Map already exists for this event");
                    } else {
                        HashMap<String, Object> data = new HashMap<>();
                        data.put("size", "0"); // initialize the size to 0

                        // creating the document
                        mapRef.document(eventID).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("Firebase", "Map added successfully");
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * Stores the location of a given user to the a map document for a given event
     * @param eventID
     *      eventID of the event whose waitlist the user joined
     * @param deviceID
     *      deviceID of the user who joined the waitlist
     * @param lat
     *      current latitude of the user
     * @param lon
     *      current longitude of the user
     */
    public void addLocation(String eventID, String deviceID, double lat, double lon) {
        DocumentReference docRef = mapRef.document(eventID);

        // getting map data associated with this eventID
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        HashMap<String, Object> updates = new HashMap<>();
                        String sizeString = doc.getString("size");
                        int size = Integer.parseInt(sizeString); // # of locations currently in the map

                        for (int i = 0; i < size; i++) { // checking that the deviceID isn't already on the map
                            String idLatLon = doc.getString("user" + i);
                            String[] split = idLatLon.split("\\s+");
                            if (Objects.equals(split[0], deviceID)) {
                                Log.d("Firebase", "This device ID is already on the map");
                                return;
                            }
                        }
                        String latString = String.valueOf(lat);
                        String lonString = String.valueOf(lon);
                        // storing deviceID + latitude + longitude as one space seperated string
                        String idLatLon = deviceID + " " + latString + " " + lonString;

                        updates.put("user" + size, idLatLon); // adding a new field for this user
                        size++;
                        updates.put("size", String.valueOf(size)); // updating the size of the map

                        // updating the document
                        docRef.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("Firebase", "Latitude and Longitude stored successfully");
                            }
                        });

                    } else { // if there is not map associated with this eventID, do nothing
                        Log.d("Firebase", "No map associated with this event ID");
                    }
                }
            }
        });
    }

    /**
     * Removing a user's location from the map data
     * @param eventID
     *      eventID of the event whose map the user's location is stored in
     * @param deviceID
     *      deviceID of the user to be removed from the map
     */
    public void removeLocation(String eventID, String deviceID) {
        DocumentReference docRef = mapRef.document(eventID);

        // fetching data for this event's map
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        HashMap<String, Object> updates = new HashMap<>();
                        String sizeString = doc.getString("size");
                        int size = Integer.parseInt(sizeString); // # locations currently on the map

                        ArrayList<String> updatedList = new ArrayList<>();
                        boolean found = false;
                        for (int i = 0; i < size; i++) { // finding all locations to stay on the document
                            String idLatLon = doc.getString("user" + i);
                            String[] split = idLatLon.split("\\s+");

                            if (Objects.equals(split[0], deviceID)) { // if the deviceID matches
                                found = true; // don't keep the field
                            } else { // otherwise
                                updatedList.add(idLatLon); // store the location to be put back in the document
                            }
                        }

                        if (found) { // if the deviceID was found in the map
                            size--;
                            HashMap<String, Object> data = new HashMap<>();
                            data.put("size", String.valueOf(size)); // updating the size of the map

                            for (int i = 0; i < size; i++) { // putting back the locations that remain on the map
                                data.put("user" + i, updatedList.get(i));
                            }

                            // updating the map document
                            mapRef.document(eventID).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d("Firebase", "User location data deleted successfully");
                                }
                            });
                        } else { // if the deviceID is not on the map, do nothing
                            Log.d("Firebase", "DeviceID is not contained in this map");
                        }

                    } else { // if this event doesn't have a map document, do nothing
                        Log.d("Firebase", "No map associated with this event ID");
                    }
                }
            }
        });
    }

    /**
     * deletes a map document for a given event
     * @param eventID
     *      eventID of the event whose map will be deleted
     */
    public void deleteMap(String eventID) {
        DocumentReference docRef = mapRef.document(eventID);

        // fetching map data for this event
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) { // if the map data exists, delete it
                        docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("Firebase", "Map data deleted successfully");
                            }
                        });
                    } else { // if there is no map data for this event, do nothing
                        Log.d("Firebase", "No map associated with this event ID");
                    }
                }
            }
        });
    }

    // splits the space seperated string of deviceID + latitude + longitude into an array of the 3 values (string form)
    public String[] splitData(String idLatLon) {
        String[] split = idLatLon.split("\\s+");
        return split;
    }
}
