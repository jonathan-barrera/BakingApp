package com.example.android.bakingapp;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.bakingapp.Models.Step;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

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
    private static MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;
    private SimpleExoPlayer mExoPlayer;
    private SimpleExoPlayerView mPlayerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step);

        // Bind views with Butterknife
        ButterKnife.bind(this);

        // Initialize the player view.
        mPlayerView = findViewById(R.id.exo_player_view);

        // Get the Step object
        mCurrentStep = getIntent().getParcelableExtra("Step");

        // Set the title in the action bar
        setTitle(mCurrentStep.getShortDescription());

        // Get the placement
        mPlace = getIntent().getStringExtra("Place");

        if (mPlace.equals("first")) {
            mPreviousButton.setVisibility(View.INVISIBLE);
        } else if (mPlace.equals("last")) {
            mNextButton.setVisibility(View.INVISIBLE);
        }

        // Set the description (extract data from Step object)
        mStepDescTextView.setText(mCurrentStep.getDescription());

        // Extract Videolink from the CurrentStep object
        String videoUrlString = mCurrentStep.getVideoURL();

        if (videoUrlString != null && !videoUrlString.equals("")) {
            // Initialize the Media Session.
            initializeMediaSession();

            // Initialize the player.
            initializePlayer(Uri.parse(videoUrlString));
        } else {
            mPlayerView.setVisibility(View.GONE);
        }

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

    private void initializeMediaSession() {

        // Create a MediaSessionCompat.
        mMediaSession = new MediaSessionCompat(this, MainActivity.class.getSimpleName());

        // Enable callbacks from MediaButtons and TransportControls.
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Do not let MediaButtons restart the player when the app is not visible.
        mMediaSession.setMediaButtonReceiver(null);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player.
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);

        mMediaSession.setPlaybackState(mStateBuilder.build());


        // MySessionCallback has methods that handle callbacks from a media controller.
        mMediaSession.setCallback(new MySessionCallback());

        // Start the Media Session since the activity is active.
        mMediaSession.setActive(true);
    }

    private class MySessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            mExoPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            mExoPlayer.setPlayWhenReady(false);
        }

        @Override
        public void onSkipToPrevious() {
            mExoPlayer.seekTo(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        if (mMediaSession != null) mMediaSession.setActive(false);
    }

    /**
     * Release ExoPlayer.
     */
    private void releasePlayer() {
        if (mExoPlayer != null) {
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    private void initializePlayer(Uri mediaUri) {
        if (mExoPlayer == null) {
            // Create an instance of the ExoPlayer.
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
            mPlayerView.setPlayer(mExoPlayer);

            // Set the ExoPlayer.EventListener to this activity.
            //mExoPlayer.addListener(this);

            // Prepare the MediaSource.
            String userAgent = Util.getUserAgent(this, "BakingApp");
            MediaSource mediaSource = new ExtractorMediaSource(mediaUri, new DefaultDataSourceFactory(
                    this, userAgent), new DefaultExtractorsFactory(), null, null);
            mExoPlayer.prepare(mediaSource);
            mExoPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mExoPlayer != null) {
            // pause the video if the app loses focus
            mExoPlayer.setPlayWhenReady(false);
        }
    }

    // After rotation, continue playing video
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        long position = savedInstanceState.getLong("ExoPlayerPosition");
        mExoPlayer.seekTo(position);
        mExoPlayer.setPlayWhenReady(true);
    }

    // Save the position of the video on rotate
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        long position = mExoPlayer.getCurrentPosition();
        outState.putLong("ExoPlayerPosition", position);
    }
}