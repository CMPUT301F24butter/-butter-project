package com.example.butter;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class QRCodeDB {
    private FirebaseFirestore db;
    private CollectionReference QRCodeRef;

    public QRCodeDB() {
        db = FirebaseFirestore.getInstance();
        QRCodeRef = db.collection("QRCode");
    }

    /**
     * Turns a QR Code bitmap to a string, and stores it into firebase
     * @param bitmap
     *      bitmap to be converted and stored as a string
     * @param eventID
     *      eventID of the event this QR code is associated with
     */
    public void add(Bitmap bitmap, String eventID) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();

        String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT); // String equivalent to bitmap

        QRCodeRef.document(eventID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) { // checking if there is already a QR code for this event
                        Log.d("Firebase", "QR Code already exists for this event"); // if so, do nothing
                    } else { // otherwise
                        HashMap<String, String> data = new HashMap<>();
                        data.put("QRCodeString", base64Image);

                        QRCodeRef.document(eventID).set(data); // store the string

                        QRCodeRef
                                .document(eventID)
                                .set(data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("Firebase", "QR Code added successfully");
                                    }
                                });
                    }
                }
            }
        });
    }

    /**
     * Deletes the QR Code associated with a eventID
     * @param eventID
     *      eventID of the event whose QR code should be deleted
     */
    public void delete(String eventID) {
        QRCodeRef.document(eventID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) { // checking that the QR code exists
                        QRCodeRef.document(eventID).delete() // if so, deleting it
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("Firebase", "QR Code deleted successfully");
                                    }
                                });
                    } else { // otherwise, do nothing
                        Log.d("Firebase", "QR Code for this event does not exist");
                    }
                }
            }
        });
    }
}
