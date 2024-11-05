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

    public void add(Bitmap bitmap, String eventID) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();

        String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        QRCodeRef.document(eventID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Log.d("Firebase", "QR Code already exists for this event");
                    } else {
                        HashMap<String, String> data = new HashMap<>();
                        data.put("QRCodeString", base64Image);

                        QRCodeRef.document(eventID).set(data);

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

    public void delete(String eventID) {
        QRCodeRef.document(eventID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        QRCodeRef.document(eventID).delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("Firebase", "QR Code deleted successfully");
                                    }
                                });
                    } else {
                        Log.d("Firebase", "QR Code for this event does not exist");
                    }
                }
            }
        });
    }
}
