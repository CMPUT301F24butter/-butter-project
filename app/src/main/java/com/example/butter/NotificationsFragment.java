package com.example.butter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NotificationsFragment newInstance} factory method to
 * create an instance of this fragment.
 */
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
        db.collection("notification")
                .whereEqualTo("recipientDeviceID", deviceID) // Filter notifications by deviceID
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                String title = document.getString("eventSender");
                                String body = document.getString("message");

                                // Create a concatenated string "title + body" and add it to the list
                                if (title != null && body != null) {
                                    String notification = title + ": " + body;
                                    notificationList.add(notification);
                                }
                            }
                            notificationAdapter.notifyDataSetChanged(); // Update the ListView
                        }
                    }
                });
    }
}
