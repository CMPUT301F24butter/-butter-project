package com.example.butter;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.platform.app.InstrumentationRegistry.getArguments;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

/**
 * This activity displays the Map for this activity using OpenStreetMap (OSM)
 * The map shows the location where each entrant has scanned the QR code and entered the waiting list
 * Generates markers for each entrant
 *
 *
 * @author Arsalan Firoozkoohi (arsalan-firoozkoohi)
 */

public class ViewMap  extends AppCompatActivity{
    private FirebaseFirestore db;
    private CollectionReference mapRef;
    private String eventID;

    MapView map = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_screen);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        eventID = getIntent().getExtras().getString("eventID"); // clicked eventID
        db = FirebaseFirestore.getInstance();
        mapRef = db.collection("map"); // map collection

        map = findViewById(R.id.mapview);
        map.setTileSource(TileSourceFactory.MAPNIK);
        IMapController mapController = map.getController();
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        mapController.setZoom((long) 12);
        GeoPoint startPoint = new GeoPoint(53.51, -113.50);
        mapController.animateTo(startPoint);

        loadMarkers(eventID);

        // setting click listener for the back button
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // returning to the previous page
            }
        });
    }

    // Loads Markers
    private void loadMarkers(String eventID) {
        DocumentReference docRef = mapRef.document(eventID);

        // Fetch map data associated with this eventID
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        String sizeString = doc.getString("size");
                        if (sizeString != null) {
                            int size = Integer.parseInt(sizeString); // Number of users on the map

                            for (int i = 0; i < size; i++) {
                                // Get each user's location (deviceID lat lon) from Firestore
                                String idLatLon = doc.getString("user" + i);
                                if (idLatLon != null) {
                                    String[] split = idLatLon.split("\\s+");
                                    String deviceID = split[0];
                                    double lat = Double.parseDouble(split[1]);
                                    double lon = Double.parseDouble(split[2]);

                                    // Create a marker for each user
                                    createMarker(lat, lon, "Entrant");
                                    map.invalidate();
                                }
                            }
                        }
                    } else {
                        Log.d("Firebase", "No map associated with this event ID");
                    }
                } else {
                    Log.d("Firebase", "Error getting document: ", task.getException());
                }
            }
        });
    }

    /**
     * Create a marker on the map at the given latitude and longitude
     */
    private void createMarker(double lat, double lon, String label) {
        // Create a GeoPoint from latitude and longitude
        GeoPoint geoPoint = new GeoPoint(lat, lon);

        // Create a marker
        Marker marker = new Marker(map);
        marker.setPosition(geoPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // Set a title for the marker (using the deviceID as the title)
        marker.setTitle(label);

        // Add the marker to the map
        map.getOverlays().add(marker);
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

}




