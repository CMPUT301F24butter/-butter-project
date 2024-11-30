package com.example.butter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This activity displays the QR code for this activity
 * This QR code can be scanned by a user in order to join the waiting list for the event
 * This activity also displays some event details as well
 *
 * Current outstanding issues: need to implement poster images
 *
 * @author Nate Pane (natepane)
 */
public class OrganizerShowCodeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference userRef;
    private CollectionReference imageRef;

    TextView eventNameText;
    TextView registrationOpenText;
    TextView registrationCloseText;
    TextView eventDateText;
    TextView facilityText;
    ImageView posterImage;
    ImageView qrCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_code);

        String deviceID = getIntent().getExtras().getString("deviceID"); // logged in deviceID
        String eventID = getIntent().getExtras().getString("eventID"); // clicked eventID

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("event"); // event collection
        userRef = db.collection("user"); // user collection
        imageRef = db.collection("image");

        // getting all text boxes
        eventNameText = findViewById(R.id.event_title);
        registrationOpenText = findViewById(R.id.reg_open_text);
        registrationCloseText = findViewById(R.id.reg_close_text);
        eventDateText = findViewById(R.id.event_date);
        facilityText = findViewById(R.id.event_place);
        posterImage = findViewById(R.id.qr_code_image);
        qrCode = findViewById(R.id.details_barcode_image);

        // retrieving event info from firebase
        eventRef.document(eventID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                DateFormatter dateFormatter = new DateFormatter();
                // setting all the text boxes with correct event info
                eventNameText.setText(doc.getString("eventInfo.name"));

                String openDate = doc.getString("eventInfo.registrationOpenDate");
                String formattedOpenDate = dateFormatter.formatDate(openDate);
                if (formattedOpenDate != null) {
                    registrationOpenText.setText(String.format("Registration Opens: %s", formattedOpenDate));
                } else {
                    registrationOpenText.setText(String.format("Registration Opens: %s", openDate));
                }

                String closeDate = doc.getString("eventInfo.registrationCloseDate");
                String formattedCloseDate = dateFormatter.formatDate(closeDate);
                if (formattedCloseDate != null) {
                    registrationCloseText.setText(String.format("Registration Closes: %s", formattedCloseDate));
                } else {
                    registrationCloseText.setText(String.format("Registration Closes: %s", closeDate));
                }

                String date = doc.getString("eventInfo.date");
                String formattedDate = dateFormatter.formatDate(date);
                if (formattedDate != null) {
                    eventDateText.setText(String.format("Event Date: %s", formattedDate));
                } else {
                    eventDateText.setText(String.format("Event Date: %s", date));
                }
            }
        });

        // retrieving image data from firebase
        imageRef.document(eventID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) { // if there is image data associated with this event
                        String base64string = doc.getString("imageData"); // retrieving the image's string data
                        ImageDB imageDB = new ImageDB();
                        Bitmap bitmap = imageDB.stringToBitmap(base64string); // converting the string data into a bitmap

                        posterImage.setImageBitmap(bitmap); // displaying the image
                    }
                }
            }
        });

        // getting the organizer's facility from firebase
        userRef.document(deviceID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                // displaying the organizer's facility
                String place = doc.getString("userInfo.facility");
                facilityText.setText(String.format("Location: %s", place));
            }
        });

        displayQRCode(eventID); // displaying the event's QR code

        // setting a click listener for the back button
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // displays the QR Code for this event
    private void displayQRCode(String eventID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference QRCodeRef = db.collection("QRCode");

        // fetching the QR Code associated to this eventID from firebase
        QRCodeRef.document(eventID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) { // if there is a QR Code associated with this eventID
                        String base64String = doc.getString("QRCodeString"); // retrieving the string

                        Bitmap bitmap = stringToBitmap(base64String); // turning the string into a bitmap

                        qrCode.setImageBitmap(bitmap); // displaying the bitmap
                    } else {
                        generateQRCode(eventID);
                    }
                }
            }
        });
    }

    // function to convert a string into a bitmap
    private Bitmap stringToBitmap(String base64String) {
        byte[] imageBytes = Base64.decode(base64String, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        return bitmap;
    }

    private void generateQRCode(String eventID) {
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            BitMatrix matrix = writer.encode(eventID, BarcodeFormat.QR_CODE, 600, 600);

            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(matrix); // generating the bitmap

            qrCode.setImageBitmap(bitmap);

            QRCodeDB qrCodeDB = new QRCodeDB();
            qrCodeDB.add(bitmap, eventID); // adding this QR Code to firebase

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}
