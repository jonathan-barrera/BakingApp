package com.example.android.bakingapp;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.bakingapp.Models.Step;

public class StepFragment extends Fragment {

    Step mStep;

    public StepFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout
        View rootView = inflater.inflate(R.layout.fragment_step, container, false);

        //ButterKnife.bind(getActivity());
        // Get a reference to the Step Description textview
        TextView stepDescription = rootView.findViewById(R.id.step_frag_desc_text_view);

        // Get the Step object
        if (getActivity().getIntent().hasExtra("Step")) {
            mStep = getActivity().getIntent().getParcelableExtra("Step");
        }

        stepDescription.setText(mStep.getDescription());

        return rootView;
    }

    public void setStepInfo(Step step) {
        mStep = step;
    }
}
