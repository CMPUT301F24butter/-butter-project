package com.example.butter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Objects;

/**
 * Dialog shows up everytime the user tries to delete something. It will warn them that the
 * deletion is permanent and determines whether they will delete or not.
 *
 * @author Angela Dakay (angelcache)
 */

public class ConfirmationDialog {
    private Context context;
    private ConfirmationDialogListener listener;
    private String deletedItem;

    /**
     * Confirmation Dialog constructor. Intializes the context, listener and deletedItem
     * @param context activity or fragment dialog is being displayed in
     * @param listener listener that allows dialog to communicate with the activity or fragment
     * @param deletedItem is either Event, Facility, QR Code, or Event Poster
     */
    public ConfirmationDialog(Context context, ConfirmationDialogListener listener, String deletedItem) {
        this.context = context;
        this.listener = listener;
        this.deletedItem = deletedItem;
    }

    /**
     * Defines Confirmation Dialog Listener Interface
     */
    public interface ConfirmationDialogListener {
        void deleteConfirmation(boolean confirmDelete, String deletedItem);
    }

    /**
     * Shows the dialog which asks user if they're sure about deleting the item
     */
    public void showDialog() {
        // Inflate custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.geolocation_dialog, null);

        TextView dialogue = dialogView.findViewById(R.id.dialogue_text);
        TextView title = dialogView.findViewById(R.id.dialogue_title);

        // Customize the dialogue and title
        String dialogText = "Are you sure you want to delete this " + deletedItem + "? You cannot undo your action.";
        dialogue.setText(dialogText);
        String dialogTitle = "Delete";
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
                listener.deleteConfirmation(Boolean.FALSE, deletedItem); // Notify activity about cancellation
            }
        });

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                listener.deleteConfirmation(Boolean.TRUE, deletedItem); // Notify activity about confirmation
            }
        });

        // Show the dialog
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent); // transparent so we only see the customized XML dialogue
        dialog.show();
    }
}