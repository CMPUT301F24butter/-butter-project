package com.example.butter;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This document will perform all tests corresponding to {@link SplashActivity}
 *
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
 * @author Soopyman
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SplashScreenTest {
    @Rule
    public ActivityScenarioRule<SplashActivity> scenario = new ActivityScenarioRule<>(SplashActivity.class);

    @Test
    public void testSplashScreen() {   // tests if viewing the profile shows the views
        onView(withId(R.id.splash_activity)).check(matches(isDisplayed()));
        onView(withText("Butter")).check(matches(isDisplayed()));
    }
}
