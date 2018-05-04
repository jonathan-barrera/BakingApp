package com.example.android.bakingapp;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.example.android.bakingapp.Adapters.RecipeAdapter;
import com.example.android.bakingapp.ApiUtils.ApiClient;
import com.example.android.bakingapp.ApiUtils.ApiInterface;
import com.example.android.bakingapp.IdlingResource.SimpleIdlingResource;
import com.example.android.bakingapp.Models.Ingredient;
import com.example.android.bakingapp.Models.Recipe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity
        implements RecipeAdapter.RecipeAdapterOnClickHandler{

    private RecyclerView mRecyclerView;
    private RecipeAdapter mRecipeAdapter;
    private Parcelable mRecyclerViewSavedState;

    public static final String INTENT_EXTRA_RECIPE_KEY = "Recipe";
    public static final String RECYCLER_VIEW_POSITION_KEY = "recycler-view-position-key";

    // The Idling Resource which will be null in production.
    @Nullable
    private SimpleIdlingResource mIdlingResource;

    /**
     * Only called from test, creates and returns a new {@link SimpleIdlingResource}.
     */
    @VisibleForTesting
    @NonNull
    public IdlingResource getIdlingResource() {
        if (mIdlingResource == null) {
            mIdlingResource = new SimpleIdlingResource();
        }
        return mIdlingResource;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getIdlingResource();

        // Necessary to use Timber logging function
        Timber.plant(new Timber.DebugTree());

        // Bind views in onCreate
        ButterKnife.bind(this);

        // Set title to Recipes for this Activity
        setTitle(getString(R.string.recipes));

        if (findViewById(R.id.recipe_recycler_view_grid) == null) {
            // Set a LayoutManager to the recyclerview
            mRecyclerView = findViewById(R.id.recipe_recycler_view);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        } else {
            mRecyclerView = findViewById(R.id.recipe_recycler_view_grid);
            // We are looking at the table, make a grid view layout
            GridLayoutManager layoutManager = new GridLayoutManager(this,
                    3, GridLayoutManager.VERTICAL, false);
            mRecyclerView.setLayoutManager(layoutManager);
        }

        // Initialize Adapter and set to Recyclerview
        mRecipeAdapter = new RecipeAdapter(this);
        mRecyclerView.setAdapter(mRecipeAdapter);

        // Set RecyclerView to savedinstance position (if it exists)
        if (mRecyclerViewSavedState != null) {
            mRecyclerView.getLayoutManager().onRestoreInstanceState(mRecyclerViewSavedState);
        }

        // Create idling resource
        if (mIdlingResource != null) {
            mIdlingResource.setIdleState(false);
        }

        // API declarations must be interfaces, create one here
        ApiInterface accessApiService = ApiClient.getClient().create(ApiInterface.class);

        // Create api call for recipes
        Call<List<Recipe>> recipeCall = accessApiService.getRecipes();

        // Consume Api Asynchronously
        recipeCall.enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                if (mIdlingResource != null) {
                    mIdlingResource.setIdleState(true);
                }
                mRecipeAdapter.setRecipeData(response.body());
            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                Timber.d(getString(R.string.json_download_failed));
                Timber.d(t.toString());
            }
        });
    }

    @Override
    public void onClick(Recipe recipe) {
        // Update SharedPreferences (to be used to update the widget)
        SharedPreferences sharedPreferences = getSharedPreferences(
                BakingWidgetProvider.WIDGET_RECIPE_PREF_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(BakingWidgetProvider.INGREDIENT_PREF_KEY, getIngredientsString(recipe));
        editor.apply();

        // Update the widget
        Intent widgetIntent = new Intent(this, BakingWidgetProvider.class);
        widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(new ComponentName(getApplication(), BakingWidgetProvider.class));
        widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(widgetIntent);

        // Use intent to open the RecipeActivity
        Intent intent = new Intent(this, RecipeActivity.class);
        intent.putExtra(INTENT_EXTRA_RECIPE_KEY, recipe);
        startActivity(intent);
    }

    // Make a string holding the entire list of ingredients for the last selected recipe
    public String getIngredientsString(Recipe recipe) {
        StringBuilder builder = new StringBuilder();
        builder.append(recipe.getName() + ":\n");
        List<Ingredient> ingredients = recipe.getIngredients();
        // Loop through list of ingredients and add ech one to stringbuilder
        for (Ingredient ingredient : ingredients) {
            builder.append(ingredient.getIngredient() + "; ");
        }
        // Get rid of the last "; "
        builder.delete(builder.length() - 2, builder.length());

        return builder.toString();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(RECYCLER_VIEW_POSITION_KEY,
                mRecyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mRecyclerViewSavedState = savedInstanceState.getParcelable(RECYCLER_VIEW_POSITION_KEY);
        }
    }
}
