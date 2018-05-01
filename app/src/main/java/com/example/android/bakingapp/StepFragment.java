package com.example.android.bakingapp;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.bakingapp.Models.Step;

import org.w3c.dom.Text;

public class StepFragment extends Fragment {

    public StepFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout
        View rootView = inflater.inflate(R.layout.fragment_step, container);

        // Get a reference to the Step Description textview
        TextView stepDescription = rootView.findViewById(R.id.step_desc_text_view);

        // Get the Step object
        Step currentStep = getActivity().getIntent().getParcelableExtra("Step");

        stepDescription.setText(currentStep.getDescription());

        return rootView;
    }
}
