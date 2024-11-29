package com.example.butter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This class can be used to interact with the "image" collection in Firebase
 *
 * author: Nate Pane (natepane)
 */
public class ImageDB {
    private FirebaseFirestore db;
    private CollectionReference imageRef;

    public ImageDB() {
        db = FirebaseFirestore.getInstance();
        imageRef = db.collection("image");

    }

    /**
     * Adds a new image to the image collection of there isn't already an event associated with this document ID
     * @param uri
     *      URI returned when the user selects an image from their gallery
     * @param documentID
     *      This document ID should either be a device ID for profile pictures, or event ID for event posters
     * @param context
     */
    public void add(Uri uri, String documentID, Context context) {
        // retrieving data associated with this document ID
        imageRef.document(documentID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) { // if there is already data associated with this document ID
                        Log.d("Firebase", "An image already exists with this ID"); // do nothing
                    } else {
                        String base64string = imageUriToString(uri, context); // turning the Uri into string data
                        HashMap<String, Object> data = new HashMap<>();
                        data.put("imageData", base64string);

                        // setting the data
                        imageRef.document(documentID).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("Firebase", "Imaged added successfully");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("Firebase", "Failed to add image");
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * updates an image in the image collection if there is already an image associated with the document ID
     * @param uri
     *      URI returned when the user selects an image from their gallery
     * @param documentID
     *      This document ID should either be a device ID for profile pictures, or event ID for event posters
     * @param context
     */
    public void update(Uri uri, String documentID, Context context) {
        // retrieving data associated with this document ID
        imageRef.document(documentID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) { // if the document exists
                        HashMap<String, Object> updates = new HashMap<>();
                        String base64string = imageUriToString(uri, context); // turning the new URI into string data
                        updates.put("imageData", base64string); // updating the document
                        imageRef.document(documentID).update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("Firebase", "Image updated successfully");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("Firebase", "Failed to update image");
                            }
                        });
                    } else { // if there is no data associated with this document ID, do nothing
                        Log.d("Firebase", "No image exists with this document ID");
                    }
                }
            }
        });
    }

    /**
     * Deletes an image from the image collection if it exists
     * @param documentID
     *      This document ID should either be a device ID for profile pictures, or event ID for event posters
     */
    public void delete(String documentID) {
        // retrieving data associated with this document ID
        imageRef.document(documentID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) { // if the document exists
                        imageRef.document(documentID).delete().addOnSuccessListener(new OnSuccessListener<Void>() { // deleting the document
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d("Firebase", "Image deleted successfully");
                            }
                        });
                    } else { // if the document doesn't exist, do nothing
                        Log.d("Firebase", "No image associated with this document ID");
                    }
                }
            }
        });
    }

    /**
     * Converts an image URI into string data that can be stored in firebase
     * @param uri
     *      URI returned when the user selects an image from their gallery
     * @param context
     * @return
     *      String equivalent of the image
     */
    private String imageUriToString(Uri uri, Context context) {

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {

            if (inputStream == null) {
                throw new IOException("Unable to open InputStream for URI: " + uri);
            }

            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);

            // Resize the Bitmap
            Bitmap resizedBitmap = resizeBitmap(originalBitmap, 300, 300);

            // Compress the Bitmap to a ByteArrayOutputStream
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);

            byte[] compressedBytes = byteArrayOutputStream.toByteArray();

            // Convert the byte array to a string
            return Base64.encodeToString(compressedBytes, Base64.DEFAULT);
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * resizes the bitmap in order to make the string data small enough to store in Firebase
     * @param original
     *      original Bitmap to be resized
     * @param maxWidth
     * @param maxHeight
     * @return
     *      returns a resized Bitmap for the same image
     */
    private Bitmap resizeBitmap(Bitmap original, int maxWidth, int maxHeight) {
        int width = original.getWidth();
        int height = original.getHeight();

        float scaleWidth = ((float) maxWidth) / width;
        float scaleHeight = ((float) maxHeight) / height;
        float scaleFactor = Math.min(scaleWidth, scaleHeight);

        return Bitmap.createScaledBitmap(original,
                (int) (width * scaleFactor),
                (int) (height * scaleFactor),
                true);
    }

    /**
     * Converts string data into a Bitmap
     * This method is public so that it can be conveniently used after fetching string data of an image from firebase
     * @param base64String
     *      String data of an image
     * @return
     *      Bitmap for the image
     */
    public Bitmap stringToBitmap(String base64String) {
        try {
            // Decode string to byte array
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);

            // Convert the byte array to a Bitmap
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }
}
