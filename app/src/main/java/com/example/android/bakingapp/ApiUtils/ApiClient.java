package com.example.android.bakingapp.ApiUtils;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Create a Retrofit object that will handle connecting to the JSON file online,
 * getting the data, and parsing the JSON information into Java objects (using GSON)
 */

public class ApiClient {

    // URL for the JSON data (provided by Udacity) -- no api key needed
    private static final String RECIPE_JSON_URL =
            "https://d17h27t6h515a5.cloudfront.net/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(RECIPE_JSON_URL)
                    .addConverterFactory(GsonConverterFactory.create()) // Include Gson converter
                    .build();
        }
        return retrofit;
    }

}
