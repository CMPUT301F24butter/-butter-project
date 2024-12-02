package com.example.butter;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;

import android.content.Intent;

import androidx.fragment.app.Fragment;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This test ensures the proper functionality of notifications UI, including toggling switches
 * and deleting notifications. Improvements have been made to ensure fragment lifecycle stability.
 */
@RunWith(AndroidJUnit4.class)
public class NotificationsUITest {

    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<MainActivity>(MainActivity.class);

    @Test
    public void testViewNotificationsButton() throws InterruptedException {   // tests if viewing the profile shows the views
        onView(withId(R.id.notificationsIcon)).perform(click());
        onView(withId(R.id.profileIcon)).perform(click());
        onView(withId(R.id.notificationsIcon)).perform(click());
        Thread.sleep(3000);
        onView(withId(R.id.notification_text)).check(matches(isDisplayed()));
        onView(withId(R.id.notificationSwitch)).check(matches(isDisplayed()));
        onView(withId(R.id.notificationList)).check(matches(isDisplayed()));
    }

    @Test
    public void testToggleSwitchAndDeleteNotification() throws InterruptedException {
        onView(withId(R.id.notificationsIcon)).perform(click());
        onView(withId(R.id.profileIcon)).perform(click());
        onView(withId(R.id.notificationsIcon)).perform(click());
        Thread.sleep(3000);
        // Ensure the UI is loaded and elements are visible
        onView(withId(R.id.notificationSwitch)).check(matches(isDisplayed()));
        onView(withId(R.id.notificationList)).check(matches(isDisplayed()));

        // Toggle the SwitchCompat button
        onView(withId(R.id.notificationSwitch)).perform(click());
        System.out.println("Switch toggled successfully.");
        Thread.sleep(3000);
        // Select the first notification in the ListView
        onData(anything())
                .inAdapterView(withId(R.id.notificationList))
                .atPosition(0)
                .perform(click());
        System.out.println("Notification clicked.");

        // Click on the FloatingActionButton to delete the notification
        onView(withId(R.id.delete_button)).perform(click());
        Thread.sleep(3000);
        System.out.println("Notification deleted.");
    }
}
