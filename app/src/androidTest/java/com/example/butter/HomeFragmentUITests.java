package com.example.butter;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static com.google.common.base.CharMatcher.is;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;

import android.Manifest;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.Intent;
import android.provider.Telephony;

import androidx.annotation.ContentView;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.internal.platform.content.PermissionGranter;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class HomeFragmentUITests {

    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);

    //Tests to see if the qr code button leads to qr code scanner camera.
    @Test
    public void testQrScannerButton() {
        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();

        // allow camera permission
        uiAutomation.grantRuntimePermission(

        );
    }

    /**
     * Tests to see if clicking "browse events" will lead to right
     * listView showing up
     */
    @Test
    public void testBrowseEvents() {
//        onView(withId(R.id.entrants_spinner)).perform(click());
//        onData( withText("Browse Events")).inRoot(isPlatformPopup()).perform(click());
//
//        try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
//        onView(withId(R.id.admin_list_view)).check(matches(isDisplayed()));
    }

}
