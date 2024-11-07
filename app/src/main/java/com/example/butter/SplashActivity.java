package com.example.butter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * This is the first screen that will be shown on boot-up (as specified in AndroidManifest.xml)
 * Uses {@link SplashActivity} to provide a splash screen on boot.
 * This splash screen will show a splash, while checking for which screen to redirect to.
 * Redirect to {@link MainActivity} if the user already exists in the db.
 * Else redirect to {@link CreateProfileActivity} if the user does not exist in the db.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    /**
     * Database object used in order to access if the user exists within the database,
     * given the users deviceID.
     */
    private FirebaseFirestore db;

    /**
     * onCreate method to handle a 2 second delayed splash screen,
     * and deciding if the user is new or not. If new, go to {@link CreateProfileActivity}
     * else go to {@link MainActivity}
     * @param savedInstanceState
     * The last saved state of the activity (if exists,
     * which this should not since SplashActivity only gets called to once on boot)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set to splash screen
        setContentView(R.layout.activity_splash_screen);

        // setup handler to delay on splash before launching
        // also check which activity we need to go to (main if user exists, else create profile)
        new Handler().postDelayed(() -> {

            // initialize our two activities as intents
            Intent toMainActivity = new Intent(SplashActivity.this, MainActivity.class);
            Intent toCreateProfile = new Intent(SplashActivity.this, CreateProfileActivity.class);

            // check if user exists in the database (by deviceID)
            db = FirebaseFirestore.getInstance();
            db.collection("user")
                    .document(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID))
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            // if the user exists, go to mainactivity
                            startActivity(toMainActivity);
                            finish();
                        } else {    // else, call to activity to create new user
                            // put deviceID in args to unpack
                            toCreateProfile.putExtra("deviceID", Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
                            startActivity(toCreateProfile);
                            finish();
                        }
                    });
        }, 2000);   // delay for 2000 ms (2 sec)
    }
}