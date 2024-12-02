package com.example.butter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This will test the Users Class constructor and its methods.
 *
 * @author Angela Dakay (angelcache)
 */

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        // Initialize a User object before each test
        user = new User("123", "Angel D", 200, "Chicken Club", "angkay@gmail.com", "1234567890");
    }

    @Test
    void testConstructorAndGetters() {
        // Test constructor and getters for User class
        assertEquals("123", user.getDeviceID());
        assertEquals("Angel D", user.getName());
        assertEquals(200, user.getPrivileges());
        assertEquals("Chicken Club", user.getFacility());
        assertEquals("angkay@gmail.com", user.getEmail());
        assertEquals("1234567890", user.getPhoneNumber());
        assertNotNull(user.getNotifications());
        assertTrue(user.getNotifications());
    }

    @Test
    void testSetName() {
        // Test setter for name
        user.setName("Angela D");
        assertEquals("Angela D", user.getName());
    }

    @Test
    void testGetPrivilegesString() {
        // Test if getPrivilegesString converts privileges to string correctly
        assertEquals("200", user.getPrivilegesString());
    }

    @Test
    void testGetRole() {
        // Test if getRole method returns the correct role based on privileges
        assertEquals("Organizer", user.getRole());

        user.setPrivileges(100);
        assertEquals("Entrant", user.getRole());

        user.setPrivileges(200);
        assertEquals("Organizer", user.getRole());

        user.setPrivileges(300);
        assertEquals("Organizer & Entrant", user.getRole());

        user.setPrivileges(400);
        assertEquals("Admin", user.getRole());

        user.setPrivileges(500);
        assertEquals("Admin & Entrant", user.getRole());

        user.setPrivileges(600);
        assertEquals("Admin & Organizer", user.getRole());

        user.setPrivileges(700);
        assertEquals("Admin, Organizer, & Entrant", user.getRole());
    }

    @Test
    void testSetFacility() {
        // Test setter for facility
        user.setFacility("Chicken Club 2.0");
        assertEquals("Chicken Club 2.0", user.getFacility());
    }

    @Test
    void testSetEmail() {
        // Test setter for email
        user.setEmail("angkay2@example.com");
        assertEquals("angkay2@example.com", user.getEmail());
    }

    @Test
    void testSetPhoneNumber() {
        // Test setter for phone number
        user.setPhoneNumber("0987654321");
        assertEquals("0987654321", user.getPhoneNumber());
    }

    @Test
    void testSetProfilePicString() {
        // Test setter for profilePicString
        user.setProfilePicString("new_profile_pic_string");
        assertEquals("new_profile_pic_string", user.getProfilePicString());
    }

    @Test
    void testGetNotificationsString() {
        // Test if getNotificationsString returns "true" when notifications are enabled
        assertEquals("true", user.getNotificationsString());

        // Test if getNotificationsString returns "false" when notifications are disabled
        user.setNotifications(false);
        assertEquals("false", user.getNotificationsString());
    }

    @Test
    void testSetNotifications() {
        // Test setter for notifications
        user.setNotifications(false);
        assertFalse(user.getNotifications());
        user.setNotifications(true);
        assertTrue(user.getNotifications());
    }
}
