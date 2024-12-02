package com.example.butter;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

/**
 * Checks if the menu bar shows the proper screen fragment
 *
 * NOTE: THIS ONLY WORKS IF YOUR ROLE IS BOTH OR ADMIN + ENTRANT + ORGANIZER OR ADMIN + ORGANIZER
 *
 * @author Angela Dakay (angelcache)
 */
public class NavigabilityTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<MainActivity>(MainActivity.class);

    @Test
    public void testViewProfileButton() throws InterruptedException {
        Thread.sleep(2000);
        onView(withId(R.id.profileIcon)).perform(click());
        Thread.sleep(2000);
    }

    @Test
    public void testViewHomeButton() throws InterruptedException {
        Thread.sleep(2000);
        onView(withId(R.id.homeIcon)).perform(click());
        Thread.sleep(2000);
    }

    @Test
    public void testViewEventsButton() throws InterruptedException {
        Thread.sleep(2000);
        onView(withId(R.id.eventsIcon)).perform(click());
        Thread.sleep(2000);
    }

    @Test
    public void testNotificationButton() throws InterruptedException {
        Thread.sleep(2000);
        onView(withId(R.id.notificationsIcon)).perform(click());
        Thread.sleep(2000);
    }

}
