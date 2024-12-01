package com.example.butter;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NotificationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotificationsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String deviceID;
    private FirebaseFirestore db;
    private CollectionReference userRef;
    private CollectionReference notificationRef;
    private CollectionReference imageRef;

    SwitchCompat notificationsSwitch;
    ListView notificationsList;
    ArrayList<Notification> notificationData;
    NotificationArrayAdapter adapter;

    public NotificationsFragment() {
        db = FirebaseFirestore.getInstance();
        userRef = db.collection("user");
        notificationRef = db.collection("notification");
        imageRef = db.collection("image");
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NotificationsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NotificationsFragment newInstance(String param1, String param2) {
        NotificationsFragment fragment = new NotificationsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        Bundle args = getArguments();
        deviceID = args.getString("deviceID");

        notificationData = new ArrayList<>();
        adapter = new NotificationArrayAdapter(getContext(), notificationData);



        updateToggle();

        notificationRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                notificationData.clear();
                adapter.notifyDataSetChanged();
                for (DocumentSnapshot doc : value) {
                    String recipientID = doc.getString("notificationInfo.recipientDeviceID");
                    if (Objects.equals(recipientID, deviceID)) {
                        String notificationID = doc.getString("notificationInfo.notificationID");
                        String eventName = doc.getString("notificationInfo.eventSender");
                        String message = doc.getString("notificationInfo.message");

                        Notification notification = new Notification(notificationID, eventName, message);
                        notificationData.add(notification);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        notificationsSwitch = view.findViewById(R.id.notificationSwitch);
        notificationsList = view.findViewById(R.id.notificationList);
        notificationsList.setAdapter(adapter);

        notificationsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean notificationsBool) {
                HashMap<String, Object> update = new HashMap<>();
                update.put("userInfo.notifications", notificationsBool);
                userRef.document(deviceID).update(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("Firebase", "Notification status updated successfully");
                    }
                });
            }
        });

        return view;
    }

    private void updateToggle() {
        userRef.document(deviceID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                boolean notifications = doc.getBoolean("userInfo.notifications");
                notificationsSwitch.setChecked(notifications);
            }
        });
    }
}
