package com.example.butter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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

public class OrganizerShowCodeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference userRef;

    TextView eventNameText;
    TextView registrationOpenText;
    TextView registrationCloseText;
    TextView eventDateText;
    TextView facilityText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_code);

        String deviceID = getIntent().getExtras().getString("deviceID"); // logged in deviceID
        String eventID = getIntent().getExtras().getString("eventID"); // logged in deviceID

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("event");
        userRef = db.collection("user");

        eventNameText = findViewById(R.id.event_title);
        registrationOpenText = findViewById(R.id.reg_open_text);
        registrationCloseText = findViewById(R.id.reg_close_text);
        eventDateText = findViewById(R.id.event_date);
        facilityText = findViewById(R.id.event_place);

        eventRef.document(eventID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                eventNameText.setText(doc.getString("eventInfo.name"));
                String openDate = doc.getString("eventInfo.registrationOpenDate");
                registrationOpenText.setText(String.format("Registration Opens: %s", openDate));
                String closeDate = doc.getString("eventInfo.registrationCloseDate");
                registrationCloseText.setText(String.format("Registration Closes: %s", closeDate));
                String date = doc.getString("eventInfo.date");
                eventDateText.setText(String.format("Event Date: %s", date));
            }
        });

        userRef.document(deviceID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                String place = doc.getString("userInfo.facility");
                facilityText.setText(String.format("Location: %s", place));
            }
        });

        displayQRCode(eventID);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void displayQRCode(String eventID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference QRCodeRef = db.collection("QRCode");

        QRCodeRef.document(eventID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        String base64String = doc.getString("QRCodeString");

                        Bitmap bitmap = stringToBitmap(base64String);

                        ImageView qrCode = findViewById(R.id.details_barcode_image);
                        qrCode.setImageBitmap(bitmap);
                    }
                }
            }
        });
    }

    private Bitmap stringToBitmap(String base64String) {
        byte[] imageBytes = Base64.decode(base64String, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        return bitmap;
    }
}
