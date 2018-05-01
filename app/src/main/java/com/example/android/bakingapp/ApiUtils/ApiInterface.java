package com.example.android.bakingapp.ApiUtils;

import com.example.android.bakingapp.Models.Recipe;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * API interface is necessary for Retrofit
 */

public interface ApiInterface {

    @GET("topher/2017/May/59121517_baking/baking.json")
    Call<List<Recipe>> getRecipes();

}
