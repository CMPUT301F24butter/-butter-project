package com.example.butter;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.hasToString;

import android.Manifest;
import android.content.Intent;
import android.widget.DatePicker;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CreateEventTest {
    public ActivityTestRule<CreateEventFragment> activityRule =
            new ActivityTestRule<>(CreateEventFragment.class, false, false);

    @Before
    public void setUp() {
        String deviceID = "a256a5d278042a1d"; // CHANGE THIS TO YOUR ID
        Intent intent = new Intent();
        intent.putExtra("deviceID", deviceID);
        activityRule.launchActivity(intent);
    }

    @Test
    public void testNoName() {
        onView(withId(R.id.start_date)).perform(click());
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2026, 1, 1));
        onView(withText("OK")).perform(click());

        onView(withId(R.id.end_date)).perform(click());
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2026, 2, 1));
        onView(withText("OK")).perform(click());

        onView(withId(R.id.event_date)).perform(click());
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2026, 3, 1));
        onView(withText("OK")).perform(click());

        onView(withId(R.id.event_description)).perform(ViewActions.typeText("Event Description"));

        onView(withId(R.id.create_event_button)).perform(click());
        onView(withText("Create Event")).check(matches(isDisplayed()));
    }

    @Test
    public void testInvalidName() {
        onView(withId(R.id.start_date)).perform(click());
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2026, 1, 1));
        onView(withText("OK")).perform(click());

        onView(withId(R.id.end_date)).perform(click());
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2026, 2, 1));
        onView(withText("OK")).perform(click());

        onView(withId(R.id.event_date)).perform(click());
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2026, 3, 1));
        onView(withText("OK")).perform(click());

        onView(withId(R.id.event_description)).perform(ViewActions.typeText("Event Description"));

        onView(withId(R.id.name_event)).perform(ViewActions.typeText("test-name"));

        onView(withId(R.id.create_event_button)).perform(click());
        onView(withText("Create Event")).check(matches(isDisplayed()));

        onView(withId(R.id.name_event)).perform(clearText());
        onView(withId(R.id.name_event)).perform(ViewActions.typeText("test_name"));

        onView(withId(R.id.create_event_button)).perform(click());
        onView(withText("Create Event")).check(matches(isDisplayed()));
    }

    @Test
    public void testNotConsecutiveDates() {
        onView(withId(R.id.name_event)).perform(ViewActions.typeText("Event name"));

        onView(withId(R.id.start_date)).perform(click());
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2025, 3, 1));
        onView(withText("OK")).perform(click());

        onView(withId(R.id.end_date)).perform(click());
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2025, 2, 1));
        onView(withText("OK")).perform(click());

        onView(withId(R.id.event_date)).perform(click());
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2025, 1, 1));
        onView(withText("OK")).perform(click());

        onView(withId(R.id.event_description)).perform(ViewActions.typeText("Event Description"));

        onView(withId(R.id.create_event_button)).perform(click());
        onView(withText("Create Event")).check(matches(isDisplayed()));
    }

    @Test
    public void testConsecutiveOldDates() {
        onView(withId(R.id.name_event)).perform(ViewActions.typeText("Event name"));

        onView(withId(R.id.start_date)).perform(click());
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2020, 1, 1));
        onView(withText("OK")).perform(click());

        onView(withId(R.id.end_date)).perform(click());
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2020, 2, 1));
        onView(withText("OK")).perform(click());

        onView(withId(R.id.event_date)).perform(click());
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2020, 3, 1));
        onView(withText("OK")).perform(click());

        onView(withId(R.id.event_description)).perform(ViewActions.typeText("Event Description"));

        onView(withId(R.id.create_event_button)).perform(click());
        onView(withText("Create Event")).check(matches(isDisplayed()));
    }

    @Test
    public void testMissingDate() {
        onView(withId(R.id.name_event)).perform(ViewActions.typeText("Event name"));

        onView(withId(R.id.start_date)).perform(click());
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2025, 1, 1));
        onView(withText("OK")).perform(click());

        onView(withId(R.id.end_date)).perform(click());
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2025, 2, 1));
        onView(withText("OK")).perform(click());

        onView(withId(R.id.event_description)).perform(ViewActions.typeText("Event Description"));

        onView(withId(R.id.create_event_button)).perform(click());
        onView(withText("Create Event")).check(matches(isDisplayed()));
    }

    @Test
    public void testNoDescription() {
        onView(withId(R.id.name_event)).perform(ViewActions.typeText("Event name"));

        onView(withId(R.id.start_date)).perform(click());
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2025, 1, 1));
        onView(withText("OK")).perform(click());

        onView(withId(R.id.end_date)).perform(click());
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2025, 2, 1));
        onView(withText("OK")).perform(click());

        onView(withId(R.id.event_date)).perform(click());
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2025, 3, 1));
        onView(withText("OK")).perform(click());

        onView(withId(R.id.create_event_button)).perform(click());
        onView(withText("Create Event")).check(matches(isDisplayed()));
    }
}
