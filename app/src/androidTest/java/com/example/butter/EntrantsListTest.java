package com.example.butter;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.lifecycle.Lifecycle;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

/**
 * This will run tests to see if the home screen entrants list:  (wait, draw, upcoming) can be clicked
 * on and shows different buttons.
 *
 * MUST BE AN ENTRANT TO RUN THESE TESTS + MUST HAVE AT LEAST TWO EVENTS IN EACH OF THE WAIT, DRAW,
 * AND UPCOMING LIST
 *
 * NOTE: IN ORDER FOR THESE TESTS TO WORK, YOU HAVE TO FIRST LAUNCH THE APP NORMALLY (NOT THE TEST FILE)
 *      ONCE IT'S BOOTED UP FULLY, CLICK THE DROPDOWN BUTTON TO THE LEFT OF THE RUN APP BUTTON
 *      CLICK RUN 'ENTRANTS LIST TEST'
 *      IF YOU TRY TO RUN THE TEST FILE DIRECTLY WITHOUT THESE STEPS, IT WILL ALWAYS FAIL
 *
 *      IF THIS TRICK DOES NOT WORK ON YOUR MACHINE, PLEASE ASK ME TO SHOW YOU ON MY MACHINE
 *
 * @author Angela Dakay (angelcache)
 */

public class EntrantsListTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<MainActivity>(MainActivity.class);

    @Test
    public void testClickingEvent() throws InterruptedException {
        scenario.getScenario().moveToState(Lifecycle.State.RESUMED);
        Thread.sleep(2000);

        onView(withId(R.id.waitingListRecyclerView))
                .check(matches(isDisplayed()))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(2000);
        onView(withId(R.id.back_button)).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.eventsRecyclerView))
                .check(matches(isDisplayed()))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(2000);
        onView(withId(R.id.back_button)).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.drawListRecyclerView))
                .check(matches(isDisplayed()))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(2000);
        onView(withId(R.id.back_button)).perform(click());
    }

    // Test leaving registered list
    @Test
    public void testLeavingUpcomingLists() throws InterruptedException {
        scenario.getScenario().moveToState(Lifecycle.State.RESUMED);
        Thread.sleep(2000);

        onView(withId(R.id.eventsRecyclerView))
                .check(matches(isDisplayed()))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.waiting_list_button)).perform(click());
        Thread.sleep(2000);
    }

    // Test leaving waiting list
    @Test
    public void testLeavingWaitingLists() throws InterruptedException {
        scenario.getScenario().moveToState(Lifecycle.State.RESUMED);
        Thread.sleep(2000);

        onView(withId(R.id.waitingListRecyclerView))
                .check(matches(isDisplayed()))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.waiting_list_button)).perform(click());
        Thread.sleep(2000);
    }

}
