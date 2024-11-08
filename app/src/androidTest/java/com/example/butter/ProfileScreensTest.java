package com.example.butter;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
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
 * This document will perform all tests corresponding to {@link ProfileFragment} and {@link EditProfileActivity}
 * The user testing this must have created an account established in the database first, OR ELSE THIS WON'T RUN
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
public class ProfileScreensTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<MainActivity>(MainActivity.class);

    @Test
    public void testViewProfileButton() {   // tests if viewing the profile shows the views
        onView(withId(R.id.profileIcon)).perform(click());
        onView(withText("Username")).check(matches(isDisplayed()));
        onView(withText("Email")).check(matches(isDisplayed()));
        onView(withText("Phone Number (Optional)")).check(matches(isDisplayed()));
        onView(withText("Role")).check(matches(isDisplayed()));
    }

    @Test
    public void testEditProfileButton() {   // tests if we can click the edit profile button and save changes
        onView(withId(R.id.profileIcon)).perform(click());
        onView(withId(R.id.edit_profile_button)).perform(click());
        onView(withText("Save Changes")).check(matches(isDisplayed()));
        onView(withId(R.id.save_changes_button)).perform(click());
    }

    @Test
    public void testEditProfileName() {   // tests if we can click the edit profile button and save changes, updating the username
        onView(withId(R.id.profileIcon)).perform(click());
        onView(withId(R.id.edit_profile_button)).perform(click());
        onView(withId(R.id.username)).perform(ViewActions.clearText(), ViewActions.typeText("Testing"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.save_changes_button)).perform(click());
        onView(withId(R.id.username_text)).check(matches(withText("Testing")));
    }

    @Test
    public void testInvalidProfileName() {   // tests if we have can have invalid usernames
        onView(withId(R.id.profileIcon)).perform(click());
        onView(withId(R.id.edit_profile_button)).perform(click());
        onView(withId(R.id.username)).perform(ViewActions.clearText(), ViewActions.typeText("TestingALargeCharacterNameHopefullyLargerThan30"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.save_changes_button)).perform(click());
        onView(withText("Invalid Signup")).check(matches(isDisplayed()));
        onView(withText("Username is too long. Max of 30 characters.\nPlease try again.")).check(matches(isDisplayed()));

        onView(withText("OK")).perform(click());

        onView(withId(R.id.username)).perform(ViewActions.clearText());
        onView(withId(R.id.save_changes_button)).perform(click());
        onView(withText("Invalid Signup")).check(matches(isDisplayed()));
        onView(withText("Username box is empty.\nPlease try again.")).check(matches(isDisplayed()));
    }

    @Test
    public void testEditProfileEmail() {   // tests if we can click the edit profile button and save changes, updating the email
        onView(withId(R.id.profileIcon)).perform(click());
        onView(withId(R.id.edit_profile_button)).perform(click());
        onView(withId(R.id.email)).perform(ViewActions.clearText(), ViewActions.typeText("testing@test.test"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.save_changes_button)).perform(click());
        onView(withId(R.id.email_text)).check(matches(withText("testing@test.test")));
    }

    @Test
    public void testInvalidProfileEmail() {   // tests if we have can have invalid emails
        onView(withId(R.id.profileIcon)).perform(click());
        onView(withId(R.id.edit_profile_button)).perform(click());
        onView(withId(R.id.email)).perform(ViewActions.clearText(), ViewActions.typeText("testing123.com"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.save_changes_button)).perform(click());
        onView(withText("Invalid Signup")).check(matches(isDisplayed()));
        onView(withText("Invalid Email Address.\nPlease try again.")).check(matches(isDisplayed()));

        onView(withText("OK")).perform(click());

        onView(withId(R.id.email)).perform(ViewActions.clearText());
        onView(withId(R.id.save_changes_button)).perform(click());
        onView(withText("Invalid Signup")).check(matches(isDisplayed()));
        onView(withText("Invalid Email Address.\nPlease try again.")).check(matches(isDisplayed()));
    }

    @Test
    public void testEditProfilePhone() {   // test if we can edit the phone number
        onView(withId(R.id.profileIcon)).perform(click());
        onView(withId(R.id.edit_profile_button)).perform(click());
        onView(withId(R.id.create_number_text)).perform(ViewActions.clearText());
        onView(withId(R.id.save_changes_button)).perform(click());
        onView(withId(R.id.password_text)).check(matches(withText("No phone given.")));

        onView(withId(R.id.edit_profile_button)).perform(click());
        onView(withId(R.id.create_number_text)).perform(ViewActions.clearText(), ViewActions.typeText("8888888888"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.save_changes_button)).perform(click());
        onView(withId(R.id.password_text)).check(matches(withText("8888888888")));
    }

    @Test
    public void testInvalidProfilePhone() {   // tests if we can have invalid phone numbers
        // no way to be invalid yet, since only numbers can be typed in. Will be implemented later.
    }

    @Test
    public void testEditProfileRole() {   // test if we can change roles
        onView(withId(R.id.profileIcon)).perform(click());
        onView(withId(R.id.edit_profile_button)).perform(click());
        onView(withId(R.id.edit_role_spinner)).perform(click());
        onData(is("Entrant")).perform(click()); // choose entrant
        onView(withId(R.id.save_changes_button)).perform(click());
        onView(withId(R.id.role_text)).check(matches(withText("Entrant")));

        onView(withId(R.id.edit_profile_button)).perform(click());
        onView(withId(R.id.edit_role_spinner)).perform(click());
        onData(is("Organizer")).perform(click()); // choose organizer
        onView(withText("Facility Name")).check(matches(isDisplayed()));    // does facility show?
        onView(withId(R.id.facility_name)).perform(ViewActions.clearText(), ViewActions.typeText("TESTFACILITY"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.save_changes_button)).perform(click());
        onView(withId(R.id.role_text)).check(matches(withText("Organizer")));
        onView(withText("Facility Name")).check(matches(isDisplayed()));    // does facility show?
        onView(withId(R.id.facility_name_text)).check(matches(withText("TESTFACILITY")));
    }

    @Test
    public void testInvalidProfileRole() {   // test for an invalid role
        // this isn't really possible yet, since a role has to be valid in order to exist in the spinner.
        // may be implemented later
    }

    @Test
    public void testEditProfileFacility() {   // test if we can add a facility
        onView(withId(R.id.profileIcon)).perform(click());
        onView(withId(R.id.edit_profile_button)).perform(click());
        onView(withId(R.id.edit_role_spinner)).perform(click());
        onData(is("Organizer")).perform(click()); // choose organizer
        onView(withText("Facility Name")).check(matches(isDisplayed()));    // does facility show?
        onView(withId(R.id.facility_name)).perform(ViewActions.clearText(), ViewActions.typeText("TESTFACILITY"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.save_changes_button)).perform(click());
        onView(withId(R.id.role_text)).check(matches(withText("Organizer")));
        onView(withText("Facility Name")).check(matches(isDisplayed()));    // does facility show?
        onView(withId(R.id.facility_name_text)).check(matches(withText("TESTFACILITY")));

        onView(withId(R.id.edit_profile_button)).perform(click());
        onView(withId(R.id.edit_role_spinner)).perform(click());
        onData(is("Both")).perform(click()); // choose both
        onView(withText("Facility Name")).check(matches(isDisplayed()));    // does facility show?
        onView(withId(R.id.facility_name)).perform(ViewActions.clearText(), ViewActions.typeText("TESTFACILITY2"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.save_changes_button)).perform(click());
        onView(withId(R.id.role_text)).check(matches(withText("Organizer & Entrant")));
        onView(withText("Facility Name")).check(matches(isDisplayed()));    // does facility show?
        onView(withId(R.id.facility_name_text)).check(matches(withText("TESTFACILITY2")));
    }

    @Test
    public void testInvalidProfileFacility() {   // test for invalid facility
        onView(withId(R.id.profileIcon)).perform(click());
        onView(withId(R.id.edit_profile_button)).perform(click());
        onView(withId(R.id.edit_role_spinner)).perform(click());
        onData(is("Organizer")).perform(click()); // choose organizer
        onView(withText("Facility Name")).check(matches(isDisplayed()));    // does facility show?
        onView(withId(R.id.facility_name)).perform(ViewActions.clearText(), ViewActions.typeText("TestOver20CharacterFacilityName"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.save_changes_button)).perform(click());
        onView(withText("Invalid Signup")).check(matches(isDisplayed()));
        onView(withText("Facility is too long. Max of 20 characters.\nPlease try again.")).check(matches(isDisplayed()));

        onView(withText("OK")).perform(click());

        onView(withId(R.id.facility_name)).perform(ViewActions.clearText());
        onView(withId(R.id.save_changes_button)).perform(click());
        onView(withText("Invalid Signup")).check(matches(isDisplayed()));
        onView(withText("Invalid Facility.\nPlease try again.")).check(matches(isDisplayed()));
    }

    @Test
    public void testEditProfileAll() { // test editing all attributes of profile
        testEditProfileName();
        testEditProfileEmail();
        testEditProfilePhone();
        testEditProfileRole();
        testEditProfileFacility();
        onView(withText("Testing")).check(matches(isDisplayed()));
        onView(withText("testing@test.test")).check(matches(isDisplayed()));
        onView(withText("8888888888")).check(matches(isDisplayed()));
        onView(withText("Organizer & Entrant")).check(matches(isDisplayed()));
        onView(withText("TESTFACILITY2")).check(matches(isDisplayed()));
    }
}
