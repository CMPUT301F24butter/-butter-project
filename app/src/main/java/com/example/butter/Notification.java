package com.example.butter;

/**
 * This is the main model class for notifications
 * @author Ahmer
 */

public class Notification {
    private String notificationID;
    private String eventSender;
    private String eventSenderID;
    private String recipientDeviceID;
    private String message;
    private String datetime;
    private Boolean seen;
    private Boolean force;
    private String eventImage;

    public Notification(String notificationID, String eventSender, String eventSenderID, String recipientDeviceID, String message, boolean force) {
        this.notificationID = notificationID;
        this.eventSender = eventSender;
        this.eventSenderID = eventSenderID;
        this.recipientDeviceID = recipientDeviceID;
        this.message = message;
        this.seen = false;
        this.force = force;
    }

    public Notification(String notificationID, String eventSender, String message, String datetime) {
        this.notificationID = notificationID;
        this.eventSender = eventSender;
        this.message = message;
        this.datetime = datetime;
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

    public Boolean getSeen() {
        return seen;
    }

    public String getSeenString() {
        if (seen) {
            return "true";
        }
        return "false";
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public Boolean getForce() {
        return force;
    }

    public String getForceString() {
        if (force) {
            return "true";
        }
        return "false";
    }

    public void setForce(Boolean force) {
        this.force = force;
    }

    public String getEventImage() {
        return eventImage;
    }

    public void setEventImage(String eventImage) {
        this.eventImage = eventImage;
    }
}
