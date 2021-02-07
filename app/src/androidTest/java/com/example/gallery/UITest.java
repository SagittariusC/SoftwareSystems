package com.example.gallery;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;
import android.widget.DatePicker;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;


import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.espresso.matcher.ViewMatchers;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


public class UITest {
    // IntentsTestRule is an extension of ActivityTestRule. IntentsTestRule sets up Espresso-Intents
    // before each Test is executed to allow stubbing and validation of intents.
    @Rule
    public IntentsTestRule<MainActivity> intentsRule = new IntentsTestRule<>(MainActivity.class);

    @Test
    public void validateSnapCameraScenario() {
        // Create a bitmap we can use for our simulated camera image
        Bitmap icon = BitmapFactory.decodeFile("app/src/test.png");
        // Build a result to return from the Camera app
        Intent resultData = new Intent();
        resultData.putExtra("data", icon);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        // Stub out the Camera. When an intent is sent to the Camera, this tells Espresso to respond
        // with the ActivityResult we just created
        Intents.intending(IntentMatchers.toPackage("com.android.camera2")).respondWith(result);

        // Now that we have the stub in place, click on the button in our app that launches into the Camera
        onView(withId(R.id.snap)).perform(click());

        // We can also validate that an intent resolving to the "camera" activity has been sent out by our app
         Intents.intended(IntentMatchers.toPackage("com.android.camera2"));

        // ... additional test steps and validation ...
    }

    @Test
    public void searchCaption() throws InterruptedException {
        Thread.sleep(1000);
        onView(withId(R.id.filter_button)).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.startDateText)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(2021, 2, 4));
        onView(withId(android.R.id.button1)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.endDateText)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).perform(PickerActions.setDate(2021, 2, 7));
        onView(withId(android.R.id.button1)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.captionText)).perform(typeText("do"), closeSoftKeyboard());
        Thread.sleep(1000);
        onView(withId(R.id.TopLeftLat)).perform(typeText("48.86"), closeSoftKeyboard());
        Thread.sleep(1000);
        onView(withId(R.id.TopLeftLong)).perform(typeText("2.25"), closeSoftKeyboard());
        Thread.sleep(1000);
        onView(withId(R.id.BottomRightLat)).perform(typeText("48.84"), closeSoftKeyboard());
        Thread.sleep(1000);
        onView(withId(R.id.BottomRightLong)).perform(typeText("2.35"), closeSoftKeyboard());
        Thread.sleep(1000);
        onView(withId(R.id.searchButton)).perform(click());
        Thread.sleep(4000);
        onView(withId(R.id.button_right)).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.button_left)).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.button_left)).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.searchImageView)).perform(click());
        Thread.sleep(5000);

        
    }
}