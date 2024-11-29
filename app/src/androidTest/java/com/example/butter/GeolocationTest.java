package com.example.butter;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.is;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

/**
 * This will run tests for geolocation testing
 *
 * IN ORDER TO RUN THESE TESTS YOU MUST HAVE ADMIN PRIVILEGES
 *
 * NOTE: IN ORDER FOR THESE TESTS TO WORK, YOU HAVE TO FIRST LAUNCH THE APP NORMALLY (NOT THE TEST FILE)
 *      ONCE IT'S BOOTED UP FULLY, CLICK THE DROPDOWN BUTTON TO THE LEFT OF THE RUN APP BUTTON
 *      CLICK RUN 'EVENT SCREEN TEST'
 *      IF YOU TRY TO RUN THE TEST FILE DIRECTLY WITHOUT THESE STEPS, IT WILL ALWAYS FAIL
 *
 *      IF THIS TRICK DOES NOT WORK ON YOUR MACHINE, PLEASE ASK ME TO SHOW YOU ON MY MACHINE
 *
 *      AFTER DELETING SOMETHING ADD IT BACK IN AGAIN BEFORE TESTING IT OUT AGAIN
 *
 * @author Angela Dakay (angelcache)
 */

public class GeolocationTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<MainActivity>(MainActivity.class);

    // Tests to see if an event with no geolocation will not have a dialogue pop up
    @Test
    public void noGeolocationTest() throws InterruptedException {
        Thread.sleep(2000);
        onView(withId(R.id.entrants_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Browse Images")))
                .inRoot(isPlatformPopup())
                .perform(click());
        Thread.sleep(2000);

        // Click the specific event in the ListView by matching the event name in the TextView
        onData(anything()) // Match any data (Event object)
                .inAdapterView(withId(R.id.admin_list_view))
                .atPosition(8) // position of my event image
                .perform(click());
        onView(withId(R.id.delete_admin_button)).perform(click());

        Thread.sleep(2000);
        onView(withId(R.id.cancel_button)).perform(click());
        Thread.sleep(2000);
    }
}
