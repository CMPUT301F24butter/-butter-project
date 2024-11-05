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

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FirebaseFirestore db;
    private CollectionReference userRef;

    private String deviceID;
    private String privileges;

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
