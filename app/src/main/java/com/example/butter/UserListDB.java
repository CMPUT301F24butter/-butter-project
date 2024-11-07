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
 * This class can be used to interact with the "userList" collection of our Firebase database
 * It implements methods to create user lists, add users to lists, remove users from lists and delete lists
 *
 * @author Nate Pane (natepane)
 */
public class UserListDB {
    private FirebaseFirestore db;
    private CollectionReference userListRef;

    public UserListDB() {
        db = FirebaseFirestore.getInstance();
        userListRef = db.collection("userList"); // userList collection
    }

    /**
     * Creates a new user list in the userList collection
     * Initializes the size field to 0
     * If there is already a document with the same userListID, method does nothing
     * @param userListID
     *      ID of the new user list to be created
     */
    public void create(String userListID, String listType) {
        DocumentReference docRef = userListRef.document(userListID);

        // fetching any existing data associated with this userListID
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (!doc.exists()) { // If there is no list with this userListID
                        HashMap<String, Object> data = new HashMap<>();
                        data.put("size", String.valueOf(0)); // initialize the size to 0
                        data.put("type", listType);
                        userListRef.document(userListID).set(data); // create new userList document

                        userListRef
                                .document(userListID)
                                .set(data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("Firebase", "User list created successfully");
                                    }
                                });
                    }
                    else { // If the list already exists
                        Log.d("Firebase", "User list already exists"); // do nothing
                    }
                }
            }
        });

    }

    /**
     * Adds a user to a user list in the userList collection
     * If the user is already in the list, or the list does not exist, method does nothing
     * @param userListID
     *      ID of the user list to be added to
     * @param deviceID
     *      device ID of the user to be added to the list
     */
    public void addToList(String userListID, String deviceID) {
        DocumentReference docRef = userListRef.document(userListID);

        // fetching any existing data associated with this userListID
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) { // if there is data associated with this userListID
                        HashMap<String, Object> data = new HashMap<>();

                        String listSize = doc.getString("size");
                        data.put("user" + listSize, deviceID); // adding the new user to the list

                        int listSizeInt = Integer.parseInt(listSize);

                        for (int i = 0; i <= listSizeInt; i++) { // checking that the user isn't already in the list
                            String ID = doc.getString("user" + i);
                            if (Objects.equals(ID, deviceID)) { // if the user is already in the list
                                Log.d("Firebase", "User is already on this list"); // do nothing
                                return;
                            }
                        }

                        listSizeInt += 1; // if the user is not in the list, increment the list size
                        data.put("size", String.valueOf(listSizeInt)); // updating the list size

                        // saving changes to the user list document
                        docRef.update(data);
                        docRef
                                .update(data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("Firebase", "User added to list successfully");
                                    }
                                });
                    }
                    else { // If there is no data associated with this userListID
                        Log.d("Firebase", "User list does not exist"); // do nothing
                    }
                }
            }
        });
    }

    /**
     * Removes a user from a user list in the userList collection
     * If the user is not in the list, or the list does not exist, method does nothing
     * @param userListID
     *      ID of the user list to be removed from
     * @param deviceID
     *       device ID of the user to be removed from the list
     */
    public void removeFromList(String userListID, String deviceID) {
        DocumentReference docRef = userListRef.document(userListID);

        // fetching any existing data associated with this userListID
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) { // if there is data associated with this userListID
                        String listSize = doc.getString("size");
                        int listSizeInt = Integer.parseInt(listSize);

                        ArrayList<String> updatedUsers = new ArrayList<>();
                        int found = 0;
                        for (int i = 0; i < listSizeInt; i++) { // adding all other users who will remain in the list to an ArrayList
                            String userID = doc.getString("user" + i);
                            if (Objects.equals(userID, deviceID)) { // If the user with this deviceID is in the list
                                found = 1; // flag that it is found
                            } else {
                                updatedUsers.add(userID);
                            }
                        }

                        if (found == 1) { // If the user was in the list
                            listSizeInt--; // decrement the list size
                            HashMap<String, Object> data = new HashMap<>();
                            data.put("size", String.valueOf(listSizeInt)); // update the size of the list

                            for (int i = 0; i < updatedUsers.size(); i++) { // putting back all the remaining users with updated indexes
                                data.put("user" + i, updatedUsers.get(i));
                            }

                            // updating the user list
                            userListRef.document(userListID).set(data);
                            userListRef
                                    .document(userListID)
                                    .set(data)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("Firebase", "User deleted from list successfully");
                                        }
                                    });
                        }
                        else { // If the user is not in the list
                            Log.d("Firebase", "User is not in the list"); // do nothing
                        }
                    }
                    else { // if this user list does not exist
                        Log.d("Firebase", "User list does not exist"); // do nothing
                    }
                }
            }
        });
    }

    public void deleteList(String userListID) {
        DocumentReference docRef = userListRef.document(userListID);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {

                        docRef.delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("Firebase", "List deleted successfully");
                                    }
                                });
                    } else {
                        Log.d("Firebase", "List does not exist"); // do nothing
                    }
                }
            }
        });
    }
}
