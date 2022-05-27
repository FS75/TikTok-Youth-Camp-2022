package com.example.taxiapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class MainActivityTest{
    @get:Rule var activityScenarioRule = activityScenarioRule<MainActivity>()

    //Checking if main activity is visible
    @Test
    fun checkActivityVisibility(){
        onView(withId(R.id.linearLayout1))
            .check(matches(isDisplayed()))
        onView(withId(R.id.linearLayout2))
            .check(matches(isDisplayed()))
    }

    //Checking if objects in the app is visible
    @Test
    fun checkingTextVisibility(){
        onView(withId(R.id.titleTextView))
            .check(matches(isDisplayed()))
        onView(withId(R.id.txtId))
            .check(matches(isDisplayed()))
        onView(withId(R.id.txtId2))
            .check(matches(isDisplayed()))
        onView(withId(R.id.seekBarText))
            .check(matches(isDisplayed()))
        onView(withId(R.id.seekBar1))
            .check(matches(isDisplayed()))
        onView(withId(R.id.viewMapBtn))
            .check(matches(isDisplayed()))
        onView(withId(R.id.refresh))
            .check(matches(isDisplayed()))
    }

    //Checking if text in view is correct
    @Test
    fun testTextSpelling(){
        onView(withId(R.id.titleTextView))
            .check(matches(withText(R.string.tiktok_youth_camp_project)))
        onView(withId(R.id.txtId))
            .check(matches(withText(R.string.available_taxis_in_sg)))
        onView(withId(R.id.txtId2))
            .check(matches(withText(R.string.available_taxis_near_you)))
//        onView(withId(R.id.seekBarText))
//            .check(matches(withText(R.string.taxi_visibility_range)))
        onView(withId(R.id.viewMapBtn))
            .check(matches(withText(R.string.view_map)))
        onView(withId(R.id.refresh))
            .check(matches(withText(R.string.refresh)))

    }

    //Testing click for navigating to viewMap

    @Test
    fun navigateViewMap(){
        onView(withId(R.id.viewMapBtn))
            .perform(click())
        //Check if Map is opened and visible
        onView(withId(R.id.map))
            .check(matches(isDisplayed()))
    }

}