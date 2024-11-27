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

public class MapDB {
    private FirebaseFirestore db;
    private CollectionReference mapRef;

    public MapDB() {
        db = FirebaseFirestore.getInstance();
        mapRef = db.collection("map"); // userList collection
    }

    public void createMap(String eventID) {
        DocumentReference docRef = mapRef.document(eventID);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Log.d("Firebase", "Map already exists for this event");
                    } else {
                        HashMap<String, Object> data = new HashMap<>();
                        data.put("size", "0");

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

    public void addLocation(String eventID, String deviceID, float lat, float lon) {
        DocumentReference docRef = mapRef.document(eventID);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        HashMap<String, Object> updates = new HashMap<>();
                        String sizeString = doc.getString("size");
                        int size = Integer.parseInt(sizeString);

                        for (int i = 0; i < size; i++) {
                            String idLatLon = doc.getString("user" + i);
                            String[] split = idLatLon.split("\\s+");
                            if (Objects.equals(split[0], deviceID)) {
                                Log.d("Firebase", "This device ID is already on the map");
                                return;
                            }
                        }
                        String latString = String.valueOf(lat);
                        String lonString = String.valueOf(lon);
                        String idLatLon = deviceID + " " + latString + " " + lonString;

                        updates.put("user" + size, idLatLon);
                        size++;
                        updates.put("size", String.valueOf(size));

                        docRef.update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("Firebase", "Latitude and Longitude stored successfully");
                            }
                        });

                    } else {
                        Log.d("Firebase", "No map associated with this event ID");
                    }
                }
            }
        });
    }

    public void removeLocation(String eventID, String deviceID) {
        DocumentReference docRef = mapRef.document(eventID);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        HashMap<String, Object> updates = new HashMap<>();
                        String sizeString = doc.getString("size");
                        int size = Integer.parseInt(sizeString);

                        ArrayList<String> updatedList = new ArrayList<>();
                        boolean found = false;
                        for (int i = 0; i < size; i++) {
                            String idLatLon = doc.getString("user" + i);
                            String[] split = idLatLon.split("\\s+");

                            if (Objects.equals(split[0], deviceID)) {
                                found = true;
                            } else {
                                updatedList.add(idLatLon);
                            }
                        }

                        if (found) {
                            size--;
                            HashMap<String, Object> data = new HashMap<>();
                            data.put("size", String.valueOf(size));

                            for (int i = 0; i < size; i++) {
                                data.put("user" + i, updatedList.get(i));
                            }

                            mapRef.document(eventID).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d("Firebase", "User location data deleted successfully");
                                }
                            });
                        } else {
                            Log.d("Firebase", "DeviceID is not contained in this map");
                        }

                    } else {
                        Log.d("Firebase", "No map associated with this event ID");
                    }
                }
            }
        });
    }

    public void deleteMap(String eventID) {
        DocumentReference docRef = mapRef.document(eventID);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("Firebase", "Map data deleted successfully");
                            }
                        });
                    } else {
                        Log.d("Firebase", "No map associated with this event ID");
                    }
                }
            }
        });
    }

    public String[] splitData(String idLatLon) {
        String[] split = idLatLon.split("\\s+");
        return split;
    }
}
