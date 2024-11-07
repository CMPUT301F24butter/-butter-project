package com.example.butter;

/**
 * This is the main model class for events
 *
 * Current outstanding issues: Currently have a getter for the String equivalent of non-string
 *      fields because I don't know how to pull non-string data from Firebase
 */
public class Event {
    private final String eventID;
    private final String name;
    private final String organizerID;
    private String registrationOpenDate;
    private String registrationCloseDate;
    private String date;
    private int capacity;
    private boolean geolocation;
    private String description;

    private final String waitlistID;
    private final String drawListID;
    private final String registeredListID;
    private final String cancelledListID;

    public Event(String name, String organizerID, String registrationOpenDate, String registrationCloseDate, String date, int capacity, boolean geolocation, String description) {
        this.name = name;
        this.organizerID = organizerID;
        this.registrationOpenDate = registrationOpenDate;
        this.registrationCloseDate = registrationCloseDate;
        this.date = date;
        this.capacity = capacity;
        this.geolocation = geolocation;
        this.description = description;

        this.eventID = name.replace(" ", "_") + "-" + organizerID; // eventID = Event_Name-organizerID

        // user list ID = Event_Name-organizerID-listName
        this.waitlistID = this.eventID + "-wait";
        this.drawListID = this.eventID + "-draw";
        this.registeredListID = this.eventID + "-registered";
        this.cancelledListID = this.eventID + "-cancelled";

        createUserLists();
    }

    public Event(String eventID, String name, String date, int capacity) {
        this.eventID = eventID;
        this.name = name;
        this.date = date;
        this.capacity = capacity;

        this.organizerID = null;
        this.waitlistID = null;
        this.drawListID = null;
        this.registeredListID = null;
        this.cancelledListID = null;
    }

    private void createUserLists() {
        UserListDB userListDB = new UserListDB();

        userListDB.create(this.waitlistID, "waitlist");
        userListDB.create(this.drawListID, "draw");
        userListDB.create(this.registeredListID, "registered");
        userListDB.create(this.cancelledListID, "cancelled");
    }

    public String getEventID() {
        return eventID;
    }

    public String getName() {
        return name;
    }

    public String getOrganizerID() {
        return organizerID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getCapacityString() {
        if (capacity == -1) {
            return null;
        }
        return String.valueOf(capacity);
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getWaitlistID() {
        return waitlistID;
    }

    public String getDrawListID() {
        return drawListID;
    }

    public String getRegisteredListID() {
        return registeredListID;
    }

    public String getCancelledListID() {
        return cancelledListID;
    }

    public boolean isGeolocation() { return geolocation; }

    public void setGeolocation(boolean geolocation) { this.geolocation = geolocation; }

    public String getGeolocationString() {
        if (geolocation) {
            return "true";
        }
        return "false";
    }

    public String getRegistrationOpenDate() {
        return registrationOpenDate;
    }

    public void setRegistrationOpenDate(String registrationOpenDate) {
        this.registrationOpenDate = registrationOpenDate;
    }

    public String getRegistrationCloseDate() {
        return registrationCloseDate;
    }

    public void setRegistrationCloseDate(String registrationCloseDate) {
        this.registrationCloseDate = registrationCloseDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
