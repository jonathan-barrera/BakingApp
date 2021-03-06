package com.example.android.bakingapp;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import timber.log.Timber;

public class StepFragment extends Fragment {

    // Member Variables
    private Step mCurrentStep;
    private static MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;
    private long mVideoPosition;
    private boolean mIsVideoPlaying;
    private String mVideoUrlString;

    // Views
    private SimpleExoPlayer mExoPlayer;
    private SimpleExoPlayerView mPlayerView;
    private ImageView mStepImageView;

    public StepFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout
        View rootView = inflater.inflate(R.layout.fragment_step, container, false);

        // Get a reference to the Step Description textview, Video exoplayer, and step image view
        TextView stepDescription = rootView.findViewById(R.id.step_frag_desc_text_view);
        mPlayerView = rootView.findViewById(R.id.exo_player_view_frag);
        mStepImageView = rootView.findViewById(R.id.step_image_view_frag);

        // Get the Video information from the Step object and set to ExoPlayer
        if (mCurrentStep != null) {
            mVideoUrlString = mCurrentStep.getVideoURL();
            if (!TextUtils.isEmpty(mVideoUrlString)) {
                // Initialize the Media Session.
                initializeMediaSession();

                // Initialize the player.
                initializePlayer(Uri.parse(mVideoUrlString));
            } else {
                mPlayerView.setVisibility(View.GONE);
                // If there is no video, then set the thumbnail image
                String thumbnailUrlString = mCurrentStep.getThumbnailURL();
                if (!thumbnailUrlString.equals("")) {
                    try {
                        Picasso.with(getContext())
                                .load(thumbnailUrlString)
                                .into(mStepImageView);
                        mStepImageView.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // Set the description to the text view
            stepDescription.setText(mCurrentStep.getDescription());
        }
            return rootView;
    }

    public void setStepInfo(Step step) {
        mCurrentStep = step;
    }

    public void setVideoPosition(long videoPosition, boolean isPlaying) {
        mVideoPosition = videoPosition;
        mIsVideoPlaying = isPlaying;
    }

    private void initializeMediaSession() {
        // Create a MediaSessionCompat.
        mMediaSession = new MediaSessionCompat(getContext(), MainActivity.class.getSimpleName());

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
        mMediaSession.setCallback(new StepFragment.MySessionCallback());

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
    public void onDestroyView() {
        super.onDestroyView();

        releasePlayer();
        if (mMediaSession != null) mMediaSession.setActive(false);
    }

    // Release exo player (call this when fragment is destroyed)
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
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector, loadControl);
            mPlayerView.setPlayer(mExoPlayer);

            // Prepare the MediaSource.
            String userAgent = Util.getUserAgent(getContext(), getString(R.string.baking_app));
            MediaSource mediaSource = new ExtractorMediaSource(mediaUri, new DefaultDataSourceFactory(
                    getContext(), userAgent), new DefaultExtractorsFactory(), null, null);
            mExoPlayer.prepare(mediaSource);

            // Set to correct position
            mExoPlayer.seekTo(mVideoPosition);

            // Play if it was playing before
            mExoPlayer.setPlayWhenReady(mIsVideoPlaying);

            // Put an event listener so that you can log when the exoplayer play/pause buttons
            // have been pressed.
            mExoPlayer.addListener(new ExoPlayer.EventListener() {
                @Override
                public void onTimelineChanged(Timeline timeline, Object manifest) {

                }

                @Override
                public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

                }

                @Override
                public void onLoadingChanged(boolean isLoading) {

                }

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    if (playWhenReady) {
                        mIsVideoPlaying = true;
                    } else {
                        mIsVideoPlaying = false;
                    }
                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {

                }

                @Override
                public void onPositionDiscontinuity() {

                }
            });
        }
    }

    // Pause the video upon losing focus
    @Override
    public void onPause() {
        super.onPause();

        // Save the video position and playing status
        if (mExoPlayer != null) {
            if (mIsVideoPlaying) {
                mExoPlayer.setPlayWhenReady(false);
                mIsVideoPlaying = true;
                mVideoPosition = mExoPlayer.getCurrentPosition();
                ((RecipeActivity) getActivity()).setFragVideoPosition(mVideoPosition, mIsVideoPlaying);
            }
        }
    }


    // Resume playing the video when getting focus back
    @Override
    public void onResume() {
        super.onResume();

        if (!TextUtils.isEmpty(mVideoUrlString)) {
            initializePlayer(Uri.parse(mVideoUrlString));
        }

        if (mMediaSession != null) mMediaSession.setActive(true);
        else initializeMediaSession();

        if (mExoPlayer != null) {
            mExoPlayer.setPlayWhenReady(mIsVideoPlaying);

            if (mVideoPosition != 0) {
                mExoPlayer.seekTo(mVideoPosition);
            }
        }
    }
}
