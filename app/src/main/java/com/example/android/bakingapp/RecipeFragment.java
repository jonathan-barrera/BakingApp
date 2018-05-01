package com.example.android.bakingapp;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.bakingapp.Models.Ingredient;
import com.example.android.bakingapp.Models.Recipe;
import com.example.android.bakingapp.Models.Step;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class RecipeFragment extends Fragment implements StepAdapter.StepAdapterOnClickHandler {

    private Recipe mRecipe;
    private RecyclerView mRecyclerView;
    private StepAdapter mStepAdapter;
    private List<Ingredient> mIngredientList;

    //Empty constructor
    public RecipeFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the fragment layout
        View rootView = inflater.inflate(R.layout.fragment_recipe, container, false);

        // Get a reference to the recycler view
        mRecyclerView = rootView.findViewById(R.id.recipe_steps_recycler_view);
        mRecyclerView.setNestedScrollingEnabled(false);

        // Set a layoutmanager to the recycler view
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Instantiate StepAdapter and set it to recycler view
        mStepAdapter = new StepAdapter(this);
        mRecyclerView.setAdapter(mStepAdapter);

        // Get the Recipe object
        mRecipe = getActivity().getIntent().getParcelableExtra("Recipe");

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
        TextView ingredientsTextView = rootView.findViewById(R.id.recipe_ingredients_text_view);
        ingredientsTextView.setText(ingredientsString);

        // Extract the list of Step objects from the Recipe object
        mStepAdapter.setStepData(mRecipe.getSteps());

        return rootView;
    }

    @Override
    public void onClick(Step step) {
        Intent intent = new Intent(getActivity(), StepActivity.class);
        intent.putExtra("Step", step);
        startActivityForResult(intent, 123);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        int stepId = data.getIntExtra("currentId", -1);
        Timber.d("onactivityresult called, id is " + stepId);
        if (stepId > -1) {
            Step newStep = mRecipe.getSteps().get(stepId + 1);
            Intent intent = new Intent(getActivity(), StepActivity.class);
            intent.putExtra("Step", newStep);
            startActivityForResult(intent, 123);
        }
    }
}
