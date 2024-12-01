package com.example.butter;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasToString;

import android.content.Intent;
import android.widget.DatePicker;

import androidx.test.espresso.contrib.PickerActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * NOTE: IN ORDER FOR THESE TESTS TO WORK, YOU HAVE TO FIRST LAUNCH THE APP NORMALLY (NOT THE TEST FILE)
 *       ONCE IT'S BOOTED UP FULLY, CLICK THE DROPDOWN BUTTON TO THE LEFT OF THE RUN APP BUTTON
 *       CLICK RUN 'EVENT SCREEN TEST'
 *
 *       IF YOU TRY TO RUN THE TEST FILE DIRECTLY WITHOUT THESE STEPS, IT WILL ALWAYS FAIL
 *
 *       IF THIS TRICK DOES NOT WORK ON YOUR MACHINE, PLEASE ASK ME TO SHOW YOU ON MY MACHINE
 *
 *       ALSO, FILL IN YOUR DEVICE ID, AND AN EVENT ID OF AN EVENT YOU ORGANIZE IN THE setUp METHOD
 *
 * @author Nate Pane (natepane)
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EventScreenTest {
    @Rule
    public ActivityTestRule<EventDetailsActivity> activityRule =
            new ActivityTestRule<>(EventDetailsActivity.class, false, false); // Don't launch automatically

    @Before
    public void setUp() {
        String deviceID = "a256a5d278042a1d"; // CHANGE THIS TO YOUR DEVICE ID
        String eventID = "Bowling-a256a5d278042a1d"; // CHANGE THIS TO THE ID OF AN EVENT YOU ORGANIZE

        Intent intent = new Intent();
        intent.putExtra("deviceID", deviceID);
        intent.putExtra("eventID", eventID);

        activityRule.launchActivity(intent);
    }

    @Test
    public void testEventDetails() {
        onView(withText("Delete Event")).check(matches(isDisplayed()));
    }

    @Test
    public void testOrganizerOptions() {
        onView(withId(R.id.organizer_opt_button)).perform(click());
        onView(withText("Organizer Options")).check(matches(isDisplayed()));
    }

    @Test
    public void testEditEventScreen() {
        onView(withId(R.id.organizer_opt_button)).perform(click());
        onView(withId(R.id.edit_event_text)).perform(click());
        onView(withId(R.id.edit_event_button)).check(matches(isDisplayed()));
    }

    @Test
    public void testIllegalDescription() {
        onView(withId(R.id.organizer_opt_button)).perform(click());
        onView(withId(R.id.edit_event_text)).perform(click());
        onView(withId(R.id.event_description)).perform(clearText());
        onView(withId(R.id.edit_event_button)).perform(click());
        onView(withId(R.id.edit_event_button)).check(matches(isDisplayed()));
    }

    @Test
    public void testIEditEventNonConsecutiveDates() {
        onView(withId(R.id.organizer_opt_button)).perform(click());
        onView(withId(R.id.edit_event_text)).perform(click());
        onView(withId(R.id.event_start_date)).perform(click());
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2026, 1, 1));
        onView(withText("OK")).perform(click());
        onView(withId(R.id.edit_event_button)).perform(click());
        onView(withId(R.id.edit_event_button)).check(matches(isDisplayed()));
    }

    @Test
    public void testEditEventIllegalStartDate() {
        onView(withId(R.id.organizer_opt_button)).perform(click());
        onView(withId(R.id.edit_event_text)).perform(click());
        onView(withId(R.id.event_start_date)).perform(click());
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2024, 11, 1));
        onView(withText("OK")).perform(click());

        onView(withId(R.id.end_date)).perform(click());
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2024, 11, 2));
        onView(withText("OK")).perform(click());

        onView(withId(R.id.event_date)).perform(click());
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2024, 11, 3));
        onView(withText("OK")).perform(click());

        onView(withId(R.id.edit_event_button)).perform(click());
        onView(withId(R.id.edit_event_button)).check(matches(isDisplayed()));
    }

    @Test
    public void testEditEventValidDetails() {
        onView(withId(R.id.organizer_opt_button)).perform(click());
        onView(withId(R.id.edit_event_text)).perform(click());

        onView(withId(R.id.event_date)).perform(click());
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2025, 1, 1));
        onView(withText("OK")).perform(click());

        onView(withId(R.id.event_description)).perform(clearText());
        onView(withId(R.id.event_description)).perform(typeText("New event description"));

        onView(withId(R.id.edit_event_button)).perform(click());
        onView(withText("Delete Event")).check(matches(isDisplayed()));

        onView(withText("New event description")).check(matches(isDisplayed()));
    }

    @Test
    public void testViewEntrants() {
        onView(withId(R.id.organizer_opt_button)).perform(click());
        onView(withId(R.id.view_entrants_text)).perform(click());
        onView(withText("Entrants list")).check(matches(isDisplayed()));
    }

    @Test
    public void testWaitlistButtons() {
        onView(withId(R.id.organizer_opt_button)).perform(click());
        onView(withId(R.id.view_entrants_text)).perform(click());
        onView(withId(R.id.generate_entrants_button)).check(matches(isDisplayed()));
        onView(withId(R.id.sample_size)).check(matches(isDisplayed()));
    }

    @Test
    public void testDrawListButtons() {
        onView(withId(R.id.organizer_opt_button)).perform(click());
        onView(withId(R.id.view_entrants_text)).perform(click());
        onView(withId(R.id.entrants_spinner)).perform(click());
        onView(withText("Draw")).perform(click());
        onView(withId(R.id.generate_entrants_button)).check(matches(not(isDisplayed())));
        onView(withId(R.id.delete_entrant_button)).check(matches(isDisplayed()));
    }

    @Test
    public void testRegisteredListNoButtons() {
        onView(withId(R.id.organizer_opt_button)).perform(click());
        onView(withId(R.id.view_entrants_text)).perform(click());
        onView(withId(R.id.entrants_spinner)).perform(click());
        onView(withText("Registered")).perform(click());
        onView(withId(R.id.generate_entrants_button)).check(matches(not(isDisplayed())));
        onView(withId(R.id.delete_entrant_button)).check(matches(not(isDisplayed())));
    }

    @Test
    public void testCancelledListButtons() {
        onView(withId(R.id.organizer_opt_button)).perform(click());
        onView(withId(R.id.view_entrants_text)).perform(click());
        onView(withId(R.id.entrants_spinner)).perform(click());
        onView(withText("Cancelled")).perform(click());
        onView(withId(R.id.generate_entrants_button)).check(matches(not(isDisplayed())));
        onView(withId(R.id.delete_entrant_button)).check(matches(not(isDisplayed())));
        onView(withId(R.id.draw_replacements_button)).check(matches(isDisplayed()));
    }

    @Test
    public void testNotifications() {
        onView(withId(R.id.organizer_opt_button)).perform(click());
        onView(withId(R.id.send_notifications_text)).perform(click());
        onView(withText("Event Announcement")).check(matches(isDisplayed()));
    }

    @Test
    public void testNotificationsNoMessage() {
        onView(withId(R.id.organizer_opt_button)).perform(click());
        onView(withId(R.id.send_notifications_text)).perform(click());
        onView(withId(R.id.send_button)).perform(click());
        onView(withText("Event Announcement")).check(matches(isDisplayed()));
    }

    @Test
    public void testShowDetailsCode() {
        onView(withId(R.id.organizer_opt_button)).perform(click());
        onView(withId(R.id.show_details_code_text)).perform(click());
        onView(withText("Details QR Code")).check(matches(isDisplayed()));
    }

    @Test
    public void testDeleteEventConfirmationDialogue() {
        onView(withText("Delete Event")).perform(click());
        onView(withText("Delete")).check(matches(isDisplayed()));
    }
}
