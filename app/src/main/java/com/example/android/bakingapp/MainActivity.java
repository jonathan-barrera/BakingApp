package com.example.android.bakingapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.android.bakingapp.Adapters.RecipeAdapter;
import com.example.android.bakingapp.ApiUtils.ApiClient;
import com.example.android.bakingapp.ApiUtils.ApiInterface;
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

    @BindView(R.id.recipe_recycler_view)
    RecyclerView mRecyclerView;

    private RecipeAdapter mRecipeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Necessary to use Timber logging function
        Timber.plant(new Timber.DebugTree());

        // Bind views in onCreate
        ButterKnife.bind(this);

        // Set title to Recipes for this Activity
        setTitle("Recipes");

        // Set a LayoutManager to the recyclerview
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Adapter and set to Recyclerview
        mRecipeAdapter = new RecipeAdapter(this);
        mRecyclerView.setAdapter(mRecipeAdapter);

        // API declarations must be interfaces, create one here
        ApiInterface accessApiService = ApiClient.getClient().create(ApiInterface.class);

        // Create api call for recipes
        Call<List<Recipe>> recipeCall = accessApiService.getRecipes();

        // Consume Api Asynchronously
        recipeCall.enqueue(new Callback<List<Recipe>>() {
            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                mRecipeAdapter.setRecipeData(response.body());
            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {
                Timber.d("JSON download failed.");
                Timber.d(t.toString());
            }
        });
    }

    @Override
    public void onClick(Recipe recipe) {
        // Use intent to open the RecipeActivity
        Intent intent = new Intent(this, RecipeActivity.class);
        intent.putExtra("Recipe", recipe);
        startActivity(intent);
    }
}
