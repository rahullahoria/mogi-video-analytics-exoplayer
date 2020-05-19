package com.mogi.exoplayer2example;

import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class VideoAnalytics {

    static JSONArray events = new JSONArray();
    static String appId = "";
    static String userId = "";
    private static final String TAG = "MogiVideoAnalytics";
    private static final String baseUrl = "https://tc.mogiapp.com/events/";
    static long lastUpdate = System.currentTimeMillis();

    static int level = 0;

    /*
    * {
              userId: window['bfp'],
              location: window["gpsLocation"] && window["gpsLocation"].coords ? [window["gpsLocation"].coords.longitude, window["gpsLocation"].coords.latitude] : [],
              url: url,
              title: title,
              des: des,
              events: [{
                type: e.type,
                position: mogiPlayer.currentTime,
                bufferedPercent: bfPer,
                connection: navigator.connection
              }]
            }
    * */

    public VideoAnalytics(String appId, String userId){
        this.appId = appId;
        this.userId = userId;
    }

    public ExoPlayer.DefaultEventListener getListener(final SimpleExoPlayer player, final String url, final String title, final String des, final String tags){
        return new ExoPlayer.DefaultEventListener() {


            int vaIndex = getIndex(url,title,des,tags);

            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
                if(level>0) Log.v(TAG, "Listener-onTimelineChanged... ");
                addEvent(vaIndex,"durationchange",player.getCurrentPosition(),player.getBufferedPercentage());
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                if(level>0) Log.v(TAG, "Listener-onTracksChanged... ");
                addEvent(vaIndex,"onTracksChanged",player.getCurrentPosition(),player.getBufferedPercentage());

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                if(level>0) Log.v(TAG, "Listener-onLoadingChanged... " + isLoading);
                addEvent(vaIndex,"onLoadingChanged",player.getCurrentPosition(),player.getBufferedPercentage());

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
               // if(level>0) Log.v(TAG, "Listener-onPlayerStateChanged..." + playbackState+"|||isDrawingCacheEnabled():"+simpleExoPlayerView.isDrawingCacheEnabled());

                if (playWhenReady && playbackState == ExoPlayer.STATE_READY) {
                    // media actually playing
                    addEvent(vaIndex,"play",player.getCurrentPosition(),player.getBufferedPercentage());

                } else if (playWhenReady) {
                    // might be idle (plays after prepare()),
                    // buffering (plays when data available)
                    // or ended (plays when seek away from end)
                    switch (playbackState) {
                        case ExoPlayer.STATE_BUFFERING:
                            addEvent(vaIndex,"buffering",player.getCurrentPosition(),player.getBufferedPercentage());

                        case ExoPlayer.STATE_ENDED:
                            addEvent(vaIndex,"ended",player.getCurrentPosition(),player.getBufferedPercentage());

                        case ExoPlayer.STATE_IDLE:
                            addEvent(vaIndex,"idle",player.getCurrentPosition(),player.getBufferedPercentage());

                        case ExoPlayer.STATE_READY:
                            addEvent(vaIndex,"ready",player.getCurrentPosition(),player.getBufferedPercentage());

                        default:
                            addEvent(vaIndex,"onPlayerStateChanged",player.getCurrentPosition(),player.getBufferedPercentage());

                    }
                } else {
                    // player paused in any state
                    addEvent(vaIndex,"pause",player.getCurrentPosition(),player.getBufferedPercentage());

                }



            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
                if(level>0) Log.v(TAG, "Listener-onRepeatModeChanged... " + repeatMode);
                addEvent(vaIndex,"onRepeatModeChanged",player.getCurrentPosition(),player.getBufferedPercentage());


            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
                if(level>0) Log.v(TAG, "Listener-onShuffleModeEnabledChanged... " + shuffleModeEnabled);
                addEvent(vaIndex,"onShuffleModeEnabledChanged",player.getCurrentPosition(),player.getBufferedPercentage());


            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                if(level>0) Log.v(TAG, "Listener-onPlayerError...");
                player.stop();
               // player.prepare(loopingSource);
                player.setPlayWhenReady(true);
                addEvent(vaIndex,"onPlayerError",player.getCurrentPosition(),player.getBufferedPercentage());

            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                if(level>0) Log.v(TAG, "Listener-onPositionDiscontinuity... " + reason);
                addEvent(vaIndex,"onPositionDiscontinuity",player.getCurrentPosition(),player.getBufferedPercentage());


            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                if(level>0) Log.v(TAG, "Listener-onPlaybackParametersChanged... "+playbackParameters.toString());
                addEvent(vaIndex,"onPlaybackParametersChanged",player.getCurrentPosition(),player.getBufferedPercentage());


            }

            @Override
            public void onSeekProcessed() {
                if(level>0) Log.v(TAG, "Listener-onSeekProcessed... ");
                addEvent(vaIndex,"seeking",player.getCurrentPosition(),player.getBufferedPercentage());


            }
        };
    }

    private int getIndexByUrl(String url)
    {
        for (int i = 0; i < events.length(); i++) {

            try{
                JSONObject item = events.getJSONObject(i);
                if (item.get("url") == url){
                    return i;
                }

            }
            catch(Exception e){

            }

        }

        return -1;
    }

    public int getIndex(String url, String title, String des, String tags)  {

        int index = getIndexByUrl(url);

        if(index >-1) return index;

        JSONObject jsonObj= new JSONObject();
        try {
            jsonObj.put("url", url);
            jsonObj.put("userId", userId);
            jsonObj.put("title", title);
            jsonObj.put("des", des);
            jsonObj.put("tags", tags);
            jsonObj.put("events",  new JSONArray());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        events.put(jsonObj);

        return events.length()-1;
    }

    private void checkPost(){

        if(System.currentTimeMillis() > lastUpdate + 10000 && events.length() > 0){
            lastUpdate = System.currentTimeMillis();
            if(level>0) Log.i("compress string before", events.toString());
            if(level>0) Log.i("compress string after", compressLZW(events.toString()));

            sendPost(compressLZW(events.toString()));

            for (int i = 0; i < events.length(); i++) {


                JSONObject item = null;
                try {
                    item = events.getJSONObject(i);
                    item.put("events",  new JSONArray());

                    events.put(i,item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }





            }

        }


    }

    public boolean addEvent ( int index, String type, long position, int bufferedPercent)  {


        try{

        JSONObject item = events.getJSONObject(index);

        JSONArray urlEvents = item.getJSONArray("events");

        JSONObject jsonObj= new JSONObject();
        jsonObj.put("type", type);
        jsonObj.put("position", position);
        jsonObj.put("bufferedPercent", bufferedPercent);

        urlEvents.put(jsonObj);

        item.put("events",urlEvents);

        events.put(index,item);
            if(level>0) Log.i("events" , events.toString());
        }
        catch(Exception e){

        }

        checkPost();

        return true;
    }

    public void sendPost(final String data) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(baseUrl+appId+"?source=android");
                    if(level>0) Log.i("post data", baseUrl+appId + "  "+data);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("data", data);
                    byte[] out = jsonParam.toString().getBytes();
                    int length = out.length;

                    conn.setFixedLengthStreamingMode(length);
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.connect();

                    OutputStream os = conn.getOutputStream();
                        os.write(out);

                    if(level>0) Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    if(level>0) Log.i("MSG" , conn.getResponseMessage());

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    public static String compressLZW(String data){
        //var dict = {};

        HashMap<String, Integer> dict = new HashMap<String, Integer>();
        //var data = (s + "").split("");
        //String[] out = new String[data.length()];;
        ArrayList<String> out = new ArrayList<String>();
        String currChar;
        String phrase = data.charAt(0) + "";
        int code = 256;
        for (int i = 1; i < data.length(); i++) {
            currChar = data.charAt(i) + "";
            if (dict.containsKey (phrase + currChar)) {
                phrase += currChar;
            } else {
                out.add(phrase.length() > 1 ? dict.get(phrase) + "": (int) phrase. charAt(0)+"");
                dict.put(phrase + currChar, code);
                code++;
                phrase = currChar;
            }
        }
        out.add(phrase.length() > 1 ? dict.get(phrase) + "": (int) phrase. charAt(0)+"");
        String strOut = "";
        for (int i = 0; i < out.size(); i++) {
           // out.add(i, ((char) Integer.parseInt(out.get(i)))+"") ;
            strOut +=((char) Integer.parseInt(out.get(i)))+"";
        }
        return strOut.replaceAll("\"","\'");
    }

}
