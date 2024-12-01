package com.example.butter;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

/**
 * This class can be used to interact with the "notification" collection in Firebase
 *
 * author: Nate Pane (natepane)
 */
public class NotificationDB {
    private FirebaseFirestore db;
    private CollectionReference notificationRef;

    public NotificationDB() {
        db = FirebaseFirestore.getInstance();
        notificationRef = db.collection("notification");
    }

    /**
     * Adds a notification to the notification collection
     * @param notification
     *      new notification to be added
     */
    public void add(Notification notification) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(); // retrieving the current date and time
        String formattedDate = formatter.format(date); // formatting the date and time

        notification.setDatetime(formattedDate);

        HashMap<String, Notification> data = new HashMap<>();
        data.put("notificationInfo", notification); // setting the data

        notificationRef.document(notification.getNotificationID()).set(data);

        notificationRef
                .document(notification.getNotificationID())
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("Firebase", "Notification added successfully");
                    }
                });
    }

    /**
     * Deletes a notification from the notification collection
     * @param notificationID
     *      ID of the notification to be deleted
     */
    public void delete(String notificationID) {

        // fetching notification data for this notification ID
        notificationRef.document(notificationID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) { // if the document exists

                        notificationRef.document(notificationID).delete() // delete it
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("Firebase", "Notification deleted successfully");
                                    }
                                });
                    } else { // if the document doesn't exist, do nothing
                        Log.d("Firebase", "Notification with this ID does not exist");
                    }
                }
            }
        });
    }

    /**
     * Deletes all notifications sent by a specific event
     * @param eventID
     *      eventID of the event whose notifications are being deleted
     */
    public void deleteNotificationsFromEvent(String eventID) {
        // getting all notification data
        notificationRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                for (DocumentSnapshot doc : querySnapshot) { // iterating over all notifications
                    String eventSenderID = doc.getString("notificationInfo.eventSenderID");
                    if (Objects.equals(eventSenderID, eventID)) { // if the senderID = eventID
                        String notificationID = doc.getString("notificationInfo.notificationID");
                        delete(notificationID); // delete the document
                    }
                }
            }
        });
    }

    /**
     * Deletes all notifications sent to a specific user
     * @param deviceID
     *      deviceID of the user whose notifications will be deleted
     */
    public void deleteNotificationsToUser(String deviceID) {
        // getting all notification data
        notificationRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                for (DocumentSnapshot doc : querySnapshot) { // iterating over all notifications
                    String recipientID = doc.getString("notificationInfo.recipientDeviceID");
                    if (Objects.equals(recipientID, deviceID)) { // if recipientID = deviceID
                        String notificationID = doc.getString("notificationInfo.notificationID");
                        delete(notificationID); // delete the document
                    }
                }
            }
        });
    }
}
