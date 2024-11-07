package com.example.butter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * This is the dialog box for organizer options
 * This dialog box has options such as edit event, see QR code, see users in the event's lists, etc.
 * When one of these options is clicked, the user is directed to the corresponding page
 *
 * Current outstanding issues: Not all features in this dialog have been implemented, some of the buttons do nothing
 *
 * @author Nate Pane (natepane)
 */
public class OrganizerOptions extends DialogFragment {

    /**
     * eventID string for the current event shown
     */
    private String eventID;
    /**
     * deviceID string for the current user hosting this event
     */
    private String deviceID;

    public OrganizerOptions(String eventID, String deviceID) {
        this.eventID = eventID;
        this.deviceID = deviceID;
    }

    /**
     * onCreateDialog simply inflates the fragment for showing Organizer Options,
     * which then handles an onClickListener for each of the options shown:
     * editEvent: go to {@link EditEventActivity} to edit the corresponding event.
     * viewEntrants: go to {@link EntrantListsActivity} to show and work with entrants for the event.
     * showDetailsCode: go to {@link OrganizerShowCodeActivity} to display event details such as the QRCode.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getContext()).inflate(R.layout.organizer_options_dialog, null);

        // getting text boxes
        TextView editEvent = view.findViewById(R.id.edit_event_text);
        TextView viewEntrants = view.findViewById(R.id.view_entrants_text);
        TextView showDetailsCode = view.findViewById(R.id.show_details_code_text);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // setting click listener for 'edit event' text
        editEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), EditEventActivity.class);
                intent.putExtra("deviceID", deviceID);
                intent.putExtra("eventID", eventID);
                startActivity(intent);
            }
        });

        // setting click listener for 'view entrants' text
        viewEntrants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), EntrantListsActivity.class);
                intent.putExtra("deviceID", deviceID);
                intent.putExtra("eventID", eventID);
                startActivity(intent);
            }
        });

        // setting click listener for 'show details code' text
        showDetailsCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), OrganizerShowCodeActivity.class);
                intent.putExtra("deviceID", deviceID);
                intent.putExtra("eventID", eventID);
                startActivity(intent);
            }
        });

        return builder
                .setView(view)
                .setNeutralButton("Ok", null)
                .create();
    }
}
