package com.example.butter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class EditEventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_profile);

        String deviceID = getIntent().getExtras().getString("deviceID"); // logged in deviceID
        String eventID = getIntent().getExtras().getString("eventID"); // logged in deviceID

    }
}
