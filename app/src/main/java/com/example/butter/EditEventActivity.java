package com.example.butter;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * This activity is used to edit the details of an event
 * Called from {@link OrganizerOptions}, which passes the eventID as an argument.
 * {@link OrganizerOptions} is traversed to from the {@link EventDetailsActivity} with the corresponding event being viewed.
 * Very similar to {@link CreateEventFragment}, but instead provides options to edit an existing event.
 * Once again, all event details are validated before officially updating
 * When complete, the event details are updated in firebase
 * Organizer's can not change the name of an event
 *
 * Current outstanding issues: need to implement poster images
 *
 * @author Nate Pane (natepane)
 */
public class EditEventActivity extends AppCompatActivity {

    /**
     * Database object for access to the database
     */
    private FirebaseFirestore db;
    /**
     * Collection reference from the database for events.
     */
    private CollectionReference eventRef;
    /**
     * Collection reference from the database for images.
     */
    private CollectionReference imageRef;
    /**
     * TextView for event name (since this cannot be changed)
     */
    TextView eventNameText;
    /**
     * EditText for opening reg date
     */
    TextView registrationOpenText;
    /**
     * EditText for closing reg date
     */
    TextView registrationCloseText;
    /**
     * EditText for date of event
     */
    TextView dateText;
    /**
     * EditText for description of event
     */
    EditText descriptionText;
    /**
     * EditText for capacity of event
     */
    TextView capacityText;
    /**
     * SwitchCompat for the geolocation switch
     */
    SwitchCompat geolocationSwitch;
    /**
     * A string for the event name itself
     */
    String eventName;

    ActivityResultLauncher<Intent> resultLauncher;
    /**
     * ImageView for the event image
     */
    ImageView eventImage;
    /**
     * Uri of any image uploaded by the user, null if no image was uploaded
     */
    Uri uriSelected = null;
    /**
     * Button to upload an image from your gallery
     */
    ImageButton uploadImageButton;
    /**
     * Button to remove uploaded image
     */
    ImageButton deleteImageButton;
    /**
     * Boolean to represent if the user deleted an existing image
     */
    boolean deletedImage = false;

    String originalOpenDate;
    String originalCloseDate;
    String originalDate;

    /**
     * onCreate method performs everything here.
     * We simply grab the eventID from the args passed over,
     * which we then grab all of the attributes for the event from the database,
     * and update these attributes for our EditTexts/Geo-switch.
     * Two onClickListeners:
     * saveButton: Once clicked, perform all validity checks on the newly updated event info,
     * which if valid, update the event in the event database.
     * (Note: no new QRCode is needed since it is linked to the eventID itself)
     * backButton: simply sends the user back to the previous screen {@link EventDetailsActivity},
     * and no changes are made to the event.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_event);

        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("event"); // event collection
        imageRef = db.collection("image"); // image collection

        String deviceID = getIntent().getExtras().getString("deviceID"); // logged in deviceID
        String eventID = getIntent().getExtras().getString("eventID"); // clicked eventID

        // getting all input boxes
        eventNameText = findViewById(R.id.event_name);
        registrationOpenText = findViewById(R.id.event_start_date);
        registrationCloseText = findViewById(R.id.end_date);
        dateText = findViewById(R.id.event_date);
        descriptionText = findViewById(R.id.event_description);
        capacityText = findViewById(R.id.max_entrants);
        geolocationSwitch = findViewById(R.id.location_switch);
        eventImage = findViewById(R.id.event_image);
        uploadImageButton = findViewById(R.id.change_image_button);
        deleteImageButton = findViewById(R.id.delete_image);

        registrationOpenText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(registrationOpenText);
            }
        });

        registrationCloseText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(registrationCloseText);
            }
        });

        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(dateText);
            }
        });

        uploadImageButton.setOnClickListener(view -> pickImage());
        registerResult();

        // retrieving data for this event
        eventRef.document(eventID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        DateFormatter dateFormatter = new DateFormatter();
                        // retrieving current event details and setting the input boxes with these details
                        eventNameText.setText(doc.getString("eventInfo.name"));
                        eventName = doc.getString("eventInfo.name");

                        String openDate = doc.getString("eventInfo.registrationOpenDate");
                        originalOpenDate = openDate;
                        String formattedOpenDate = dateFormatter.formatDate(openDate);
                        if (formattedOpenDate != null) {
                            registrationOpenText.setText(formattedOpenDate);
                        } else {
                            registrationOpenText.setText(openDate);
                        }

                        String closeDate = doc.getString("eventInfo.registrationCloseDate");
                        originalCloseDate = closeDate;
                        String formattedCloseDate = dateFormatter.formatDate(closeDate);
                        if (formattedCloseDate != null) {
                            registrationCloseText.setText(formattedCloseDate);
                        } else {
                            registrationCloseText.setText(closeDate);
                        }

                        String date = doc.getString("eventInfo.date");
                        originalDate = date;
                        String formattedDate = dateFormatter.formatDate(date);
                        if (formattedDate != null) {
                            dateText.setText(formattedDate);
                        } else {
                            dateText.setText(date);
                        }

                        descriptionText.setText(doc.getString("eventInfo.description"));

                        String capacity = doc.getString("eventInfo.capacityString");
                        if (capacity != null) {
                            capacityText.setText(capacity);
                        } else {
                            capacityText.setText("N/A");
                        }

                        // setting the geolocation switch
                        String geolocation = doc.getString("eventInfo.geolocationString");
                        if (Objects.equals(geolocation, "true")) {
                            geolocationSwitch.setChecked(true);
                        }
                        geolocationSwitch.setEnabled(false); // disabling the geolocation switch
                    }
                }
            }
        });

        // retrieving image data for this event
        imageRef.document(eventID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        String base64string = doc.getString("imageData"); // fetching image string data
                        ImageDB imageDB = new ImageDB();
                        Bitmap bitmap = imageDB.stringToBitmap(base64string); // turning string data into a bitmap
                        eventImage.setImageBitmap(bitmap); // displaying the image
                    }
                }
            }
        });

        // setting click listener for the back button
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // returning to the previous page
            }
        });

        // setting click listener for the delete image button
        deleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // setting the image back to the blank grey box
                int color = ContextCompat.getColor(getApplicationContext(), R.color.secondaryGreyColor);
                eventImage.setBackgroundColor(color);
                eventImage.setImageDrawable(null);

                uriSelected = null;
                deletedImage = true; // flagging that in image was deleted
            }
        });

        // setting click listener for the save button
        Button saveButton = findViewById(R.id.edit_event_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean validDetails = true; // represents if the event details are valid or not
                DateFormatter dateFormatter = new DateFormatter();
                // retrieving inputted event details
                String registrationOpenDate = registrationOpenText.getText().toString();
                registrationOpenDate = dateFormatter.unformatDate(registrationOpenDate);

                String registrationCloseDate = registrationCloseText.getText().toString();
                registrationCloseDate = dateFormatter.unformatDate(registrationCloseDate);

                String date = dateText.getText().toString();
                date = dateFormatter.unformatDate(date);

                String eventDescription = descriptionText.getText().toString();
                String maxCapacityString = capacityText.getText().toString();
                Boolean geolocation = geolocationSwitch.isChecked();

                int maxCapacity = -1; // default capacity if capacity isn't set
                if (!maxCapacityString.equals("N/A")) { // if a max capacity was inputted
                    maxCapacity = Integer.parseInt(maxCapacityString);
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
                    Boolean bool3 = date3.after(today);
                    //Boolean bool3 = date1.after(today);

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

                if (validDetails) { // if the event details are valid, update the event in the database
                    Event event = new Event(eventName, deviceID, registrationOpenDate, registrationCloseDate, date, maxCapacity, geolocation, eventDescription);

                    // retrieving image data associated with this event
                    imageRef.document(eventID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot doc = task.getResult();
                                ImageDB imageDB = new ImageDB();
                                if (doc.exists()) { // if there is image data associated with this event
                                    if (uriSelected != null) { // if the user selected a new image
                                        imageDB.update(uriSelected, eventID, getApplicationContext()); // update the image in firebase
                                    } else if (deletedImage) { // if the user deleted an image
                                        imageDB.delete(eventID); // delete it from firebase
                                    }

                                    EventDB eventDB = new EventDB();
                                    eventDB.update(event);

                                    finish(); // returning to the previous screen
                                } else { // if there is no image data associated with this event
                                    if (uriSelected != null) { // if the user selected a new image
                                        imageDB.add(uriSelected, eventID, getApplicationContext()); // add the image in firebase
                                    }

                                    EventDB eventDB = new EventDB();
                                    eventDB.update(event);

                                    finish(); // returning to the previous screen
                                }
                            }
                        }
                    });
                }
            }
        });
    }

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

                            uriSelected = imageUri; // marking that this is the most recently selected image Uri

                        } catch (Exception e) {
                            System.out.println("Error");
                        }
                    }
                }
        );
    }

    private void pickImage() {
        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        resultLauncher.launch(intent);
    }

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

        DatePickerDialog datePickerDialog = new DatePickerDialog(EditEventActivity.this, new DatePickerDialog.OnDateSetListener() {
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
