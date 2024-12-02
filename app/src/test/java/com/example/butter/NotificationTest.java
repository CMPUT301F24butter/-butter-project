package com.example.butter;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * This will test the Notification Class constructor and its methods.
 *
 * @author Angela Dakay (angelcache)
 */

public class NotificationTest {

    private Notification notification;

    // Initialize a Notification object before each test
    @BeforeEach
    public void setUp() {
        notification = new Notification("123", "Sender Name", "SenderID", "DeviceID", "This is a message", true);
    }

    @Test
    public void testConstructorAndGetters() {
        assertEquals("123", notification.getNotificationID());
        assertEquals("Sender Name", notification.getEventSender());
        assertEquals("SenderID", notification.getEventSenderID());
        assertEquals("DeviceID", notification.getRecipientDeviceID());
        assertEquals("This is a message", notification.getMessage());
        assertFalse(notification.getSeen());  // By default, seen should be false
        assertTrue(notification.getForce());  // Force should be true based on constructor
    }

    @Test
    public void testSettersAndGetters() {
        notification.setSeen(true);
        notification.setForce(false);
        notification.setDatetime("2024-12-01T12:00:00");
        notification.setEventImage("image_url");

        // Assert setters work correctly
        assertTrue(notification.getSeen());
        assertFalse(notification.getForce());
        assertEquals("2024-12-01T12:00:00", notification.getDatetime());
        assertEquals("image_url", notification.getEventImage());
    }

    @Test
    public void testSeenStringConversion() {
        notification.setSeen(true);
        assertEquals("true", notification.getSeenString());

        notification.setSeen(false);
        assertEquals("false", notification.getSeenString());
    }

    @Test
    public void testForceStringConversion() {
        notification.setForce(true);
        assertEquals("true", notification.getForceString());

        notification.setForce(false);
        assertEquals("false", notification.getForceString());
    }
}
