package com.mogi.exoplayer2example;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;



/*


Description:
This Example app was created to show a simple example of ExoPlayer Version 2.8.4.
There is an option to play mp4 files or live stream content.
Exoplayer provides options to play many different formats, so the code can easily be tweaked to play the requested format.
Scroll down to "ADJUST HERE:" I & II to change between sources.
Keep in mind that m3u8 files might be stale and you would need new sources.
 */

public class MainActivity extends AppCompatActivity implements VideoRendererEventListener {


    private static final String TAG = "MainActivity";
    private PlayerView simpleExoPlayerView;
    private SimpleExoPlayer player;
    private TextView resolutionTextView;
    private Button btnStream;

    private String streamUrl = "https://mogi-live-stream.b-cdn.net/mogi.m3u8";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resolutionTextView = new TextView(this);
        resolutionTextView = (TextView) findViewById(R.id.resolution_textView);

        btnStream = (Button) findViewById(R.id.stream);



        init();
    }

    private  void  play(){
        // I. ADJUST HERE:
        final Uri mp4VideoUri =Uri.parse(streamUrl); //ABC NEWS

        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(); //test

        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create the player
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        simpleExoPlayerView = new SimpleExoPlayerView(this);
        simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.player_view);
        simpleExoPlayerView.setControllerAutoShow(false);

        int h = simpleExoPlayerView.getResources().getConfiguration().screenHeightDp;
        int w = simpleExoPlayerView.getResources().getConfiguration().screenWidthDp;
        //Log.v(TAG, "height : " + h + " weight: " + w);
        ////Set media controller
        simpleExoPlayerView.setUseController(true);//set to true or false to see controllers
        simpleExoPlayerView.requestFocus();
        // Bind the player to the view.
        simpleExoPlayerView.setPlayer(player);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "exoplayer2example"), bandwidthMeter);
        MediaSource videoSource = new HlsMediaSource(mp4VideoUri, dataSourceFactory, 1, null, null);
        final LoopingMediaSource loopingSource = new LoopingMediaSource(videoSource);
        player.prepare(videoSource);

        //Creating instance of Video Analytics Class
        VideoAnalytics va = new VideoAnalytics("5de51d6e4e8b541fb802d8f3","rahul99");

        //passing event listener to Exoplayer
        player.addListener(va.getListener(player,mp4VideoUri.toString(),"7 wonder", "7 beautiful wonders of earth","tag1,tag2,hindi,comedy"));
        player.setPlayWhenReady(true); //run file/link when ready to play.
        player.setVideoDebugListener(this);
    }

    private void init(){
        final EditText efu = (EditText) findViewById(R.id.url);
        efu.setText(streamUrl);

        btnStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                streamUrl = efu.getText().toString();
                play();
            }
        });
    }



    @Override
    public void onVideoEnabled(DecoderCounters counters) {

    }

    @Override
    public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {

    }

    @Override
    public void onVideoInputFormatChanged(Format format) {

    }

    @Override
    public void onDroppedFrames(int count, long elapsedMs) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        Log.v(TAG, "onVideoSizeChanged [" + " width: " + width + " height: " + height + "]");
        resolutionTextView.setText("RES:(WxH):" + width + "X" + height + "\n           " + height + "p");//shows video info
    }

    @Override
    public void onRenderedFirstFrame(Surface surface) {

    }

    @Override
    public void onVideoDisabled(DecoderCounters counters) {

    }
//-------------------------------------------------------ANDROID LIFECYCLE---------------------------------------------------------------------------------------------

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "onStop()...");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart()...");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume()...");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "onPause()...");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy()...");
        player.release();
    }
}
