package com.example.butter;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.CoreMatchers.anything;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

/**
 * Checks if the menu bar shows the proper screen fragment
 *
 * NOTE: THIS ONLY WORKS IF YOUR ROLE IS ORGANIZER AND YOU HAVE AT LEAST TWO EVENTS
 *
 * @author Angela Dakay (angelcache)
 */

public class CheckMapTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<MainActivity>(MainActivity.class);

    // checks if map shows up
    @Test
    public void testMapShowsUp() throws InterruptedException {
        onView(withId(R.id.eventsIcon)).perform(click());
        Thread.sleep(2000);
        onData(anything()) // Match any data (Event object)
                .inAdapterView(withId(R.id.events_list))
                .atPosition(0)
                .perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.organizer_opt_button)).perform(click());
        onView(withId(R.id.view_map_text)).perform(click());
        Thread.sleep(2000);
    }

    // check another events map to see if the markers are not the same
    @Test
    public void testMarkersDifferent() throws InterruptedException {
        Thread.sleep(2000);
        onView(withId(R.id.eventsIcon)).perform(click());
        Thread.sleep(2000);
        onData(anything()) // Match any data (Event object)
                .inAdapterView(withId(R.id.events_list))
                .atPosition(0)
                .perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.organizer_opt_button)).perform(click());
        onView(withId(R.id.view_map_text)).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.back_button)).perform(click());
        onView(withId(R.id.ok_text)).perform(click());
        onView(withId(R.id.back_button)).perform(click());
        onData(anything()) // Match any data (Event object)
                .inAdapterView(withId(R.id.events_list))
                .atPosition(1)
                .perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.organizer_opt_button)).perform(click());
        onView(withId(R.id.view_map_text)).perform(click());
    }
}
