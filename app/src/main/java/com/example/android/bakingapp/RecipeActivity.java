package com.example.android.bakingapp;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.android.bakingapp.Adapters.StepAdapter;
import com.example.android.bakingapp.Models.Ingredient;
import com.example.android.bakingapp.Models.Recipe;
import com.example.android.bakingapp.Models.Step;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class RecipeActivity extends AppCompatActivity
        implements StepAdapter.StepAdapterOnClickHandler, RecipeFragment.onItemClickListener{

    // Keys
    private static final String INSTANCE_VIDEO_PLAYING_KEY = "video-playing-key";
    private static final String INSTANCE_VIDEO_POSITION_KEY = "video-position-key";
    private static final String INSTANCE_STATE_KEY = "last-step";
    public static final String INTENT_EXTRA_STEP_KEY = "step-key";
    public static final String INTENT_EXTRA_PLACE_KEY = "place-key";
    public static final String PLACE_ID_FIRST = "place-first";
    public static final String PLACE_ID_MIDDLE = "place-middle";
    public static final String PLACE_ID_LAST = "place-last";

    // member variables
    private RecyclerView mRecyclerView;
    private StepAdapter mStepAdapter;
    private Recipe mRecipe;
    private List<Ingredient> mIngredientList;
    private Step mLastStepClicked;
    private long mVideoPosition;
    private boolean mIsVideoPlaying;

    View stepFragmentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        // Get the Recipe object
        mRecipe = getIntent().getParcelableExtra(MainActivity.INTENT_EXTRA_RECIPE_KEY);

        // Set the title in the actionbar along with number of servings
        String title;
        if (mRecipe.getServings() == 0) {
            title = mRecipe.getName();
        } else {
            title = mRecipe.getName() + " (" + mRecipe.getServings() + getString(R.string.servings) + ")";
        }
        setTitle(title);

        // If true, we are on a tablet
        if (getResources().getBoolean(R.bool.isTablet)) {
            stepFragmentView = findViewById(R.id.step_frag_frame_layout);

            // Create the Step fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            StepFragment stepFragment = new StepFragment();

            //TODO work with mlaststepclicked as it should be step 0 by default
            // If possible, have the step fragment show the last viewed step
            if (savedInstanceState != null) {
                mLastStepClicked = savedInstanceState.getParcelable(INSTANCE_STATE_KEY);
                mVideoPosition = savedInstanceState.getLong(INSTANCE_VIDEO_POSITION_KEY);
                mIsVideoPlaying = savedInstanceState.getBoolean(INSTANCE_VIDEO_PLAYING_KEY);
            } else {
                // otherwise, show the first step by default
                mLastStepClicked = mRecipe.getSteps().get(0);
                mVideoPosition = 0;
                mIsVideoPlaying = false;
            }
            Timber.d(String.valueOf(mVideoPosition) + "oncreate");
            stepFragment.setStepInfo(mLastStepClicked);
            stepFragment.setVideoPosition(mVideoPosition, mIsVideoPlaying);
            fragmentManager.beginTransaction()
                    .add(R.id.step_frag_frame_layout, stepFragment)
                    .commit();

        } else {
            // We are on a phone
            // Get a reference to the recycler view
            mRecyclerView = findViewById(R.id.recipe_steps_recycler_view);
            mRecyclerView.setNestedScrollingEnabled(false);

            // Set a layoutmanager to the recycler view
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            // Instantiate StepAdapter and set it to recycler view
            mStepAdapter = new StepAdapter(this);
            mRecyclerView.setAdapter(mStepAdapter);

            // Extract the Ingredient object from the Recipe object
            mIngredientList = mRecipe.getIngredients();

            // Create Lists to hold the information for each ingredient needed for this recipe
            List<Double> quantities = new ArrayList<>();
            List<String> units = new ArrayList<>();
            List<String> ingredients = new ArrayList<>();

            // Loop through each ingredient object in the list and extract the relevant information
            // then add it to the proper list
            for (Ingredient ingredient : mIngredientList) {
                quantities.add(ingredient.getQuantity());
                units.add(ingredient.getMeasure());
                ingredients.add(ingredient.getIngredient());
            }

            // Create one String to hold all of the ingredient information in this list
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < quantities.size(); i++) {
                stringBuilder.append(quantities.get(i) + " " +
                        units.get(i).toLowerCase() + " " +
                        ingredients.get(i) + "\n");
            }
            String ingredientsString = stringBuilder.toString();

            // Set the ingredients string to the appropriate textview
            TextView ingredientsTextView = findViewById(R.id.recipe_ingredients_text_view);
            ingredientsTextView.setText(ingredientsString);

            // Extract the list of Step objects from the Recipe object
            mStepAdapter.setStepData(mRecipe.getSteps());
        }
    }

    @Override
    public void onClick(Step step) {
        // Use intent to open the StepActivity, and pass on the clicked on Step information
        // Also pass on the place (is the step first, last, or middle?)
        Intent intent = new Intent(this, StepActivity.class);
        String place = checkStepIdPlace(step);
        intent.putExtra(INTENT_EXTRA_STEP_KEY, step);
        intent.putExtra(INTENT_EXTRA_PLACE_KEY, place);
        startActivityForResult(intent, 123);
    }

    // Check whether the step is the first, the last, or in the middle
    public String checkStepIdPlace(Step step) {
        if (step.getId() == 0) {
            return PLACE_ID_FIRST;
        }
        if (step.getId() == mRecipe.getSteps().size() - 1) {
            return PLACE_ID_LAST;
        } else {
            return PLACE_ID_MIDDLE;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Get the id of the last step the user viewed
        if (data != null) {
            int stepId = data.getIntExtra(StepActivity.INTENT_EXTRA_CURRENT_ID_KEY, -1);
            if (stepId > -1) {
                Step newStep;
                if (resultCode == 101) {
                    // User clicked the "next" button
                    newStep = mRecipe.getSteps().get(stepId + 1);
                } else {
                    // resultCode == 102
                    // User clicked the "previous" button
                    newStep = mRecipe.getSteps().get(stepId - 1);
                }
                Intent intent = new Intent(this, StepActivity.class);
                intent.putExtra(INTENT_EXTRA_STEP_KEY, newStep);
                intent.putExtra(INTENT_EXTRA_PLACE_KEY, checkStepIdPlace(newStep));
                startActivityForResult(intent, 123);
            }
        }
    }

    @Override
    public void onStepSelected(Step step) {
        // Method used in Tablet mode. Chooses the appropriate Step object
        // to show in the step info fragment
        mLastStepClicked = step;
        StepFragment newStepFragment = new StepFragment();
        newStepFragment.setStepInfo(step);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.step_frag_frame_layout, newStepFragment).commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(INSTANCE_STATE_KEY, mLastStepClicked);
        outState.putLong(INSTANCE_VIDEO_POSITION_KEY, mVideoPosition);
        outState.putBoolean(INSTANCE_VIDEO_PLAYING_KEY, mIsVideoPlaying);
        Timber.d(String.valueOf(mVideoPosition) + "onsaveinstance");
    }

//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        if (savedInstanceState != null) {
//            mLastStepClicked = savedInstanceState.getParcelable(INSTANCE_STATE_KEY);
//        }
//    }

    public void setFragVideoPosition(long position, boolean isPlaying) {
        mVideoPosition = position;
        mIsVideoPlaying = isPlaying;
        Timber.d(String.valueOf(mVideoPosition) + "setfrag");
    }
}
