package com.example.android.bakingapp;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import timber.log.Timber;

/**
 * Implementation of App Widget functionality.
 */
public class BakingWidgetProvider extends AppWidgetProvider {

    // Keys
    public static final String WIDGET_RECIPE_PREF_KEY = "widget-recipe";
    public static final String INGREDIENT_PREF_KEY = "ingredient";

    // Set the default ingredients list as Nutella Pie. This will change once the user uses the app.
    public static final String DEFAULT_INGREDIENTS = "Nutella Pie:\n" +
            "Graham Cracker crumbs; unsalted butter, melted; granulated sugar; salt; vanilla; Nutella or other chocolate-hazelnut spread; Mascapone Cheese(room temperature); heavy cream(cold); cream cheese(softened)";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Get the ingredient list for the last recipe the user looked at
        SharedPreferences sharedPreferences = context.getSharedPreferences(WIDGET_RECIPE_PREF_KEY,
                Context.MODE_PRIVATE);
        String ingredientList = sharedPreferences.getString(INGREDIENT_PREF_KEY,
                DEFAULT_INGREDIENTS);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.baking_widget_provider);
        views.setTextViewText(R.id.appwidget_text, ingredientList);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

