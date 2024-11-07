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

/**
 * This class can be used to interact with the "user" collection of our Firebase database
 * It implements methods to easily add, update and remove users in this collection
 *
 * @author Nate Pane (natepane)
 */
public class UserDB {
    private FirebaseFirestore db;
    private CollectionReference userRef;

    public UserDB() {
        db = FirebaseFirestore.getInstance();
        userRef = db.collection("user"); // user collection
    }

    /**
     * Adds a new user to the user collection
     * If there is already a document with the same deviceID, method does nothing
     * @param user
     *      New User object to be added to the user collection
     */
    public void add(User user) {
        DocumentReference docRef = userRef.document(user.getDeviceID());

        // fetching any existing data associated with this deviceID
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) { // If there is already user data associated with this deviceID
                        Log.d("Firebase", "User already exists"); // Do nothing
                    }
                    else { // If there is not yet user data associated with this deviceID
                        HashMap<String, User> data = new HashMap<>();

                        data.put("userInfo", user);

                        // add new user document to the collection
                        userRef.document(user.getDeviceID()).set(data);

                        userRef
                                .document(user.getDeviceID())
                                .set(data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("Firebase", "User added successfully");
                                    }
                                });
                    }
                }
            }
        });
    }

    /**
     * Updates an existing document in the user collection
     * If there is not yet a document with the same deviceID, method does nothing
     * @param user
     *      User object with any potentially updated field(s)
     *      deviceID field must stay consistent with the document you want to update
     */
    public void update(User user) {
        DocumentReference docRef = userRef.document(user.getDeviceID());
        // fetching any existing data associated with this deviceID
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) { // if there is already data associated with this deviceID

                        HashMap<String, Object> update = new HashMap<>();

                        update.put("userInfo", user);

                        docRef.update(update) // update the document
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("Firebase", "User updated successfully");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("Firebase", "User update failed");
                                    }
                                });
                    }
                    else { // If there is no data associated with this deviceID
                        Log.d("Firebase", "User does not exist"); // do nothing
                    }
                }
            }
        });
    }

    /**
     * Deletes a user from the user collection
     * If there is not yet a document with the same deviceID, method does nothing
     * @param deviceID
     *      deviceID of the user to be deleted
     */
    public void delete(String deviceID) {
        DocumentReference docRef = userRef.document(deviceID);
        // fetching any existing data associated with this deviceID
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) { // if there is already data associated with this deviceID

                        docRef.delete() // delete the document
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("Firebase", "User deleted successfully");
                                    }
                                });

                    } else { // If there is no data associated with this deviceID
                        Log.d("Firebase", "User does not exist"); // do nothing
                    }
                }
            }
        });
    }
}