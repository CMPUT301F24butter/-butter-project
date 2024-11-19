package com.example.butter;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Uses {@link HomeFragment#newInstance} to create an instance of this fragment.
 *
 * This fragment is used to set up the home screen on the admins side and the entrants (or both
 * entrant and organizers) side. The admins have access to a spinner and the entrants will have
 * access to only the upcoming and waiting list events.
 * Outstanding Issue: When I move to another screen and come back to the admin screen,
 *               the spinner goes back to default option (browse events) instead of the
 *               option user last clicked on
 * @author Angela Dakay (angelcache)
 */
public class HomeFragment extends Fragment {

    // the fragment initialization parameters
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    // Need access to all events, users, and posters, deviceID
    private FirebaseFirestore db;
    private CollectionReference userRef;
    private String deviceID;

    // Variables used for users role
    private Boolean isFacility;
    HomeEntrantFragment entrantFragment;
    HomeAdminFragment adminFragment;

    // Scanner variables
    Button qrScan;
    ActivityResultLauncher<ScanOptions> barLauncher;

    /**
     * Constructor for Home Fragment.
     */
    public HomeFragment() {
        db = FirebaseFirestore.getInstance();
        //eventRef = db.collection("event"); // event collection
        userRef = db.collection("user"); // user collection
    }

    /**
     * Create a new instance of home fragment.
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * onCreate method
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Bundle args = getArguments();
        deviceID = args.getString("deviceID");

        barLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                Intent intent = new Intent(requireContext(), EventDetailsActivity.class);
                intent.putExtra("deviceID", deviceID);
                intent.putExtra("eventID", result.getContents());
                startActivity(intent);
            }
        });
    }

    /**
     * Inflates view and sets up qrScan, admin and entrants XML.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Instantiates the entrant and admin fragment
        entrantFragment = new HomeEntrantFragment(deviceID);
        adminFragment = new HomeAdminFragment("Default");

        checkUserRole(view);

        // Starts the scanner
        qrScan = view.findViewById(R.id.qrScannerButton);
        qrScan.setOnClickListener(v -> {
            scanCode();
        });

        // Populating the spinner
        Spinner adminSpinner = view.findViewById(R.id.entrants_spinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, new String[]{"Browse Events", "Browse Profiles", "Browse Facilities", "Browse Images", "Entrant's Page"});
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adminSpinner.setAdapter(spinnerAdapter);

        if (adminSpinner.getVisibility() == View.VISIBLE) {
            changeSpinnerList(adminSpinner);
        }

        return view;
    }

    /**
     * Notifies HomeAdminFragment if there are changes in the spinner
     * @param adminSpinner used to see the changes that admin fragment needs to be aware of
     */
    private void changeSpinnerList(Spinner adminSpinner) {
        adminSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                // If user selects a new option in spinner, it will show the right list / text
                switch (selection) {
                    case "Browse Events":
                        adminFragment.spinnerBrowseChange("Browse Events");
                        switchFragment(adminFragment);
                        break;
                    case "Browse Profiles":
                        adminFragment.spinnerBrowseChange("Browse Profiles");
                        switchFragment(adminFragment);
                        break;
                    case "Browse Facilities":
                        adminFragment.spinnerBrowseChange("Browse Facilities");
                        switchFragment(adminFragment);
                        break;
                    case "Browse Images":
                        adminFragment.spinnerBrowseChange("Browse Images");
                        switchFragment(adminFragment);
                        break;
                    case "Entrant's Page":
                        switchFragment(entrantFragment);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Checks to see whether the user is an admin or just an entrant. As an entrant they will
     * see the welcome screen.
     * @param view used to find xml items (like greetingText and qrCodeText)
     */
    private void checkUserRole(View view) {
        RelativeLayout adminSpinnerLayout = view.findViewById(R.id.admin_spinner_layout);

        // depending on users privileges, user sees either browsing or upcoming/waiting list page
        userRef.document(deviceID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        String privileges = doc.getString("userInfo.privilegesString");

                        // If a user is an admin + organizer, they get browse abilities + upcoming / waiting list
                        if (Objects.equals(privileges, "400") || Objects.equals(privileges, "500") || Objects.equals(privileges, "600") || Objects.equals(privileges, "700")) {
                            adminSpinnerLayout.setVisibility(View.VISIBLE);
                            switchFragment(adminFragment);

                            // If user is an entrant / entrant + organizer, only has upcoming / waiting list
                        } else if (Objects.equals(privileges, "100") || Objects.equals(privileges, "300")) {
                            String user = doc.getString("userInfo.name");
                            TextView greetingText = view.findViewById(R.id.greetingText);
                            TextView qrCodeText = view.findViewById(R.id.qrCodeText);
                            greetingText.setText(String.format("Hey %s!", user));
                            greetingText.setVisibility(View.VISIBLE);
                            qrCodeText.setVisibility(View.VISIBLE);
                            switchFragment(entrantFragment);
                        }
                    }
                }
            }
        });
    }

    /**
     * Sets up the QR code scanner.
     */
    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    /**
     * Puts Home Admin Fragment / Home Entrants Fragment as the fragment on the home screen
     */
    private void switchFragment(Fragment roleFragment) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.home_frame_layout, roleFragment);
        transaction.commit();
    }
}