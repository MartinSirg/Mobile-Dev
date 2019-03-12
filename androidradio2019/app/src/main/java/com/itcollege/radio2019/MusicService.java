package com.itcollege.radio2019;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.Time;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.itcollege.radio2019.Domain.Artist;
import com.itcollege.radio2019.Domain.SongPlayed;
import com.itcollege.radio2019.Domain.Station;
import com.itcollege.radio2019.Repositories.ArtistRepository;
import com.itcollege.radio2019.Repositories.SongPlayedRepostory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {
    static private final String TAG = MusicService.class.getSimpleName();

    // on android 9 add this to manifest
    // <application android:usesCleartextTraffic="true"


    // not the best option in real life, problemas with buffers and formats and ...
    // use exoplayer or something else
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private Station mCurrentStation;
    private ScheduledExecutorService mScheduledExecutorService;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.reset();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (intent == null) {
            Log.e(TAG, "Intent was null!!!");
            return Service.START_NOT_STICKY;
        }

        mBroadcastReceiver = new MusicServiceBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(C.ACTIVITY_INTENT_STOPPMUSIC);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mBroadcastReceiver, intentFilter);


        mMediaPlayer.reset();

        mCurrentStation = new Station(
                intent.getExtras().getInt(C.SERVICE_STATION_ID_KEY),
                intent.getExtras().getString(C.SERVICE_STATION_NAME_KEY),
                intent.getExtras().getString(C.SERVICE_STATION_STREAM_URL_KEY),
                intent.getExtras().getString(C.SERVICE_STATION_SONGS_API_URL_KEY)
        );

        try {
            mMediaPlayer.setDataSource(mCurrentStation.getStreamUrl());

            // Stop all ongoing Web requests
            WebApiSingletonServiceHandler.getInstance(getApplicationContext()).cancelRequestQueue(C.MUSICSERVICE_VOLLEYTAG);

            mMediaPlayer.prepareAsync();

            // Inform main activity that we are buffering
            Intent intentInformActivity = new Intent(C.MUSICSERVICE_INTENT_BUFFERING);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentInformActivity);

        } catch (IOException e) {
            e.printStackTrace();
        }


        return Service.START_REDELIVER_INTENT;
    }

    //This is called when stopService() is called
    @Override
    public void onDestroy() {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        if (mScheduledExecutorService != null) mScheduledExecutorService.shutdown();
        WebApiSingletonServiceHandler.getInstance(getApplicationContext()).cancelRequestQueue(C.MUSICSERVICE_VOLLEYTAG);
        Intent intentInformActivity = new Intent(C.MUSICSERVICE_INTENT_STOPPED);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentInformActivity);
    }

    // ====================== Typically not used =====================
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    // ====================== MediaPLayer Lifecycle events =====================
    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onCompletion");

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(TAG, "onError");
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "onPrepared");
        mMediaPlayer.start();


        // Inform main activity, that we are playing
        Intent intentInformActivity = new Intent(C.MUSICSERVICE_INTENT_PLAYING);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentInformActivity);


        StartMediaInfoGathering();

    }


    private void StartMediaInfoGathering() {
        mScheduledExecutorService = Executors.newScheduledThreadPool(5);
        mScheduledExecutorService.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        new GetSongInfo().execute();
                    }
                }, 0, 15, TimeUnit.SECONDS
        );
    }

    private class GetSongInfo extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            String songInfoUrl = mCurrentStation.getSongNameApiUrl();
            StringRequest stringRequest = new StringRequest(
                    Request.Method.GET,
                    songInfoUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, "onResponse " + response);

                            // Parse the json response
                            // {"StationName":"SP","SongHistoryList":[{"Count":0,"Artist":"Sky Plus","Title":"","IsSkippable":false,"TimeStamp":1550214212,"SqlTimeStamp":"2019-02-15T09:03:32","Album":""},{"Count":1,"Artist":"Ava Max","Title":"Sweet but Psycho","IsSkippable":true,"TimeStamp":1550214031,"SqlTimeStamp":"2019-02-15T09:00:31","Album":"https://sky.ee/laulusonad/laulusonad-ava-max-sweet-but-psych"}]}

                            try {
                                JSONObject jsonObjectStationInfo = new JSONObject(response);
                                JSONArray jsonArraySongHistory = jsonObjectStationInfo.getJSONArray("SongHistoryList");
                                JSONObject jsonSongInfo = jsonArraySongHistory.getJSONObject(0);

                                String artistName = jsonSongInfo.getString("Artist");
                                String trackTitle = jsonSongInfo.getString("Title");
                                int timePlayed = jsonSongInfo.getInt("TimeStamp");

                                updateDatabase(artistName, trackTitle, timePlayed);

                                // broadcast the song info
                                Intent sendSongInfoIntent = new Intent(C.MUSICSERVICE_INTENT_SONGINFO);
                                sendSongInfoIntent.putExtra(C.MUSICSERVICE_ARTIST, artistName);
                                sendSongInfoIntent.putExtra(C.MUSICSERVICE_TRACKTITLE, trackTitle);
                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(sendSongInfoIntent);


                            } catch (JSONException e) {
                                Log.e(TAG, e.getMessage());
                            }


                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, "onErrorResponse " + error.getMessage());
                        }
                    }
            );

            stringRequest.addMarker(C.MUSICSERVICE_VOLLEYTAG);
            // add the request to Volley queue
            WebApiSingletonServiceHandler.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

            return null;
        }
    }

    private void updateDatabase(String artistName, String trackTitle, int timePlayed) {
        ArtistRepository artistRepo = new ArtistRepository(this);
        SongPlayedRepostory songsRepo = new SongPlayedRepostory(this);

        artistRepo.open();
        songsRepo.open();

        int artistId = artistRepo.getArtistIdOrInsert(new Artist(artistName, mCurrentStation.getStationId()));
        SongPlayed song = new SongPlayed(trackTitle, artistId, timePlayed);
        songsRepo.add(song);

        Log.d(TAG, "REMOVE ME LATER! Songs database currently holds : " + songsRepo.getAll().size() + " songs.");
        Log.d(TAG, "REMOVE ME LATER! Artists database currently holds : " + artistRepo.getAll().size() + " artists");

        Calendar c =  Calendar.getInstance();
//        c.setTimeInMillis((long) song.getTimePlayedAt() * 1000);
//        Log.d(TAG, "updateDatabase: Song played at " +  DateFormat.getDateTimeInstance().format(c.getTime()));

//        for (SongPlayed songPlayed : songsRepo.getAll()) {
//            Date date = new Date((long) songPlayed.getTimePlayedAt() * 1000);
//            Log.d(TAG, "Song played at: " +  new SimpleDateFormat("HH:mm:ss YYYY-MM-dd").format(date));
//            // 1552039861
//        }

        artistRepo.close();
        songsRepo.close();
    }

    public class MusicServiceBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: STOP SERVICE");
            switch (intent.getAction()) {
                case C.ACTIVITY_INTENT_STOPPMUSIC:
                    stopSelf();
            }
        }
    }
}
