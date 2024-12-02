package com.example.butter;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NotificationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 * @author Nate Pane (natepane)
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

    String notificationIDSelected;
    int positionSelected;

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

    /**
     * on creation of the fragment
     * Get the notifications from the database for the user, and show
     */
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
                notificationsList.clearChoices();
                notificationsList.invalidateViews();
                adapter.notifyDataSetChanged();

                for (DocumentSnapshot doc : value) {
                    String recipientID = doc.getString("notificationInfo.recipientDeviceID");
                    if (Objects.equals(recipientID, deviceID)) {
                        String notificationID = doc.getString("notificationInfo.notificationID");
                        String eventID = doc.getString("notificationInfo.eventSenderID");
                        String eventName = doc.getString("notificationInfo.eventSender");
                        String message = doc.getString("notificationInfo.message");
                        String datetime = doc.getString("notificationInfo.datetime");

                        Notification notification = new Notification(notificationID, eventName, message, datetime);

                        imageRef.document(eventID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot imageDoc = task.getResult();
                                    if (imageDoc.exists()) {
                                        String imageString = imageDoc.getString("imageData");
                                        notification.setEventImage(imageString);
                                    }
                                    notificationData.add(notification);
                                }
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                                notificationData.sort((n1, n2) -> {
                                    try {
                                        Date date1 = sdf.parse(n1.getDatetime());
                                        Date date2 = sdf.parse(n2.getDatetime());
                                        return date2.compareTo(date1);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                        return 0; // If parsing fails, treat dates as equal
                                    }
                                });
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * Using the notifications fetched in onCreate, create the views (populate the list)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        notificationsSwitch = view.findViewById(R.id.notificationSwitch);
        notificationsList = view.findViewById(R.id.notificationList);
        FloatingActionButton deleteButton = view.findViewById(R.id.delete_button);

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

        notificationsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                notificationsList.setItemChecked(position, true);
                notificationIDSelected = notificationData.get(position).getNotificationID();
                positionSelected = position;
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (notificationIDSelected != null) {
                    NotificationDB notificationDB = new NotificationDB();
                    notificationDB.delete(notificationIDSelected);
                    notificationIDSelected = null;
                }
            }
        });

        return view;
    }

    /**
     * Toggles the on/off switch for notifications
     */
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
