package com.example.butter;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.withId;


import static org.junit.Assert.assertEquals;

import android.content.Intent;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Objects;

/**
 * This will run tests for geolocation (checks if geolocation dialog pops up for
 * an event with geolocation on)
 *
 * IN ORDER TO RUN THESE TESTS YOU MUST NOT HAVE JOIN THE EVENT WE'RE TESTING YET
 *
 * NOTE: WILL NEED TO CHANGE DEVICE ID TO YOUR OWN TO RUN THIS TEST MAKE SURE YOU ARE
 * NOT ALREADY IN THE EVENT WAITING / REGISTER / DRAW LIST OF THE TEST EVENT. ALSO, IF
 * NEEDED, CHANGE THE TEST EVENT AS WELL.
 *
 * @author Angela Dakay (angelcache)
 */

public class GeolocationTest {
    @Rule
    public ActivityTestRule<EventDetailsActivity> activityRule =
            new ActivityTestRule<>(EventDetailsActivity.class, false, false); // Don't launch automatically

    @Before
    public void setUp() {
        // Event Data we will use Cat Meetup (has geolocation on)
        String deviceID = "c847b34b3c917f78"; // CHANGE THIS TO YOUR ID
        String eventID = "Cat_Meetup-65e1e878f39577f3";
        String listType = "wait";

        // Create an intent with event data
        Intent intent = new Intent();
        intent.putExtra("deviceID", deviceID);
        intent.putExtra("eventID", eventID);
        intent.putExtra("listType", listType);

        // Launch the activity with the intent
        activityRule.launchActivity(intent);
    }

    // Tests to see if an event with geolocation has a dialogue pop up and declining that dialogue
    @Test
    public void GeolocationDeclineTest() throws InterruptedException {
        // Join waiting list
        Thread.sleep(2000);
        onView(withId(R.id.waiting_list_button)).perform(click());

        // Decline Dialogue
        Thread.sleep(2000);
        onView(withId(R.id.cancel_button)).perform(click());
        Thread.sleep(2000);
    }

    // Tests to see if an event with geolocation has a dialogue pop up and accepting that dialogue
    @Test
    public void GeolocationAcceptTest() throws InterruptedException {
        // Join Waiting List
        Thread.sleep(2000);
        onView(withId(R.id.waiting_list_button)).perform(click());

        // Accept Dialogue
        Thread.sleep(2000);
        onView(withId(R.id.yes_button)).perform(click());

        // Leave Waiting list
        Thread.sleep(2000);
        onView(withId(R.id.waiting_list_button)).perform(click());
        Thread.sleep(2000);
    }
}
