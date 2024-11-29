package com.example.butter;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.is;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This document will perform all admin tests corresponding to {@link HomeFragment}
 * Note that this will only work if you are an Admin already.
 * Sometimes this glitches and doesn't work so if that is the case, follow the NOTE below.
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
public class AdminHomeTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<MainActivity>(MainActivity.class);

    @Test
    public void testBrowseProfiles() {   // tests if we can see our newly updated profile
        onView(withId(R.id.profileIcon)).perform(click());  // first change our profile name
        onView(withId(R.id.edit_profile_button)).perform(click());
        onView(withId(R.id.username)).perform(ViewActions.clearText(), ViewActions.typeText("Testing"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.save_changes_button)).perform(click());


        onView(withId(R.id.homeIcon)).perform(click());
        onView(withId(R.id.entrants_spinner)).perform(click());
        onData(is("Browse Profiles")).perform(click()); // choose profiles to show
        onView(withText("Testing")).perform(scrollTo()).check(matches(isDisplayed()));  // check if our profile is displayed
    }

    @Test
    public void testBrowseFacilities() {   // tests if we can see our newly updated profile
        onView(withId(R.id.profileIcon)).perform(click());  // first change our profile name
        onView(withId(R.id.edit_profile_button)).perform(click());
        onView(withId(R.id.edit_role_spinner)).perform(click());
        onData(is("Admin, Organizer, & Entrant")).perform(click()); // choose entrant
        onView(withId(R.id.facility_name)).perform(ViewActions.clearText(), ViewActions.typeText("TESTFACILITY"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.save_changes_button)).perform(click());


        onView(withId(R.id.homeIcon)).perform(click());
        onView(withId(R.id.entrants_spinner)).perform(click());
        onData(is("Browse Facilities")).perform(click()); // choose profiles to show
        onView(withText("TESTFACILITY")).perform(scrollTo()).check(matches(isDisplayed()));  // check if our profile is displayed
    }
}
