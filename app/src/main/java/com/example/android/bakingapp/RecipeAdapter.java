package com.example.android.bakingapp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.bakingapp.Models.Recipe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeAdapterViewHolder> {

    // List of Recipe objects to return
    private List<Recipe> mRecipeData;
    private RecipeAdapterOnClickHandler mClickHandler;

    // An interface for handling onclick events
    public interface RecipeAdapterOnClickHandler {
        void onClick(Recipe recipe);
    }

    // Create a constructor to to instantiate the clickhandler
    public RecipeAdapter(RecipeAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    @Override
    public RecipeAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the list item view
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.recipe_list_item, parent, false);

        return new RecipeAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecipeAdapterViewHolder holder, int position) {
        // For each Recipe object in the List<Recipe> get the name and bind it to the text view
        Recipe currentRecipe = mRecipeData.get(position);
        String recipeName = currentRecipe.getName();
        holder.mRecipeNameTextView.setText(recipeName);
    }

    @Override
    public int getItemCount() {
        if (mRecipeData == null) {
            return 0;
        }
        else {
            return mRecipeData.size();
        }
    }


    public class RecipeAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        @BindView(R.id.recipe_name_text_view)
        TextView mRecipeNameTextView;

        public RecipeAdapterViewHolder(View itemView) {
            super(itemView);
            // Bind views with Butterknife
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Recipe recipe = mRecipeData.get(position);
            mClickHandler.onClick(recipe);
        }
    }

    public void setRecipeData(List<Recipe> recipeList) {
        // Set the recipeList to mRecipeData
        mRecipeData = recipeList;
        notifyDataSetChanged();
    }
}
