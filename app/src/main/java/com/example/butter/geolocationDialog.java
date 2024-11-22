package com.example.butter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class geolocationDialog {
    private Context context;

    // Constructor
    public geolocationDialog(Context context) {
        this.context = context;
    }

    // Method to show the dialog
    public void showDialog() {
        // Inflate custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.geolocation_dialog, null);

        TextView dialogue = dialogView.findViewById(R.id.dialogue_text);
        TextView title = dialogView.findViewById(R.id.dialogue_title);

        // Customize the dialogue and title
        String dialogText = "Registration for this event requires geolocation. Would you like to continue?";
        dialogue.setText(dialogText);
        String dialogTitle = "Geolocation Sharing Required";
        title.setText(dialogTitle);

        // Find the buttons and set them up
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        Button addButton = dialogView.findViewById(R.id.yes_button);

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // Show the dialog
        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }
}
