package com.example.android.bakingapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.android.bakingapp.Models.Step;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StepActivity extends AppCompatActivity {

    @BindView(R.id.step_desc_text_view)
    TextView mStepDescTextView;
    private Step mCurrentStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step);

        // Bind views with Butterknife
        ButterKnife.bind(this);

        // Get the Step object
        mCurrentStep = getIntent().getParcelableExtra("Step");

        mStepDescTextView.setText(mCurrentStep.getDescription());
    }

    public void goToNextStep(View view){
        Intent intent = new Intent(this, RecipeActivity.class);
        int id = mCurrentStep.getId();
        intent.putExtra("currentId", id);
        setResult(123, intent);
        finish();
    }


}