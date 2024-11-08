package com.example.butter;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static
        androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.hasToString;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.hamcrest.Matcher;
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
 *       ALSO, YOU MUST HAVE AT LEAST ONE EVENT CREATED FOR THESE TESTS TO WORK
 *
 * @author Nate Pane (natepane)
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EventScreenTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<MainActivity>(MainActivity.class);

    @Test
    public void testAddEventButton() {
        onView(withId(R.id.eventsIcon)).perform(click());
        onView(withText("My Events")).check(matches(isDisplayed()));
        onView(withId(R.id.addButton)).perform(click());
        onView(withText("Create Event")).check(matches(isDisplayed()));
    }

    @Test
    public void testInvalidEventDetailsNoDescription() {
        onView(withId(R.id.eventsIcon)).perform(click());
        onView(withId(R.id.addButton)).perform(click());
        onView(withId(R.id.name_event)).perform(ViewActions.typeText("Test Event"));
        onView(withId(R.id.start_date)).perform(ViewActions.typeText("2024-12-01"));
        onView(withId(R.id.end_date)).perform(ViewActions.typeText("2024-12-02"));
        onView(withId(R.id.event_date)).perform(ViewActions.typeText("2024-12-03"));

        onView(withId(R.id.create_event_button)).perform(click());
        onView(withText("Create Event")).check(matches(isDisplayed()));
    }

    @Test
    public void testInvalidEventNotConsecutiveDates() {
        onView(withId(R.id.eventsIcon)).perform(click());
        onView(withId(R.id.addButton)).perform(click());
        onView(withId(R.id.name_event)).perform(ViewActions.typeText("Test Event"));
        onView(withId(R.id.start_date)).perform(ViewActions.typeText("2024-12-01"));
        onView(withId(R.id.end_date)).perform(ViewActions.typeText("2024-11-30"));
        onView(withId(R.id.event_date)).perform(ViewActions.typeText("2024-11-29"));
        onView(withId(R.id.event_description)).perform(ViewActions.typeText("Event description"));

        onView(withId(R.id.create_event_button)).perform(click());
        onView(withText("Create Event")).check(matches(isDisplayed()));
    }

    @Test
    public void testEventDetails() {
        onView(withId(R.id.eventsIcon)).perform(click());
        onView(withId(R.id.events_list)).perform(click());
        onView(withText("Delete Event")).check(matches(isDisplayed()));
    }

    @Test
    public void testOrganizerOptionsDialog() {
        onView(withId(R.id.eventsIcon)).perform(click());
        onView(withId(R.id.events_list)).perform(click());
        onView(withId(R.id.organizer_opt_button)).perform(click());

        onView(withText("Organizer Options")).check(matches(isDisplayed()));
    }

    @Test
    public void testEditEventButton() {
        onView(withId(R.id.eventsIcon)).perform(click());
        onView(withId(R.id.events_list)).perform(click());
        onView(withId(R.id.organizer_opt_button)).perform(click());
        onView(withId(R.id.edit_event_text)).perform(click());
        onView(withId(R.id.edit_event_button)).check(matches(isDisplayed()));
    }

    @Test
    public void testEditDetailsInvalidDescription() {
        onView(withId(R.id.eventsIcon)).perform(click());
        onView(withId(R.id.events_list)).perform(click());
        onView(withId(R.id.organizer_opt_button)).perform(click());
        onView(withId(R.id.edit_event_text)).perform(click());
        onView(withId(R.id.event_description)).perform(clearText());
        onView(withId(R.id.edit_event_button)).perform(click());
        onView(withId(R.id.edit_event_button)).check(matches(isDisplayed()));
    }

    @Test
    public void testEditDetailsInvalidDate() {
        onView(withId(R.id.eventsIcon)).perform(click());
        onView(withId(R.id.events_list)).perform(click());
        onView(withId(R.id.organizer_opt_button)).perform(click());
        onView(withId(R.id.edit_event_text)).perform(click());
        onView(withId(R.id.event_start_date)).perform(clearText());
        onView(withId(R.id.event_start_date)).perform(ViewActions.typeText("1900-01-01"));
        onView(withId(R.id.edit_event_button)).perform(click());
        onView(withId(R.id.edit_event_button)).check(matches(isDisplayed()));
    }

    @Test
    public void testEditDetailsValid() {
        onView(withId(R.id.eventsIcon)).perform(click());
        onView(withId(R.id.events_list)).perform(click());
        onView(withId(R.id.organizer_opt_button)).perform(click());
        onView(withId(R.id.edit_event_text)).perform(click());
        onView(withId(R.id.event_description)).perform(clearText());
        onView(withId(R.id.event_description)).perform(typeText("New event description"));
        onView(withId(R.id.edit_event_button)).perform(click());
        onView(withText("Ok")).perform(click());
        onView(withText("New event description")).check(matches(isDisplayed()));
    }

    @Test
    public void testViewEntrantsButton() {
        onView(withId(R.id.eventsIcon)).perform(click());
        onView(withId(R.id.events_list)).perform(click());
        onView(withId(R.id.organizer_opt_button)).perform(click());
        onView(withId(R.id.view_entrants_text)).perform(click());
        onView(withText("Entrants list")).check(matches(isDisplayed()));
    }

    @Test
    public void testEntrantsListDissapearingButton() {
        onView(withId(R.id.eventsIcon)).perform(click());
        onView(withId(R.id.events_list)).perform(click());
        onView(withId(R.id.organizer_opt_button)).perform(click());
        onView(withId(R.id.view_entrants_text)).perform(click());
        onView(withId(R.id.generate_entrants_button)).check(matches(isDisplayed()));
        onView(withId(R.id.entrants_spinner)).perform(click());
        onView(withText("Draw")).perform(click());
        onView(withId(R.id.generate_entrants_button)).check(matches(not(isDisplayed())));
    }

    @Test
    public void testShowDetailsCode() {
        onView(withId(R.id.eventsIcon)).perform(click());
        onView(withId(R.id.events_list)).perform(click());
        onView(withId(R.id.organizer_opt_button)).perform(click());
        onView(withId(R.id.show_details_code_text)).perform(click());
        onView(withText("Details QR Code")).check(matches(isDisplayed()));
    }
}
