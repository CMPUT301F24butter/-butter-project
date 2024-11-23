package com.example.butter;

public class Notification {
    private String notificationID;
    private String eventSender;
    private String eventSenderID;
    private String recipientDeviceID;
    private String message;
    private String datetime;
    private boolean invitation;

    public Notification(String notificationID, String eventSender, String eventSenderID, String recipientDeviceID, String message, boolean invitation) {
        this.notificationID = notificationID;
        this.eventSender = eventSender;
        this.eventSenderID = eventSenderID;
        this.recipientDeviceID = recipientDeviceID;
        this.message = message;
        this.invitation = invitation;
    }

    public String getNotificationID() {
        return notificationID;
    }

    public String getEventSender() {
        return eventSender;
    }

    public String getEventSenderID() {
        return eventSenderID;
    }

    public String getRecipientDeviceID() {
        return recipientDeviceID;
    }

    public String getMessage() {
        return message;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public boolean isInvitation() {
        return invitation;
    }

    public String isInvitationString() {
        if (invitation) {
            return "true";
        }
        return "false";
    }

    public void setInvitation(boolean invitation) {
        this.invitation = invitation;
    }
}
