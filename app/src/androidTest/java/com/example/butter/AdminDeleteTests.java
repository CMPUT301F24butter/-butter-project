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
 * qr codes in admin home screen works.
 *
 * NOTE: BE CAREFUL WHEN RUNNING THESE TESTS, WILL DELETE THINGS AT POSITION 8!!
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
 *      ALSO, YOU MUST HAVE AT LEAST ONE EVENT CREATED FOR THESE TESTS TO WORK
 *
 * @author Angela Dakay (angelcache)
 */

public class AdminDeleteTests {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<MainActivity>(MainActivity.class);

    @Test
    public void testDeleteImages() throws InterruptedException {
        /*
        onView(withId(R.id.eventsIcon)).perform(click());
        onView(withId(R.id.addButton)).perform(click());
        onView(withId(R.id.name_event)).perform(click());
        onView(withId(R.id.name_event)).perform(ViewActions.typeText("Dummy Event"));
        onView(withId(R.id.start_date)).perform(click());
        onView(withId(R.id.start_date)).perform(ViewActions.typeText("2025-01-01"));
        onView(withId(R.id.end_date)).perform(click());
        onView(withId(R.id.end_date)).perform(ViewActions.typeText("2025-01-02"));
        onView(withId(R.id.event_date)).perform(click());
        onView(withId(R.id.event_date)).perform(ViewActions.typeText("2025-01-03"));
        onView(withId(R.id.event_date)).perform(click());
        onView(withId(R.id.event_date)).perform(ViewActions.typeText("2025-01-03"));
        onView(withId(R.id.location_switch)).perform(click());
        onView(withId(R.id.create_event_button)).perform(click());
        onView(withId(R.id.back_button)).perform((click()));
        onView(withId(R.id.homeIcon)).perform(click());
         */
        Thread.sleep(2000);
        onView(withId(R.id.entrants_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Browse Images")))
                .inRoot(isPlatformPopup())
                .perform(click());
        Thread.sleep(2000);
        // Click the specific event in the ListView by matching the event name in the TextView
        onData(anything()) // Match any data (Event object)
                .inAdapterView(withId(R.id.admin_list_view))
                .atPosition(8) // position of my event
                .perform(click());
        onView(withId(R.id.delete_admin_button)).perform(click());

        Thread.sleep(2000);
        onView(withId(R.id.yes_button)).perform(click());
        Thread.sleep(2000);
    }

    public static Matcher<Object> withEventName(String name) {
        return new BoundedMatcher<Object, Event>(Event.class) {
            @Override
            protected boolean matchesSafely(Event event) {
                return event.getName().equals(name);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with event name: " + name);
            }
        };
    }


}
