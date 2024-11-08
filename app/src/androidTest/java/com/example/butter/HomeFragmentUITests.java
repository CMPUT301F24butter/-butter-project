package com.example.butter;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.is;

import android.Manifest;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * NOTE: Run the app first before the testing file
 *
 * @author Angela
 */

public class HomeFragmentUITests {

    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        Intents.init();
        // Grant camera permission for the QR scanner test
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "pm grant " + InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName() + " " + Manifest.permission.CAMERA
        );
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    // Tests to see if the qr code button leads to the qr code scanner camera.
    @Test
    public void testQrScannerButton() {
        onView(withId(R.id.qrScannerButton)).perform(click());

        // Check if the CaptureAct activity is launched
        intended(hasComponent(CaptureAct.class.getName()));
    }

    // Tests to see if clicking "Browse Events" will lead to the right listView showing up.
    @Test
    public void testBrowseEvents() {
        onView(withId(R.id.entrants_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Browse Events")))
                .inRoot(isPlatformPopup())
                .perform(click());

        onView(withId(R.id.admin_list_view)).check(matches(isDisplayed()));
    }

    // Tests to see if clicking "Browse Profiles" will lead to the right listView showing up.
    @Test
    public void testBrowseProfiles() {
        onView(withId(R.id.entrants_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Browse Profiles")))
                .inRoot(isPlatformPopup())
                .perform(click());

        onView(withId(R.id.admin_list_view)).check(matches(isDisplayed()));
    }

    // Tests to see if clicking "Browse Facilities" will lead to the right listView showing up.
    @Test
    public void testBrowseFacilities() {
        onView(withId(R.id.entrants_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Browse Facilities")))
                .inRoot(isPlatformPopup())
                .perform(click());

        onView(withId(R.id.admin_list_view)).check(matches(isDisplayed()));
    }

    // Tests to see if clicking "Browse Images" will lead to the right listView showing up.
    @Test
    public void testBrowseImages() {
        onView(withId(R.id.entrants_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Browse Images")))
                .inRoot(isPlatformPopup())
                .perform(click());

        // Assuming the admin_list_view is hidden or shows a different view for images
        onView(withId(R.id.admin_list_view)).check(matches(isDisplayed()));
    }

    // Tests to see if clicking "Entrant's Page" will switch to the entrant view with the correct elements displayed.
    @Test
    public void testEntrantsPage() throws InterruptedException {
        onView(withId(R.id.entrants_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Entrant's Page")))
                .inRoot(isPlatformPopup())
                .perform(click());

        // Add a delay to wait for UI elements to load
        Thread.sleep(2000);

        // Check if the entrant view elements are displayed
        onView(withId(R.id.upcomingText)).check(matches(isDisplayed()));
        onView(withId(R.id.horizontalScrollView)).check(matches(isDisplayed()));
        onView(withId(R.id.waiting_list_label)).check(matches(isDisplayed()));
        onView(withId(R.id.waitingListScrollView)).check(matches(isDisplayed()));
    }
}