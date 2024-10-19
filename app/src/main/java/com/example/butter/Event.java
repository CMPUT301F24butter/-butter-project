package com.example.butter;

public class Event {
    private final String eventID;
    private final String name;
    private final String organizerID;
    private String date;
    private String time;
    private int capacity;

    private final String waitlistID;
    private final String drawListID;
    private final String registeredListID;
    private final String cancelledListID;

    public Event(String name, String organizerID, String date, String time, int capacity) {
        this.name = name;
        this.organizerID = organizerID;
        this.date = date;
        this.time = time;
        this.capacity = capacity;

        this.eventID = name.replace(" ", "_") + "-" + organizerID; // eventID = Event_Name-organizerID

        // user list ID = Event_Name-organizerID-listName
        this.waitlistID = this.eventID + "-wait";
        this.drawListID = this.eventID + "-draw";
        this.registeredListID = this.eventID + "-registered";
        this.cancelledListID = this.eventID + "-cancelled";

        createUserLists();
    }

    private void createUserLists() {
        UserListDB userListDB = new UserListDB();

        userListDB.create(this.waitlistID);
        userListDB.create(this.drawListID);
        userListDB.create(this.registeredListID);
        userListDB.create(this.cancelledListID);
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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
}
