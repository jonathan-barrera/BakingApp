package com.example.android.bakingapp;

import android.content.Intent;
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
    //TODO fix bug where the activity crashes when the orientation changes for the tablet (only horizontal
    // to vertical, vertical to horizontal works. why?

    // boolean to keep track of whether we are on a phone or a table
    private boolean mTwoPane;

    private RecyclerView mRecyclerView;
    private StepAdapter mStepAdapter;
    private Recipe mRecipe;
    private List<Ingredient> mIngredientList;

    View stepFragmentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("recipe activity oncreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        // Get the Recipe object
        mRecipe = getIntent().getParcelableExtra("Recipe");

        // Set the title in the actionbar
        setTitle(mRecipe.getName());

        // If true, we are on a tablet
        if (findViewById(R.id.step_frag_frame_layout) != null) {
            mTwoPane = true;
            stepFragmentView = findViewById(R.id.step_frag_frame_layout);

            // Create the Step fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            StepFragment stepFragment = new StepFragment();
            stepFragment.setStepInfo(mRecipe.getSteps().get(0));
            fragmentManager.beginTransaction()
                    .add(R.id.step_frag_frame_layout, stepFragment)
                    .commit();


        } else {
            mTwoPane = false;
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
        if (mTwoPane) {
            Timber.d("onclick works for tablet");
        } else {
            Intent intent = new Intent(this, StepActivity.class);
            String place = checkStepIdPlace(step);
            intent.putExtra("Step", step);
            intent.putExtra("Place", place);
            startActivityForResult(intent, 123);
        }
    }

    public String checkStepIdPlace(Step step) {
        if (step.getId() == 0) {
            return "first";
        }
        if (step.getId() == mRecipe.getSteps().size() - 1) {
            return "last";
        } else {
            return "middle";
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Get the id of the last step seen
        if (data != null) {
            int stepId = data.getIntExtra("currentId", -1);
            if (stepId > -1) {
                Step newStep;
                if (resultCode == 101) {
                    newStep = mRecipe.getSteps().get(stepId + 1);
                } else {
                    // resultCode == 102
                    newStep = mRecipe.getSteps().get(stepId - 1);
                }
                Intent intent = new Intent(this, StepActivity.class);
                intent.putExtra("Step", newStep);
                intent.putExtra("Place", checkStepIdPlace(newStep));
                startActivityForResult(intent, 123);
            }
        }
    }

    @Override
    public void onStepSelected(Step step) {
        StepFragment newStepFragment = new StepFragment();
        newStepFragment.setStepInfo(step);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.step_frag_frame_layout, newStepFragment).commit();
    }

}
