package com.example.butter;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class User implements Serializable {
    private final String deviceID;
    private String name;
    private int privileges;
    private String facility;
    private String email;
    private String phoneNumber;

    public User(String deviceID, String name, int privileges, @Nullable String facility, String email, @Nullable String phoneNumber) {
        this.deviceID = deviceID;
        this.name = name;
        this.privileges = privileges;
        this.facility = facility;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrivileges() {
        return privileges;
    }

    public String getPrivilegesString() {
        return String.valueOf(privileges);
    }

    public String getRole() {   // returns the role as a string
        if (this.privileges < 200) {
            return "Entrant";
        } else if (this.privileges < 300) {
            return "Organizer";
        } else if (this.privileges < 400) {
            return "Organizer & Entrant";
        } else {
            return "Admin";
        }
    }

    public void setPrivileges(int privileges) {
        this.privileges = privileges;
    }

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facility) {
        this.facility = facility;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
