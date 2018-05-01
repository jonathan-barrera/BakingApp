package com.example.android.bakingapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.bakingapp.Models.Step;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StepActivity extends AppCompatActivity {

    @BindView(R.id.step_desc_text_view)
    TextView mStepDescTextView;
    @BindView(R.id.previous_button)
    Button mPreviousButton;
    @BindView(R.id.next_button)
    Button mNextButton;
    private Step mCurrentStep;
    private String mPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step);

        // Bind views with Butterknife
        ButterKnife.bind(this);

        // Get the Step object
        mCurrentStep = getIntent().getParcelableExtra("Step");

        // Get the placement
        mPlace = getIntent().getStringExtra("Place");

        if (mPlace.equals("first")) {
            mPreviousButton.setVisibility(View.INVISIBLE);
        } else if (mPlace.equals("last")) {
            mNextButton.setVisibility(View.INVISIBLE);
        }

        mStepDescTextView.setText(mCurrentStep.getDescription());
    }

    // Create method to take the user to the next step
    // Intent takes application back to RecipeActivity where it will then automatically open a
    // new StepActivity for the following step
    public void goToNextStep(View view){
        setResult(101, createChangeStepsIntent());
        finish();
    }

    // Create method to take the user to the previous step
    // Intent takes application back to RecipeActivity where it will then automatically open a
    // new StepActivity for the previous step
    public void goToPreviousStep(View view){
        setResult(102, createChangeStepsIntent());
        finish();
    }

    private Intent createChangeStepsIntent() {
        Intent intent = new Intent(this, RecipeActivity.class);
        int id = mCurrentStep.getId();
        intent.putExtra("currentId", id);
        return intent;
    }
}