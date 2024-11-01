package com.example.butter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link AppCompatActivity} subclass.
 * Uses {@link SplashActivity} to provide a splash screen on boot.
 * This splash screen will show a splash, while checking for which screen to redirect to.
 * Redirect to {@link MainActivity} if the user already exists in the db.
 * Else redirect to {@link CreateProfileActivity} if the user does not exist in the db.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private FirebaseFirestore db;

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