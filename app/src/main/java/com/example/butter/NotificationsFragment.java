package com.example.butter;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NotificationsFragment extends Fragment {

    private FirebaseFirestore db;
    private String deviceID;
    private ListView notificationListView;
    private List<String> notificationList;
    private ArrayAdapter<String> notificationAdapter;
    private SwitchCompat notificationSwitch;

    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            deviceID = getArguments().getString("deviceID");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        notificationListView = view.findViewById(R.id.notificationList);
        notificationSwitch = view.findViewById(R.id.notificationSwitch);

        notificationList = new ArrayList<>();
        notificationAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, notificationList);
        notificationListView.setAdapter(notificationAdapter);

        db = FirebaseFirestore.getInstance();

        fetchNotifications();
        setupNotificationSwitch();

        return view;
    }

    /**
     * Fetch notifications from Firestore for the current user and display them in the ListView
     */
    /**
     * Fetch notifications from Firestore for the current user and display them in the ListView
     */
    private void fetchNotifications() {
        // Access the user document for the current deviceID
        db.collection("user")
                .document(deviceID)  // Fetch document based on deviceID
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            // Retrieve the 'notifications' Boolean from userInfo
                            Boolean notificationsEnabled = document.getBoolean("userInfo.notifications");

                            // First, check for force notifications
                            fetchNotificationsFromDatabase(true);  // Force notifications first

                            // Then, check if notifications are enabled for the user
                            if (Boolean.TRUE.equals(notificationsEnabled)) {
                                fetchNotificationsFromDatabase(false);  // Fetch notifications if enabled
                            } else {
                                Log.d("NotificationsFragment", "Notifications are disabled for this user.");
                            }
                        } else {
                            Log.d("NotificationsFragment", "User document does not exist.");
                        }
                    } else {
                        Log.e("NotificationsFragment", "Error fetching user document", task.getException());
                    }
                });
    }

    private void fetchNotificationsFromDatabase(boolean forceCheck) {
        // Fetch notifications where recipientDeviceID matches the current deviceID
        db.collection("notification")
                .whereEqualTo("notificationInfo.recipientDeviceID", deviceID)  // Filter by recipientDeviceID
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            // Iterate through the notifications and add them to the list
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                String title = document.getString("notificationInfo.eventSender");
                                String body = document.getString("notificationInfo.message");
                                Boolean force = document.getBoolean("notificationInfo.force");

                                // If force is true, add the notification immediately
                                if (Boolean.TRUE.equals(force) && forceCheck) {
                                    addNotificationToList(title, body);
                                } else if (Boolean.TRUE.equals(force) || Boolean.TRUE.equals(document.getBoolean("userInfo.notifications"))) {
                                    // Add notification if force is true, or user has enabled notifications
                                    addNotificationToList(title, body);
                                }
                            }
                            notificationAdapter.notifyDataSetChanged(); // Update the ListView
                        }
                    } else {
                        Log.e("NotificationsFragment", "Error fetching notifications", task.getException());
                    }
                });
    }




    /**
     * Adds a notification to the ListView.
     */
    private void addNotificationToList(String title, String body) {
        if (title != null && body != null) {
            String notification = body + " \n\n Notification for the event " + title + "\n";
            notificationList.add(notification);
        }
    }




    /**
     * Set up the notification switch to update user settings in Firestore
     */
    private void setupNotificationSwitch() {
        // Reference the document in the `user` collection using deviceID
        DocumentReference userDocRef = db.collection("user").document(deviceID);

        // Fetch the initial value for notifications from the userInfo map in Firestore
        userDocRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("userInfo.notifications")) {
                        // Retrieve the notifications field from the userInfo map
                        Boolean notificationsEnabled = documentSnapshot.getBoolean("userInfo.notifications");
                        notificationSwitch.setChecked(notificationsEnabled != null ? notificationsEnabled : true); // Default to ON
                    } else {
                        // If the field or document doesn't exist, set default state
                        notificationSwitch.setChecked(true);
                        // Create the userInfo.notifications field in Firestore
                        userDocRef.update("userInfo.notifications", true)
                                .addOnSuccessListener(aVoid -> System.out.println("Default notification setting created."))
                                .addOnFailureListener(e -> System.err.println("Error creating default notification setting: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> System.err.println("Error fetching userInfo: " + e.getMessage()));

        // Add a listener to handle switch changes
        notificationSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            userDocRef.update("userInfo.notifications", isChecked)
                    .addOnSuccessListener(aVoid -> {
                        String status = isChecked ? "enabled" : "disabled";
                        System.out.println("Notifications " + status);
                    })
                    .addOnFailureListener(e -> System.err.println("Error updating notifications: " + e.getMessage()));
        });
    }
}
