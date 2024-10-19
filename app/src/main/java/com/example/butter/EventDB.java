package com.example.butter;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class EventDB {
    private FirebaseFirestore db;
    private CollectionReference eventRef;

    public EventDB() {
        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("event"); // event collection
    }

    /**
     * Adds a new event to the event collection
     * If there is already a document with the same eventID, method does nothing
     * @param event
     *      New Event object to be added to the event collection
     */
    public void add(Event event) {
        DocumentReference docRef = eventRef.document(event.getEventID());

        // fetching any existing data associated with this eventID
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) { // If there is already event data associated with this eventID
                        Log.d("Firebase", "Event already exists"); // Do nothing
                    }
                    else { // If there is not yet event data associated with this eventID
                        HashMap<String, Event> data = new HashMap<>();

                        data.put("eventInfo", event);

                        // add new event document to the collection
                        eventRef.document(event.getEventID()).set(data);

                        eventRef
                                .document(event.getEventID())
                                .set(data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("Firebase", "Event added successfully");
                                    }
                                });

                    }
                }
            }
        });
    }

    /**
     * Updates an existing document in the event collection
     * If there is not yet a document with the same eventID, method does nothing
     * @param event
     *      Event object with any potentially updated field(s)
     *      eventID field must stay consistent with the document you want to update
     */
    public void update(Event event) {
        DocumentReference docRef = eventRef.document(event.getEventID());
        // fetching any existing data associated with this eventID
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) { // if there is already data associated with this eventID

                        HashMap<String, Object> update = new HashMap<>();

                        update.put("eventInfo", event); // update the fields

                        docRef.update(update) // update the document
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("Firebase", "Event updated successfully");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("Firebase", "Event update failed");
                                    }
                                });
                    }
                    else { // If there is no data associated with this eventID
                        Log.d("Firebase", "Event does not exist"); // do nothing
                    }
                }
            }
        });
    }

    /**
     * Deletes an event from the event collection
     * If there is not yet a document with the same eventID, method does nothing
     * @param eventID
     *      eventID of the event to be deleted
     */
    public void delete(String eventID) {
        DocumentReference docRef = eventRef.document(eventID);
        // fetching any existing data associated with this eventID
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) { // if there is already data associated with this eventID

                        docRef.delete() // delete the document
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("Firebase", "Event deleted successfully");
                                    }
                                });

                    } else { // If there is no data associated with this deviceID
                        Log.d("Firebase", "Event does not exist"); // do nothing
                    }
                }
            }
        });
    }
}