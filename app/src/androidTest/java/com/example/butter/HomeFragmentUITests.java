package com.example.butter;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.containsString;

import androidx.annotation.ContentView;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

public class HomeFragmentUITests {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);

    /**
     * Tests to see if the spinner selects the write option.
     */
    @Test
    public void testSpinnerSelection() {
//        onView(withId(R.id.admin_spinner_layout)).check(matches(isDisplayed()));  // Check parent layout
//        onView(withId(R.id.entrants_spinner)).perform(click());
//        onView(withText("Browse Events")).perform(click());
//        onView(withId(R.id.entrants_spinner))
//                .check(matches(withSpinnerText(containsString("Browse Events"))));
    }

    /**
     * Tests to see if the qr code button leads to qr code scanner.
     */
    @Test
    public void testQrScanner() {
    }

    /**
     * Tests to see if clicking "browse events" will lead to right
     * lists of events showing up.
     */
    @Test
    public void testBrowseEvents() {

    }
}
