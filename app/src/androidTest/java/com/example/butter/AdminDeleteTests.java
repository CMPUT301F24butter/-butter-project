package com.example.butter;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.is;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;

/**
 * This will run tests to see if the ability to delete events, profiles, facilities, images and
 * qr codes in admin home screen works. It includes tests for testing out cancelling a deletion
 * as well as test for going through with the deletion.
 *
 * NOTE: I HAVE COMMENTED OUT THE ONES THAT WILL ACTUALLY DELETE THINGS. YOU CAN UNCOMMENT IT TO
 * TRY IT OUT. BE CAREFUL WHEN RUNNING THOSE TESTS, THEY WILL DELETE THINGS AT A SPECIFIC POSITION!!
 * IF YOU WANT TO RUN AND RE_RUN AGAIN WITH THE DELETE TEST UNCOMMENTED OUT MAKE SURE TO NOTE WHAT
 * WILL BE DELETED!
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

public class AdminDeleteTests {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<MainActivity>(MainActivity.class);

    // Test out cancelling a deletion of event poster in position 8 (Pokemon Go Tournament)
    @Test
    public void testCancelDeletePosterImage() throws InterruptedException {
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

    // Test out cancelling a deletion of a profile picture in position 11 (My Profile Picture)
    @Test
    public void testCancelDeleteProfileImage() throws InterruptedException {
        Thread.sleep(2000);
        onView(withId(R.id.entrants_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Browse Images")))
                .inRoot(isPlatformPopup())
                .perform(click());
        Thread.sleep(2000);

        // Click the specific event in the ListView by matching the event name in the TextView
        onData(anything()) // Match any data (Event object)
                .inAdapterView(withId(R.id.admin_list_view))
                .atPosition(11) // position of my profile image
                .perform(click());
        onView(withId(R.id.delete_admin_button)).perform(click());

        Thread.sleep(2000);
        onView(withId(R.id.cancel_button)).perform(click());
        Thread.sleep(2000);
    }

    // Test out cancelling a deletion of a QR Code in position 0
    @Test
    public void testCancelDeleteQRCodes() throws InterruptedException {
        Thread.sleep(2000);
        onView(withId(R.id.entrants_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Browse QR Codes")))
                .inRoot(isPlatformPopup())
                .perform(click());
        Thread.sleep(2000);

        // Click the specific event in the ListView by matching the event name in the TextView
        onData(anything()) // Match any data (Event object)
                .inAdapterView(withId(R.id.admin_list_view))
                .atPosition(0)
                .perform(click());
        onView(withId(R.id.delete_admin_button)).perform(click());

        Thread.sleep(2000);
        onView(withId(R.id.cancel_button)).perform(click());
        Thread.sleep(2000);
    }

    // Test out cancelling a deletion of a profile in position 3
    @Test
    public void testCancelDeleteProfiles() throws InterruptedException {
        Thread.sleep(2000);
        onView(withId(R.id.entrants_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Browse Profiles")))
                .inRoot(isPlatformPopup())
                .perform(click());
        Thread.sleep(2000);

        // Click the specific event in the ListView by matching the event name in the TextView
        onData(anything()) // Match any data (Event object)
                .inAdapterView(withId(R.id.admin_list_view))
                .atPosition(3) // Random user
                .perform(click());
        onView(withId(R.id.delete_admin_button)).perform(click());

        Thread.sleep(2000);
        onView(withId(R.id.cancel_button)).perform(click());
        Thread.sleep(2000);
    }

    // Tests out cancelling a deletion of an event in position 1
    @Test
    public void testCancelDeleteEvents() throws InterruptedException {
        Thread.sleep(2000);
        onView(withId(R.id.entrants_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Browse Events")))
                .inRoot(isPlatformPopup())
                .perform(click());
        Thread.sleep(2000);

        // Click the specific event in the ListView by matching the event name in the TextView
        onData(anything()) // Match any data (Event object)
                .inAdapterView(withId(R.id.admin_list_view))
                .atPosition(1) // Random user
                .perform(click());

        Thread.sleep(2000);
        onView(withId(R.id.admin_delete_button)).perform(click());

        Thread.sleep(2000);
        onView(withId(R.id.cancel_button)).perform(click());
        Thread.sleep(2000);
    }

    /*
    // Test out deletion of event poster in position 8 (Pokemon Go Tournament)
    @Test
    public void testDeletePosterImage() throws InterruptedException {
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
        onView(withId(R.id.yes_button)).perform(click());
        Thread.sleep(2000);
    }

    // Test out deletion of a profile picture in position 11 (My Profile Picture)
    @Test
    public void testDeleteProfileImage() throws InterruptedException {
        Thread.sleep(2000);
        onView(withId(R.id.entrants_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Browse Images")))
                .inRoot(isPlatformPopup())
                .perform(click());
        Thread.sleep(2000);

        // Click the specific event in the ListView by matching the event name in the TextView
        onData(anything()) // Match any data (Event object)
                .inAdapterView(withId(R.id.admin_list_view))
                .atPosition(11) // position of my profile image
                .perform(click());
        onView(withId(R.id.delete_admin_button)).perform(click());

        Thread.sleep(2000);
        onView(withId(R.id.yes_button)).perform(click());
        Thread.sleep(2000);
    }

    // Test out deletion of a QR Code in position 0
    @Test
    public void testDeleteQRCodes() throws InterruptedException {
        Thread.sleep(2000);
        onView(withId(R.id.entrants_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Browse QR Codes")))
                .inRoot(isPlatformPopup())
                .perform(click());
        Thread.sleep(2000);

        // Click the specific event in the ListView by matching the event name in the TextView
        onData(anything()) // Match any data (Event object)
                .inAdapterView(withId(R.id.admin_list_view))
                .atPosition(0)
                .perform(click());
        onView(withId(R.id.delete_admin_button)).perform(click());

        Thread.sleep(2000);
        onView(withId(R.id.yes_button)).perform(click());
        Thread.sleep(2000);
    }

    // Test out deletion of a profile in position 3
    @Test
    public void testDeleteProfiles() throws InterruptedException {
        Thread.sleep(2000);
        onView(withId(R.id.entrants_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Browse Profiles")))
                .inRoot(isPlatformPopup())
                .perform(click());
        Thread.sleep(2000);

        // Click the specific event in the ListView by matching the event name in the TextView
        onData(anything()) // Match any data (Event object)
                .inAdapterView(withId(R.id.admin_list_view))
                .atPosition(3) // Random user
                .perform(click());
        onView(withId(R.id.delete_admin_button)).perform(click());

        Thread.sleep(2000);
        onView(withId(R.id.yes_button)).perform(click());
        Thread.sleep(2000);
    }

    // Tests deletion of an event in position 1
    @Test
    public void testDeleteEvents() throws InterruptedException {
        Thread.sleep(2000);
        onView(withId(R.id.entrants_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Browse Events")))
                .inRoot(isPlatformPopup())
                .perform(click());
        Thread.sleep(2000);

        // Click the specific event in the ListView by matching the event name in the TextView
        onData(anything()) // Match any data (Event object)
                .inAdapterView(withId(R.id.admin_list_view))
                .atPosition(1) // Random user
                .perform(click());

        Thread.sleep(2000);
        onView(withId(R.id.admin_delete_button)).perform(click());

        Thread.sleep(2000);
        onView(withId(R.id.yes_button)).perform(click());
        Thread.sleep(2000);
    }
     */
}
