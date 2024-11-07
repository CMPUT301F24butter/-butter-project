package com.example.butter;


import android.content.Intent;
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
 * @author Angela
 */
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FirebaseFirestore db;
    private CollectionReference userRef;

    private String deviceID;
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

        // Set system bars insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listenForPrivilegesChange();
        replaceFragment(new HomeFragment());
        invalidateOptionsMenu();
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

        if (privileges.equals("100")) {
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
     * @param fragment
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
}
