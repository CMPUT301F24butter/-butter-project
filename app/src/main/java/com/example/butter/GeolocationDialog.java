package com.example.butter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Objects;

/**
 * Dialog that informs the user the event requires geolocation and sets up a cancel and yes button
 * that will determine whether user will join the waiting list or not in EventDetailsActivity.
 *
 * @author Angela Dakay (angelcache)
 */

public class GeolocationDialog {
    private Context context;
    private GeolocationDialogListener listener;

    // Constructor
    public GeolocationDialog(Context context, GeolocationDialogListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public interface GeolocationDialogListener {
        void onJoinEventConfirmed(boolean confirmJoin);
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
        Button yesButton = dialogView.findViewById(R.id.yes_button);

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                listener.onJoinEventConfirmed(Boolean.FALSE); // Notify activity about cancellation
            }
        });

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                listener.onJoinEventConfirmed(Boolean.TRUE); // Notify activity about confirmation
            }
        });

        // Show the dialog
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent); // transparent so we only see the customized XML dialogue
        dialog.show();
    }
}