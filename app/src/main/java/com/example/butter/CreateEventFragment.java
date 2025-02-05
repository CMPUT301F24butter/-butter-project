package com.example.butter;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import com.bumptech.glide.Glide;

/**
 * This class is for organizers to publish new events
 * A simple activity called from {@link EventsFragment} when the '+' button is clicked.
 * All inputted event details be validated before the event is published
 * When the event is created, the details are saved in firebase
 * When the event is created, a QR Code is generated and stored in firebase
 *
 * Current outstanding issues: need to implement poster images
 *
 * @author Nate Pane (natepane)
 */
public class CreateEventFragment extends AppCompatActivity {

    /**
     * Imagie view for the event image
     */
    ImageView eventImage;
    /**
     * Button to be clicked to select an image from your gallery
     */
    ImageButton uploadEventImage;
    ActivityResultLauncher<Intent> resultLauncher;
    /**
     * Uri of the image uploaded by the user, null if no image is uploaded
     */
    Uri uriUploaded = null;
    /**
     * EditText for the event name
     */
    EditText eventName;
    /**
     * EditText for opening reg date
     */
    TextView openDate;
    /**
     * EditText for closing reg date
     */
    TextView closeDate;
    /**
     * EditText for date of event
     */
    TextView eventDate;
    /**
     * EditText for description of event
     */
    EditText description;
    /**
     * EditText for capacity of event
     */
    EditText capacity;
    /**
     * SwitchCompat for the geolocation switch
     */
    SwitchCompat geolocationSwitch;

    /**
     * Firebase database object for access to the database
     */
    private FirebaseFirestore db;
    /**
     * Firebase database object for access to the database
     */
    private CollectionReference eventRef;

    /**
     * onCreate simply sets up all of our EditTexts,
     * and an onClickListener for the button to create the event.
     * If clicked, perform validity checks on all of the attributes available.
     * If valid, an eventID will be generated, and the event will be added to {@link UserDB}.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_event);

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("event"); // event collection

        String deviceID = getIntent().getExtras().getString("deviceID"); // logged in deviceID

        Button createButton = findViewById(R.id.create_event_button);
        ImageButton backButton = findViewById(R.id.back_button);

        // setting click listener for back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // go back when back button is clicked
            }
        });

        // getting all input boxes
        eventName = findViewById(R.id.name_event);
        openDate = findViewById(R.id.start_date);
        closeDate = findViewById(R.id.end_date);
        eventDate = findViewById(R.id.event_date);
        description = findViewById(R.id.event_description);
        capacity = findViewById(R.id.max_entrants);
        geolocationSwitch = findViewById(R.id.location_switch);
        eventImage = findViewById(R.id.event_image);
        uploadEventImage = findViewById(R.id.change_image_button);

        uploadEventImage.setOnClickListener(view -> pickImage());
        registerResult();

        openDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(openDate);
            }
        });

        closeDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(closeDate);
            }
        });

        eventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(eventDate);
            }
        });

        // setting click listener for the create event button
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean validDetails = true; // represents whether the event details are valid or not
                DateFormatter dateFormatter = new DateFormatter();
                // extracting inputted event details
                String name = eventName.getText().toString();

                String registrationOpenDate = openDate.getText().toString();
                if (registrationOpenDate.isEmpty()) {
                    validDetails = false;
                } else {
                    registrationOpenDate = dateFormatter.unformatDate(registrationOpenDate);
                }

                String registrationCloseDate = closeDate.getText().toString();
                if (registrationCloseDate.isEmpty()) {
                    validDetails = false;
                } else {
                    registrationCloseDate = dateFormatter.unformatDate(registrationCloseDate);
                }

                String date = eventDate.getText().toString();
                if (date.isEmpty()) {
                    validDetails = false;
                } else {
                    date = dateFormatter.unformatDate(date);
                }

                String eventDescription = description.getText().toString();
                String maxCapacityString = capacity.getText().toString();
                Boolean geolocation = geolocationSwitch.isChecked();

                if (name.isEmpty()) { // if no event name is given
                    validDetails = false;
                    Toast toast = Toast.makeText(getApplicationContext(), "Must enter an event name.", Toast.LENGTH_LONG);
                    toast.show();
                }

                if (name.indexOf('-') != -1 || name.indexOf('_') != -1) { // if the event name contains '-' or '_'
                    validDetails = false;
                    Toast toast = Toast.makeText(getApplicationContext(), "Event name cannot contain '-' or '_'", Toast.LENGTH_LONG);
                    toast.show();
                }

                int maxCapacity = -1; // default capacity if capacity isn't set
                if (!maxCapacityString.isEmpty()) { // if a max capacity was inputted
                    maxCapacity = Integer.parseInt(maxCapacityString);
                    if (maxCapacity < 1) { // max capacity cannot be 0
                        validDetails = false;
                        Toast toast = Toast.makeText(getApplicationContext(), "Capacity cannot be 0.", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }

                if (eventDescription.isEmpty()) { // event description cannot be empty
                    validDetails = false;
                    Toast toast = Toast.makeText(getApplicationContext(), "Must provide an event description.", Toast.LENGTH_LONG);
                    toast.show();
                }

                // format for date input
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date todaysDate = new Date();
                String todayString = formatter.format(todaysDate);
                Date date1, date2, date3, today;
                try {
                    date1 = formatter.parse(registrationOpenDate);
                    date2 = formatter.parse(registrationCloseDate);
                    date3 = formatter.parse(date);
                    today = formatter.parse(todayString);

                    Boolean bool1 = date2.after(date1);
                    Boolean bool2 = date3.after(date2);
                    Boolean bool3 = date1.after(today);


                    // confirming that all dates are valid, i.e. event date isn't before registration date
                    if (!bool1 || !bool2 || !bool3) {
                        validDetails = false;
                        Toast toast = Toast.makeText(getApplicationContext(), "Invalid dates.", Toast.LENGTH_LONG);
                        toast.show();
                    }

                } catch (ParseException e) {
                    validDetails = false;
                    Toast toast = Toast.makeText(getApplicationContext(), "Invalid dates.", Toast.LENGTH_LONG);
                    toast.show();
                }


                if (validDetails) { // if event details are valid to this point

                    int cap = maxCapacity;
                    String eventID = name.replace(" ", "_") + "-" + deviceID; // eventID of this potential new event
                    // checking if an event with this ID already exists
                    String finalRegistrationOpenDate = registrationOpenDate;
                    String finalRegistrationCloseDate = registrationCloseDate;
                    String finalDate = date;
                    eventRef.document(eventID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot doc = task.getResult();
                                if (doc.exists()) { // if an event with this ID already exists, don't add the event
                                    Toast toast = Toast.makeText(getApplicationContext(), "Invalid event name.", Toast.LENGTH_LONG);
                                    toast.show();
                                }
                                else { // otherwise, add the event to firebase and return to the previous page

                                    Event event = new Event(name, deviceID, finalRegistrationOpenDate, finalRegistrationCloseDate, finalDate, cap, geolocation, eventDescription);

                                    EventDB eventDB = new EventDB();
                                    eventDB.add(event);

                                    if (uriUploaded != null) { // if the user uploaded an image
                                        ImageDB imageDB = new ImageDB();
                                        imageDB.add(uriUploaded, event.getEventID(), getApplicationContext()); // add the image to firebase
                                    }

                                    if (event.isGeolocation()) { // if the event has geolocation
                                        MapDB mapDB = new MapDB();
                                        mapDB.createMap(event.getEventID()); // create a map docuement for this event
                                    }

                                    generateQRCode(eventID); // generating the QR code for this event

                                    try {
                                        Thread.sleep(300);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }

                                    finish();
                                }
                            }
                        }
                    });
                }
            }
        });

    }

    /**
     * Takes in the eventID, and generates a QRCode for said ID which gets put into the database.
     * This is called once the event being created has been verified, and added to the event database.
     * The outputted bitmap for the QRCode gets put into a QRCode database.
     * @param eventID
     * The eventID corresponding to the event which is currently being created
     */
    private void generateQRCode(String eventID) {
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            BitMatrix matrix = writer.encode(eventID, BarcodeFormat.QR_CODE, 600, 600);

            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(matrix); // generating the bitmap

            QRCodeDB qrCodeDB = new QRCodeDB();
            qrCodeDB.add(bitmap, eventID); // adding this QR Code to firebase

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    /**
     * After grabbing an image from intent, handle it by setting its uri.
     * Update the existing image
     * This method is called upon the return after a call to "pickImage".
     */
    private void registerResult() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        try {
                            Uri imageUri = result.getData().getData(); // getting the Uri of the selected image
                            eventImage.setImageURI(imageUri); // displaying the image

                            uriUploaded = imageUri;

                        } catch (Exception e) {
                            System.out.println("Error");
                        }
                    }
                }
        );
    }

    /**
     * Simply launches an intent to grab the image from a launcher
     * Returns the result to the launcher in registerResult
     */
    private void pickImage() {
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        resultLauncher.launch(intent);
    }

    /**
     * Called upon click on a date.
     * This opens the date picker to pick a date, as well as updates the textView if picked.
     */
    private void showDatePicker(TextView dateTextView) {

        String selectedDate = dateTextView.getText().toString();
        int year, month, day;

        if (selectedDate.isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
        } else {
            DateFormatter dateFormatter = new DateFormatter();
            String unformattedDate = dateFormatter.unformatDate(selectedDate);
            String[] parts = unformattedDate.split("-");

            year = Integer.parseInt(parts[0]);
            month = Integer.parseInt(parts[1]);
            day = Integer.parseInt(parts[2]);

            month--;
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(CreateEventFragment.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month++;
                String dateString = String.format("%04d-%02d-%02d", year, month, day);
                DateFormatter dateFormatter = new DateFormatter();
                String formattedDate = dateFormatter.formatDate(dateString);
                dateTextView.setText(formattedDate);
            }
        }, year, month, day);
        datePickerDialog.show();
    }
}
