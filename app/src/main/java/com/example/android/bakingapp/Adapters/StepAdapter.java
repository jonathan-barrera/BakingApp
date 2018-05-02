package com.example.android.bakingapp.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.bakingapp.Models.Step;
import com.example.android.bakingapp.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StepAdapter extends RecyclerView.Adapter<StepAdapter.StepAdapterViewHolder> {

    // List of Step objects to return
    private List<Step> mStepData;
    private StepAdapter.StepAdapterOnClickHandler mClickHandler;

    // An interface for handling onclick events
    public interface StepAdapterOnClickHandler {
        void onClick(Step step);
    }

    // Create a constructor to to instantiate the clickhandler
    public StepAdapter(StepAdapter.StepAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    @Override
    public StepAdapter.StepAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the list item view
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.step_list_item, parent, false);

        return new StepAdapter.StepAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StepAdapter.StepAdapterViewHolder holder, int position) {
        // For each Step object in the List<Step> get the name and bind it to the text view
        Step currentStep = mStepData.get(position);
        String shortDescription = currentStep.getShortDescription();
        int stepNumber = currentStep.getId();
        String stepString;
        if (stepNumber == 0) {
            stepString = shortDescription;
        } else {
            stepString = stepNumber + ". " + shortDescription;
        }
        holder.mStepNameTextView.setText(stepString);
    }

    @Override
    public int getItemCount() {
        if (mStepData == null) {
            return 0;
        }
        else {
            return mStepData.size();
        }
    }


    public class StepAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        @BindView(R.id.step_short_desc_text_view)
        TextView mStepNameTextView;

        public StepAdapterViewHolder(View itemView) {
            super(itemView);
            // Bind views with Butterknife
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Step step = mStepData.get(position);
            mClickHandler.onClick(step);
        }
    }

    public void setStepData(List<Step> stepList) {
        // Set the stepList to mStepData
        mStepData = stepList;
        notifyDataSetChanged();
    }
}
