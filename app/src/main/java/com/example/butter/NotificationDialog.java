package com.example.butter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class NotificationDialog extends DialogFragment {

    String eventID;
    String sendGroup;

    EditText messageInput;

    private FirebaseFirestore db;
    private CollectionReference eventRef;
    private CollectionReference userListRef;

    public NotificationDialog(String eventID) {
        this.eventID = eventID;
        db = FirebaseFirestore.getInstance();
        eventRef = db.collection("event");
        userListRef = db.collection("userList");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View view = LayoutInflater.from(getContext()).inflate(R.layout.organizer_notif_dialog, null);

        messageInput = view.findViewById(R.id.dialogue_text);

        Spinner spinner = view.findViewById(R.id.notif_entrant_spinner);
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("Waitlist Entrants");
        arrayList.add("Chosen Entrants");
        arrayList.add("Cancelled Entrants");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, arrayList);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String sendGroupSelected = adapterView.getItemAtPosition(position).toString();
                sendGroup = sendGroupSelected;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Button cancelButton = view.findViewById(R.id.cancel_button);
        Button sendButton = view.findViewById(R.id.send_button);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = messageInput.getText().toString();
                if (message.equals("")) {
                    Toast toast = Toast.makeText(getContext(), "Notification must have a message.", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                String listID;
                if (sendGroup == "Waitlist Entrants") {
                    listID = eventID + "-wait";
                } else if (sendGroup == "Chosen Entrants") {
                    listID = eventID + "-draw";
                } else {
                    listID = eventID + "-cancelled";
                }

                eventRef.document(eventID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot eventDoc) {
                        String eventName = eventDoc.getString("eventInfo.name");

                        userListRef.document(listID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot listDoc) {
                                int size = Integer.parseInt(listDoc.getString("size"));

                                for (int i = 0; i < size; i++) {
                                    String deviceID = listDoc.getString("user" + i);

                                    String notificationID = UUID.randomUUID().toString();
                                    Notification notification = new Notification(notificationID, eventName, eventID, deviceID, message, false);

                                    NotificationDB notificationDB = new NotificationDB();
                                    notificationDB.add(notification);

                                    Toast toast = Toast.makeText(getContext(), "Notification(s) sent.", Toast.LENGTH_SHORT);
                                    toast.show();

                                    getDialog().dismiss();
                                }
                            }
                        });
                    }
                });
            }
        });

        AlertDialog dialog = builder.setView(view).create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent); // makes dialogue background transparent
        return dialog;
    }
}

