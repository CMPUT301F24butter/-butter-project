package com.example.butter;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private FirebaseFirestore db;
    private String deviceID;
    private ListView notificationListView;
    private List<String> notificationList;
    private ArrayAdapter<String> notificationAdapter;

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

        notificationList = new ArrayList<>();
        notificationAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, notificationList);
        notificationListView.setAdapter(notificationAdapter);

        db = FirebaseFirestore.getInstance();

        fetchNotifications();

        return view;
    }

    /**
     * Fetch notifications from Firestore for the current user and display them in the ListView
     */
    private void fetchNotifications() {
        // Fetch notifications where recipientDeviceID matches the current deviceID
        db.collection("notification")
                .whereEqualTo("notificationInfo.recipientDeviceID", deviceID) // Filter by recipientDeviceID
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            // Iterate through the notifications and add them to the list
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                String title = document.getString("notificationInfo.eventSender");
                                String body = document.getString("notificationInfo.message");
                                addNotificationToList(title, body);
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
            String notification = body + " \n\nNotification for the event: " + title + "\n";
            notificationList.add(notification);
        }
    }
}
