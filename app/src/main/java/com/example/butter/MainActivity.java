package com.example.butter;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.butter.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

/**
 * This activity is used to implement the navigation bar of the app with menu icons that
 * will have constraints depending on a users privileges.
 * User info is taken from firebase using a collection reference to the user document.
 *
 * @author Angela Dakay (angelcache)
 */
public class MainActivity extends AppCompatActivity {
    /**
     * Used for binding to main activity
     */
    private ActivityMainBinding binding;
    /**
     * Used for accessing the database (privileges specifically)
     */
    private FirebaseFirestore db;
    /**
     * Also used for accessing the database (privileges specifically)
     */
    private CollectionReference userRef;
    /**
     * String to store the deviceID for the user in an attribute.
     * Is later passed to each fragment.
     */
    private CollectionReference notificationRef;
    /**
     * Used to reference the notification collection and see if there are any updates
     */
    private String deviceID;
    /**
     * String to store the privileges for the user.
     * Is updated upon a change to the user in database with "addSnapshotListener".
     */
    private String privileges;

    /**
     * OnCreate method: sets up the menu bar by listening to privilege changes
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        userRef = db.collection("user");

        notificationRef = db.collection("notification");

        // Create notification channel
        createNotificationChannel();

        // Set system bars insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listenForPrivilegesChange();
        replaceFragment(new HomeFragment());
        invalidateOptionsMenu();

        listenForNotificationUpdates();

    }

    /**
     * Listens to privilege changes: finds the users privileges from document and calls
     * setupBottomNavigationView() which restricts the menu bar access based off the new
     * privilege
     */
    private void listenForPrivilegesChange() {
        // Fetch user privileges and set up bottom navigation based on privileges
        userRef.document(deviceID).addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                privileges = documentSnapshot.getString("userInfo.privilegesString");
                setupBottomNavigationView(); // Refresh navigation view based on new privileges
            }
        });
    }

    /**
     * Changes menu bar access according to their new role.
     *      if user changes from entrant to organizer: hides the home icon + shows ticket menu icon
     *      If user changes from organizer to entrant: hides the ticket icon + shows home menu icon
     *      Else if user is both or admin + organizer: all menu icons are accessible
     */
    private void setupBottomNavigationView() {
        // Hide the home menu if user is an organizer, or events menu if user is an entrant
        Menu menu = binding.bottomNavigationView.getMenu();

        if (privileges.equals("100") || privileges.equals("400") || privileges.equals("500")) {
            menu.findItem(R.id.homeIcon).setVisible(true);
            menu.findItem(R.id.eventsIcon).setVisible(false);

        } else if (privileges.equals("200")) {
            menu.findItem(R.id.homeIcon).setVisible(false);
            menu.findItem(R.id.eventsIcon).setVisible(true);
        } else {
            menu.findItem(R.id.homeIcon).setVisible(true);
            menu.findItem(R.id.eventsIcon).setVisible(true);
        }

        // The bottom navigation leads to different fragments
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.homeIcon) {
                replaceFragment(new HomeFragment());
            } else if (id == R.id.eventsIcon) {
                replaceFragment(new EventsFragment());
            } else if (id == R.id.notificationsIcon) {
                replaceFragment(new NotificationsFragment());
            } else if (id == R.id.profileIcon) {
                replaceFragment(new ProfileFragment());
            }
            return true;
        });
    }

    /**
     * replaceFragment simply takes in the fragment to replace,
     * and changes to the corresponding fragment using fragmentTransaction.commit().
     * Note that we also create a Bundle to pass arguments,
     * specifically passing the deviceID for use in other fragments (if needing user info).
     */
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        Bundle args = new Bundle();
        args.putString("deviceID", deviceID);
        fragment.setArguments(args);
        fragmentTransaction.commit();
    }

    private void createNotificationChannel() {
        // Check if the OS version is Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Butter Notifications";
            String description = "Channel for Butter app notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("butter_notifications", name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void listenForNotificationUpdates() {
        notificationRef.addSnapshotListener((querySnapshot, e) -> {
            if (e != null) {
                Log.e("MainActivity", "Listen failed: ", e);
                return;
            }

            Log.d("MainActivity", "Snapshot listener triggered");

            if (querySnapshot != null) {
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Log.d("MainActivity", "Document fetched: " + doc.getData());

                    // Extract data from the document
                    String recipientDeviceID = doc.getString("notificationInfo.recipientDeviceID");
                    String title = doc.getString("notificationInfo.eventSender");
                    String body = doc.getString("notificationInfo.message");
                    Boolean seen = doc.getBoolean("notificationInfo.seen");
                    Boolean force = doc.getBoolean("notificationInfo.force");

                    // Check if the notification is intended for this device and has not been seen
                    if (recipientDeviceID != null && recipientDeviceID.equals(deviceID) && seen != null && !seen) {
                        // Fetch user notification preferences from the "users" collection
                        userRef.document(deviceID).get().addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                DocumentSnapshot userDoc = task.getResult();
                                Boolean notifications = userDoc.getBoolean("userInfo.notifications");

                                // Log the fetched preferences
                                Log.d("MainActivity", "Force: " + force + ", Notifications: " + notifications);

                                // Send the notification based on `force` or `notifications`
                                if (Boolean.TRUE.equals(force)) {
                                    Log.d("MainActivity", "Force sending notification: " + title);
                                    NotificationManagerHelper.handleNotification(this, title, body);

                                    // Mark the notification as seen
                                    doc.getReference().update("notificationInfo.seen", true)
                                            .addOnSuccessListener(aVoid -> Log.d("MainActivity", "Notification marked as seen"));
                                } else if (Boolean.TRUE.equals(notifications)) {
                                    Log.d("MainActivity", "Sending notification based on user settings: " + title);
                                    NotificationManagerHelper.handleNotification(this, title, body);

                                    // Mark the notification as seen
                                    doc.getReference().update("notificationInfo.seen", true)
                                            .addOnSuccessListener(aVoid -> Log.d("MainActivity", "Notification marked as seen"));
                                } else {
                                    doc.getReference().update("notificationInfo.seen", true);
                                    Log.d("MainActivity", "Notification not sent: force=false and user notifications are disabled.");
                                }
                            } else {
                                Log.e("MainActivity", "Failed to fetch user preferences", task.getException());
                            }
                        });
                    }
                }
            }
        });
    }

}
