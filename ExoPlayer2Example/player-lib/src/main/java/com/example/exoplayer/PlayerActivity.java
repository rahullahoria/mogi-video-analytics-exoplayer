/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
* limitations under the License.
 */
package com.example.exoplayer;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;

import java.text.NumberFormat;

import static java.lang.Math.abs;


/**
 * Fullscreen activity to play audio / video streams.
 */
public class PlayerActivity extends AppCompatActivity {

    private PlayerView playerView;
    private SimpleExoPlayer player;
    private MediaSource mediaSource;

    private static final NumberFormat PERCENT_FORMAT;
    static {
        PERCENT_FORMAT = NumberFormat.getInstance();
        PERCENT_FORMAT.setMinimumFractionDigits(2);
        PERCENT_FORMAT.setMaximumFractionDigits(2);
    }

    private long playbackPosition;
    private int currentWindow;
    private long TimeMs = 1000;
    private boolean playWhenReady = true;

    private ComponentListener componentListener;
    private EventLogger eventLogger;
    private static final String TAG = "Track Events";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        componentListener = new ComponentListener();
        playerView = findViewById(R.id.video_view);
    }


    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource.Factory(new DefaultHttpDataSourceFactory("mobile-dev-test")).createMediaSource(uri);
    }


    private void initializePlayer() {
        if (player == null) {
            DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this);
            DefaultTrackSelector trackSelector = new DefaultTrackSelector();
            player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, new DefaultLoadControl());
            player.addListener(componentListener);
            eventLogger = new EventLogger(trackSelector);
            player.addAnalyticsListener(eventLogger);
            playerView.setPlayer(player);
            player.setPlayWhenReady(playWhenReady);
            player.seekTo(currentWindow, playbackPosition);

            Uri uri = Uri.parse(getString(R.string.media_url_mp4));
            MediaSource mediaSource = buildMediaSource(uri);
            player.prepare(mediaSource, true, false);
        }
    }


    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.removeListener(componentListener);
            player.release();
            player = null;
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }


    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }


    private class ComponentListener extends Player.DefaultEventListener {
        Boolean firstStart = true;
        long totalDuration;
        long pauses = 0;
        long resumes = 0;
        float completion;

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            String stateString;

            long durationMs = player.getDuration();
            totalDuration = durationMs /TimeMs;

            playbackPosition = player.getCurrentPosition();
            
            if (playbackPosition != 0) {
                playbackPosition = playbackPosition / TimeMs;
            }

            completion = abs(100 * (float)playbackPosition / (float)totalDuration);

            switch (playbackState) {
                case Player.STATE_IDLE:
                    stateString = "State - Idle  ";
                    break;
                case Player.STATE_BUFFERING:
                    stateString = "State - Buffer";
                    break;
                case Player.STATE_READY:

                    if (firstStart && playWhenReady) {
                        pauses = 0;
                        resumes = 0;
                        stateString = "State - Start ";
                        firstStart = false;
                    } else if (playWhenReady){
                        stateString = "State - Resume";
                        resumes++;
                    } else {
                        stateString = "State - Pause ";
                        pauses++;
                    }
                    break;
                case Player.STATE_ENDED:
                    stateString = "State - End   ";
                    firstStart = true;
                    break;
                default:
                    stateString = "State - Unknown";
                    break;
            }
          Log.d(TAG, "Changed state to " + stateString + "   playhead: " + playbackPosition + " s" + "\n" + " [total duration " + totalDuration + " s" + "]" + " - completion = " + PERCENT_FORMAT.format(completion) + " %" + "\n" + " number of pauses: " + pauses + " - number of resumes: " + resumes);
      }
    }
}