package com.example.butter;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.CompoundButton;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Collections;
import java.util.Locale;

public class NotificationsFragment extends Fragment {

    private FirebaseFirestore db;
    private String deviceID;
    private ListView notificationListView;
    private List<Notification> notificationList; // List of Notification objects
    private ArrayAdapter<String> notificationAdapter;
    private String selectedNotificationID = null;

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
        FloatingActionButton deleteButton = view.findViewById(R.id.delete_button);
        notificationSwitch = view.findViewById(R.id.notificationSwitch);

        notificationList = new ArrayList<>();
        notificationAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
        notificationListView.setAdapter(notificationAdapter);

        db = FirebaseFirestore.getInstance();

        fetchNotifications();
        setupNotificationSwitch();

        // Set up ListView item click listener
        notificationListView.setOnItemClickListener((parent, itemView, position, id) -> {
            selectedNotificationID = notificationList.get(position).getNotificationID(); // Get the ID of the selected notification
        });

        // Set up Delete Button click listener
        deleteButton.setOnClickListener(v -> {
            if (selectedNotificationID != null) {
                deleteNotification(selectedNotificationID);
            } else {
                Log.w("NotificationsFragment", "No notification selected for deletion.");
            }
        });

        return view;
    }

    private void fetchNotifications() {
        db.collection("notification")
                .whereEqualTo("notificationInfo.recipientDeviceID", deviceID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            notificationList.clear();
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                String title = document.getString("notificationInfo.eventSender");
                                String body = document.getString("notificationInfo.message");
                                String notificationID = document.getString("notificationInfo.notificationID");
                                String datetimeStr = document.getString("notificationInfo.datetime"); // This is the string to convert

                                Date datetime = convertStringToDate(datetimeStr);
                                if (datetime != null) {
                                    Notification notification = new Notification(title, body, notificationID, datetime);
                                    notificationList.add(notification);
                                }
                            }

                            // Sort the notifications by datetime (most recent first)
                            Collections.sort(notificationList, (n1, n2) -> n2.getDatetime().compareTo(n1.getDatetime()));

                            // Update the ListView
                            List<String> displayList = new ArrayList<>();
                            for (Notification notification : notificationList) {
                                String formattedNotification = notification.getBody() + " for " + notification.getTitle();
                                displayList.add(formattedNotification);
                            }
                            notificationAdapter.clear();
                            notificationAdapter.addAll(displayList);
                            notificationAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("NotificationsFragment", "Error fetching notifications", task.getException());
                    }
                });
    }

    private Date convertStringToDate(String datetimeStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            return format.parse(datetimeStr); // Parse the string into a Date object
        } catch (Exception e) {
            Log.e("NotificationsFragment", "Error parsing datetime string", e);
            return null;
        }
    }

    private void deleteNotification(@NonNull String notificationID) {
        db.collection("notification")
                .whereEqualTo("notificationInfo.notificationID", notificationID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("NotificationsFragment", "Notification deleted: " + notificationID);
                                        fetchNotifications(); // Refresh the ListView
                                    })
                                    .addOnFailureListener(e -> Log.e("NotificationsFragment", "Error deleting notification", e));
                        }
                    } else {
                        Log.e("NotificationsFragment", "Error finding notification to delete", task.getException());
                    }
                });
    }

    private void setupNotificationSwitch() {
        DocumentReference userDocRef = db.collection("user").document(deviceID);

        userDocRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("userInfo.notifications")) {
                        Boolean notificationsEnabled = documentSnapshot.getBoolean("userInfo.notifications");
                        notificationSwitch.setChecked(notificationsEnabled != null ? notificationsEnabled : true);
                    } else {
                        notificationSwitch.setChecked(true);
                        userDocRef.update("userInfo.notifications", true)
                                .addOnSuccessListener(aVoid -> System.out.println("Default notification setting created."))
                                .addOnFailureListener(e -> System.err.println("Error creating default notification setting: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> System.err.println("Error fetching userInfo: " + e.getMessage()));

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userDocRef.update("userInfo.notifications", isChecked)
                    .addOnSuccessListener(aVoid -> {
                        String status = isChecked ? "enabled" : "disabled";
                        System.out.println("Notifications " + status);
                    })
                    .addOnFailureListener(e -> System.err.println("Error updating notifications: " + e.getMessage()));
        });
    }

    // Notification class to hold the details of each notification
    private static class Notification {
        private String title;
        private String body;
        private String notificationID;
        private Date datetime;

        public Notification(String title, String body, String notificationID, Date datetime) {
            this.title = title;
            this.body = body;
            this.notificationID = notificationID;
            this.datetime = datetime;
        }

        public String getBody() {
            return body;
        }

        public String getTitle() {
            return title;
        }

        public String getNotificationID() {
            return notificationID;
        }

        public Date getDatetime() {
            return datetime;
        }
    }
}
