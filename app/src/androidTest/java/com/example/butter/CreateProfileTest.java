package com.example.butter;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This document will perform all tests corresponding to {@link CreateProfileActivity}
 * Note that in order for the tests to run, the user should not exist in the database.
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
public class CreateProfileTest {
    @Rule
    public ActivityScenarioRule<CreateProfileActivity> scenario = new ActivityScenarioRule<>(CreateProfileActivity.class);

    @Test
    public void testShowCreateScreen() {   // tests if viewing the profile shows the views
        // will test
    }
    @Test
    public void testInvalidProfileName() {   // tests if we have can have invalid usernames
        onView(withId(R.id.username)).perform(ViewActions.clearText(), ViewActions.typeText("TestingALargeCharacterNameHopefullyLargerThan30"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.sign_up_button)).perform(click());
        onView(withText("Invalid Signup")).check(matches(isDisplayed()));
        onView(withText("Username is too long. Max of 30 characters.\nPlease try again.")).check(matches(isDisplayed()));

        onView(withText("OK")).perform(click());

        onView(withId(R.id.username)).perform(ViewActions.clearText());
        onView(withId(R.id.sign_up_button)).perform(click());
        onView(withText("Invalid Signup")).check(matches(isDisplayed()));
        onView(withText("Username box is empty.\nPlease try again.")).check(matches(isDisplayed()));
    }

    @Test
    public void testInvalidProfileEmail() {   // tests if we have can have invalid emails

        onView(withId(R.id.username)).perform(ViewActions.clearText(), ViewActions.typeText("ValidUsername"), ViewActions.closeSoftKeyboard());

        onView(withId(R.id.email)).perform(ViewActions.clearText(), ViewActions.typeText("testing123.com"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.sign_up_button)).perform(click());
        onView(withText("Invalid Signup")).check(matches(isDisplayed()));
        onView(withText("Invalid Email Address.\nPlease try again.")).check(matches(isDisplayed()));

        onView(withText("OK")).perform(click());

        onView(withId(R.id.email)).perform(ViewActions.clearText());
        onView(withId(R.id.sign_up_button)).perform(click());
        onView(withText("Invalid Signup")).check(matches(isDisplayed()));
        onView(withText("Invalid Email Address.\nPlease try again.")).check(matches(isDisplayed()));
    }

    @Test
    public void testInvalidProfilePhone() {   // tests if we can have invalid phone numbers
        // no way to be invalid yet, since only numbers can be typed in. Will be implemented later.
    }

    @Test
    public void testCreateProfileRole() {   // test if we can change roles
        onView(withId(R.id.role_spinner)).perform(click());
        onData(is("Organizer")).perform(click()); // choose organizer
        onView(withText("Facility Name")).check(matches(isDisplayed()));    // does facility show?

        onView(withId(R.id.role_spinner)).perform(click());
        onData(is("Entrant")).perform(click()); // choose entrant
        onView(withText("Facility Name")).check(matches(not(isDisplayed())));    // does facility show?
    }

    @Test
    public void testInvalidProfileRole() {   // test for an invalid role
        // this isn't really possible yet, since a role has to be valid in order to exist in the spinner.
        // may be implemented later
    }

    @Test
    public void testInvalidProfileFacility() {   // test for invalid facility

        onView(withId(R.id.username)).perform(ViewActions.clearText(), ViewActions.typeText("ValidUsername"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.email)).perform(ViewActions.clearText(), ViewActions.typeText("valid@valid.com"), ViewActions.closeSoftKeyboard());

        onView(withId(R.id.role_spinner)).perform(click());
        onData(is("Organizer")).perform(click()); // choose entrant
        onView(withText("Facility Name")).check(matches(isDisplayed()));    // does facility show?
        onView(withId(R.id.facility_name)).perform(ViewActions.clearText(), ViewActions.typeText("TestOver20CharacterFacilityName"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.sign_up_button)).perform(click());
        onView(withText("Invalid Signup")).check(matches(isDisplayed()));
        onView(withText("Facility is too long. Max of 20 characters.\nPlease try again.")).check(matches(isDisplayed()));

        onView(withText("OK")).perform(click());

        onView(withId(R.id.facility_name)).perform(ViewActions.clearText());
        onView(withId(R.id.sign_up_button)).perform(click());
        onView(withText("Invalid Signup")).check(matches(isDisplayed()));
        onView(withText("Invalid Facility.\nPlease try again.")).check(matches(isDisplayed()));
    }
}
