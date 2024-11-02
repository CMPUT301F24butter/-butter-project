package com.example.butter;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // Need access to all events, users, and posters
    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference userRef;

    String deviceID;

    // Lists of events, users, and posters
    private ArrayList<Event> eventsList;
    private ArrayList<User> usersList;

    public HomeFragment() {
        eventsList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("event"); // event collection
        userRef = db.collection("user");
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Bundle args = getArguments();
        deviceID = args.getString("deviceID");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // get access to elements in the xml
        TextView greetingText = view.findViewById(R.id.greetingText);
        TextView qrCodeText = view.findViewById(R.id.qrCodeText);
        TextView upcomingText = view.findViewById(R.id.upcomingText);
        HorizontalScrollView upcomingScrollView = view.findViewById(R.id.horizontalScrollView);
        TextView waitingText = view.findViewById(R.id.waiting_list_label);
        HorizontalScrollView waitingScrollView = view.findViewById(R.id.waitingListScrollView);

        RelativeLayout adminSpinnerLayout = view.findViewById(R.id.admin_spinner_layout);
        Spinner adminSpinner = view.findViewById(R.id.entrants_spinner);
        RecyclerView adminRecyclerView = view.findViewById(R.id.admin_recycler_view);

        // Populating the spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, new String[]{"Events", "Profiles", "Images", "Entrants Page"});
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adminSpinner.setAdapter(spinnerAdapter);

        // depending on users privileges, user sees either browsing or upcoming/waiting list
        userRef.document(deviceID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        String privileges = doc.getString("userInfo.privilegesString");

                        // If user is an admin / admin + organizer, they get browse abilities
                        if (Objects.equals(privileges, "400") || Objects.equals(privileges, "600")) {
                            adminSpinnerLayout.setVisibility(View.VISIBLE);
                            adminRecyclerView.setVisibility(View.VISIBLE);

                        // If user is an entrant / entrant + organizer, only has upcoming / waiting list
                        } else if (Objects.equals(privileges, "100") || Objects.equals(privileges, "300")) {
                            greetingText.setVisibility(View.VISIBLE);
                            qrCodeText.setVisibility(View.VISIBLE);
                            upcomingText.setVisibility(View.VISIBLE);
                            upcomingScrollView.setVisibility(View.VISIBLE);
                            waitingText.setVisibility(View.VISIBLE);
                            waitingScrollView.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });
        return view;
    }
}