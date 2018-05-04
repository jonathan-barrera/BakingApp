package com.example.android.bakingapp;

import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static java.lang.Thread.sleep;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTests {

    private static final String FIRST_RECIPE_NAME = "Nutella Pie";
    private static final String SECOND_STEP_TEXT = "2. Prep the cookie crust.";
    private static final String FIRST_STEP_DESC_TEXT = "1. Preheat the oven to 350\u00b0F. Butter a 9\" deep dish pie pan.";
    private static final String SECOND_STEP_DESC_TEXT = "2. Whisk the graham cracker crumbs, 50 grams (1/4 cup) of sugar, and 1/2 teaspoon of salt together in a medium bowl. Pour the melted butter and 1 teaspoon of vanilla into the dry ingredients and stir together until evenly mixed.";

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    private IdlingResource mIdlingResource;

    @Before
    public void registerIdlingResource() {
        mIdlingResource = mActivityTestRule.getActivity().getIdlingResource();
        // To prove that the test fails, omit this call:
        IdlingRegistry.getInstance().register(mIdlingResource);
    }

    @Test
    public void mainActivity_ShowRecipes() {
        // Get a reference to the first item in the recycler view and check if it is called
        // Nutella Pie
        onView(withId(R.id.recipe_recycler_view)).check(matches(hasDescendant(withText(FIRST_RECIPE_NAME))));
    }

    @Test
    public void clickOnRecyclerViewItem_OpensRecipeActivity() {
        // Perform click on first recipe
        //onData(anything()).inAdapterView(withId(R.id.recipe_recycler_view)).atPosition(0).perform(click());
        onView(withId(R.id.recipe_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Check that the RecipeActivity opens with the correct steps
        onView(withId(R.id.recipe_steps_recycler_view)).check(matches(hasDescendant(withText(SECOND_STEP_TEXT))));
    }

    @Test
    public void clickOnStep_OpenStepActivity() {
        // Perform click on first recipe
        //onData(anything()).inAdapterView(withId(R.id.recipe_recycler_view)).atPosition(0).perform(click());
        onView(withId(R.id.recipe_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Check that the RecipeActivity opens with the correct steps
        onView(withId(R.id.recipe_steps_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

        // Check that the StepActivity opens with the correct step
        onView(withId(R.id.step_desc_text_view)).check(matches(withText(FIRST_STEP_DESC_TEXT)));
    }

    @Test
    public void clickOnNextStep_OpenNextStepActivity() {
        // Perform click on first recipe
        //onData(anything()).inAdapterView(withId(R.id.recipe_recycler_view)).atPosition(0).perform(click());
        onView(withId(R.id.recipe_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Check that the RecipeActivity opens with the correct steps
        onView(withId(R.id.recipe_steps_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

        // Start the step activity and click on the "next" button
        onView(withId(R.id.next_button)).perform(click());

        // Check if this opens the next step activity
        onView(withId(R.id.step_desc_text_view)).check(matches(withText(SECOND_STEP_DESC_TEXT)));
    }

    @Test
    public void clickOnPreviousStep_OpenPreviousStepActivity() {
        // Perform click on first recipe
        //onData(anything()).inAdapterView(withId(R.id.recipe_recycler_view)).atPosition(0).perform(click());
        onView(withId(R.id.recipe_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Check that the RecipeActivity opens with the correct steps
        onView(withId(R.id.recipe_steps_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(3, click()));

        // The app needs a little bit of time to get the button to appear
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Start the step activity and click on the "next" button
        onView(withId(R.id.previous_button)).perform(click());

        // Check if this opens the next step activity
        onView(withId(R.id.step_desc_text_view)).check(matches(withText(SECOND_STEP_DESC_TEXT)));
    }

    @After
    public void unregisterIdlingResource() {
        if (mIdlingResource != null) {
            IdlingRegistry.getInstance().unregister(mIdlingResource);
        }
    }

}
