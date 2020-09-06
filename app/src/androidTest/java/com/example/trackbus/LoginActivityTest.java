package com.example.trackbus;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest

    public  class LoginActivityTest {

    @Rule
    public ActivityTestRule<LoginActivity> rule = new ActivityTestRule<>(LoginActivity.class);

    //to login into the application
        @Test
        public void LoginActivityUI() {

            onView(withId(R.id.textInputEditTextEmail)).perform(typeText("p@gmail.com"));
            closeSoftKeyboard();

            onView(withId(R.id.textInputEditTextPassword)).perform(typeText("1234567"));
            closeSoftKeyboard();
            onView(withId(R.id.appCompatButtonLogin)).perform(click());

        }

    //Login UI link test
    @Test
    public void checkingRegisterlinkbutton() throws Exception{
        onView(withId(R.id.textViewLinkRegister)).perform(click());
        onView((withId(R.id.nestedSignupScrollView))).check(matches(isDisplayed()));

    }


    // check validation work or not
    @Test
    public void testCheckFieldsEmailEmpty(){
        onView(withId(R.id.appCompatButtonLogin)).perform(click());

        onView(withId(com.google.android.material.R.id.textinput_error))
                .check(matches(withText("Enter a email"))).check(matches(isDisplayed()));

    }

    //checking validation of password
    @Test
    public void testCheckFieldsPasswordEmpty(){

        onView(withId(R.id.textInputEditTextEmail)).perform(typeText("suraj@gmail.comk"));
        closeSoftKeyboard();
        onView(withId(R.id.appCompatButtonLogin)).perform(click());

        onView(withId(com.google.android.material.R.id.textinput_error))
                .check(matches(withText("Enter a password"))).check(matches(isDisplayed()));

    }
}
