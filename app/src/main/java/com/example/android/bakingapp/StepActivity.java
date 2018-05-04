package com.example.android.bakingapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ConfigurationInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.bakingapp.Models.Step;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class StepActivity extends AppCompatActivity {

    // Views
    @BindView(R.id.step_desc_text_view)
    TextView mStepDescTextView;
    @BindView(R.id.previous_button)
    Button mPreviousButton;
    @BindView(R.id.next_button)
    Button mNextButton;
    @BindView(R.id.step_image_view)
    ImageView mStepImageView;
    @BindView(R.id.exo_player_view)
    SimpleExoPlayerView mPlayerView;

    // Member Variables
    private Step mCurrentStep;
    private String mPlace;
    private static MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;
    private SimpleExoPlayer mExoPlayer;

    // Keys
    public static final String INTENT_EXTRA_CURRENT_ID_KEY = "current-id";
    private static final String INSTANCE_STATE_EXO_POSITION_KEY = "exo-player-position-key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step);

        // Bind views with Butterknife
        ButterKnife.bind(this);

        // Get the Step object
        mCurrentStep = getIntent().getParcelableExtra(RecipeActivity.INTENT_EXTRA_STEP_KEY);

        // Set the title in the action bar
        setTitle(mCurrentStep.getShortDescription());

        // Get the placement
        mPlace = getIntent().getStringExtra(RecipeActivity.INTENT_EXTRA_PLACE_KEY);

        if (mPlace.equals(RecipeActivity.PLACE_ID_FIRST)) {
            mPreviousButton.setVisibility(View.INVISIBLE);
        } else if (mPlace.equals(RecipeActivity.PLACE_ID_LAST)) {
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

            // If the mobile is in landscape mode, only show the video
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                RelativeLayout relativeLayout = findViewById(R.id.step_activity_relative_layout);
                relativeLayout.setVisibility(View.GONE);
            }
        } else {
            mPlayerView.setVisibility(View.GONE);
            // If there is no video, then set the thumbnail image
            String thumbnailUrlString = mCurrentStep.getThumbnailURL();
            if (!thumbnailUrlString.equals("")) {
                try {
                    Picasso.with(this)
                            .load(thumbnailUrlString)
                            .into(mStepImageView);
                    mStepImageView.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
        intent.putExtra(INTENT_EXTRA_CURRENT_ID_KEY, id);
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

    // Release exoplayer (call this when the activity is destroyed)
    private void releasePlayer() {
        if (mExoPlayer != null) {
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    // Method for initializing the ExoPlayer.
    private void initializePlayer(Uri mediaUri) {
        if (mExoPlayer == null) {
            // Create an instance of the ExoPlayer.
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
            mPlayerView.setPlayer(mExoPlayer);

            // Prepare the MediaSource.
            String userAgent = Util.getUserAgent(this, getString(R.string.baking_app));
            MediaSource mediaSource = new ExtractorMediaSource(mediaUri, new DefaultDataSourceFactory(
                    this, userAgent), new DefaultExtractorsFactory(), null, null);
            mExoPlayer.prepare(mediaSource);

            // Don't start playing video immediately
            mExoPlayer.setPlayWhenReady(false);
        }
    }

    // Pause the video if the activity loses focus
    @Override
    protected void onPause() {
        super.onPause();
        if (mExoPlayer != null) {
            // pause the video if the app loses focus
            mExoPlayer.setPlayWhenReady(false);
        }
    }


    // After rotation, have the video position set back to where it was before.
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            if (mExoPlayer != null) {
                long position = savedInstanceState.getLong(INSTANCE_STATE_EXO_POSITION_KEY);
                mExoPlayer.seekTo(position);
            }
        }
    }

    // Save the position of the video on rotate or loss of focus
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mExoPlayer != null) {
            long position = mExoPlayer.getCurrentPosition();
            outState.putLong(INSTANCE_STATE_EXO_POSITION_KEY, position);
        }
    }
}