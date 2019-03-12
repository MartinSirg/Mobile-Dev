package com.itcollege.radio2019;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.itcollege.radio2019.Domain.Station;
import com.itcollege.radio2019.Fragments.StationsDialog;
import com.itcollege.radio2019.Repositories.StationRepository;

import java.io.Serializable;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        SeekBar.OnSeekBarChangeListener,
        StationsDialog.StationDialogListener {
    static private final String TAG = MainActivity.class.getSimpleName();
    private BroadcastReceiver mBroadcastReceiver;

    private int mMusicPlayerStatus = C.MUSICSERVICE_STOPPED;
    private boolean mMusicServiceStarted = false;
    private Station mSelectedStation;

    private TextView mTextViewArtist;
    private TextView mTextViewTitle;
    private TextView mTextViewStation;
    private TextView mTextViewVolume;
    private SeekBar mSeekBarAudioVolume;

    private SettingsContentObserver mSettingsContentObserver;

    private String mArtist;
    private String mTrackTitle;

    private List<Station> mStations;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextViewArtist = (TextView) findViewById(R.id.textViewArtist);
        mTextViewTitle = (TextView) findViewById(R.id.textViewTitle);
        mSeekBarAudioVolume = (SeekBar) findViewById(R.id.seekBarAudioVolume);
        mTextViewStation = (TextView) findViewById(R.id.textViewRadioStation);
        mTextViewVolume = findViewById(R.id.textViewVolume);
        mFab = (FloatingActionButton) findViewById(R.id.fab);


        // set the seek bar maximum - based on audiomanager reported max
        AudioManager audio = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mSeekBarAudioVolume.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        // and the position
        mSeekBarAudioVolume.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));
        mTextViewVolume.setText(Integer.toString(mSeekBarAudioVolume.getProgress()));


        mSeekBarAudioVolume.setOnSeekBarChangeListener(this);

        // Register intents we intend to receive
        // this is explicit - ie we can receive only those we specify. this is mandatory.
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(C.MUSICSERVICE_INTENT_BUFFERING);
        intentFilter.addAction(C.MUSICSERVICE_INTENT_PLAYING);
        intentFilter.addAction(C.MUSICSERVICE_INTENT_STOPPED);
        intentFilter.addAction(C.MUSICSERVICE_INTENT_SONGINFO);

        //Broadcast receiver

        mBroadcastReceiver = new MainActivityBroadcastReceiver();

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mBroadcastReceiver, intentFilter);

        //
        StationRepository artistRepository = new StationRepository(this);
        artistRepository.open();
        mStations = artistRepository.getAllOrInitialize();
        Log.d(TAG, "Stations: " + mStations.size());
        artistRepository.close();

        mSelectedStation = mStations.get(0);
        mTextViewStation.setText(mSelectedStation.getName());

        Intent songInfoRequestIntent = new Intent(C.ACTIVITY_INTENT_SEND_DATA);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(songInfoRequestIntent);

    }


    // ============================== LIFECYCLE EVENTS ===============================
    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();


        mSettingsContentObserver = new SettingsContentObserver(new Handler());
        this.getApplicationContext().getContentResolver().registerContentObserver(
                android.provider.Settings.System.CONTENT_URI, true,
                mSettingsContentObserver);

    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }


    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mBroadcastReceiver);
    }


    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart");
        super.onRestart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: stationID = " + mSelectedStation.getStationId());
        outState.putInt(C.SAVE_STATE_MEDIA_PLAYER_STATUS, mMusicPlayerStatus);
        outState.putInt(C.SAVE_STATE_SELECTED_STATION, mSelectedStation.getStationId());
        outState.putString(C.SAVE_STATE_CURRENT_ARTIST, mArtist);
        outState.putString(C.SAVE_STATE_CURRENT_SONG, mTrackTitle);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState");

        mSelectedStation = mStations.stream()
                .filter(station -> station.getStationId() == savedInstanceState.getInt(C.SAVE_STATE_SELECTED_STATION)).findFirst().get();
        mArtist = savedInstanceState.getString(C.SAVE_STATE_CURRENT_ARTIST);
        mTrackTitle = savedInstanceState.getString(C.SAVE_STATE_CURRENT_SONG);
        mMusicPlayerStatus = savedInstanceState.getInt(C.SAVE_STATE_MEDIA_PLAYER_STATUS);
        mMusicServiceStarted = mMusicPlayerStatus != C.MUSICSERVICE_STOPPED;
        updateUI();
        super.onRestoreInstanceState(savedInstanceState);
    }

    // ============================== OTHER ===============================

    public void buttonStatisticsOnClick(View view) {
        Log.d(TAG, "buttonStatisticsOnClick: ");
        Intent intent = new Intent(this, StatisticsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(C.SERIALIZABLE_STATIONS, (Serializable) mStations);
        intent.putExtra(C.SERIALIZABLE_STATIONS_BUNDLE, bundle);
        this.startActivity(intent);
    }

    public void buttonControlMusicOnClick(View view) {
        Log.d(TAG, "buttonControlMusicOnClick");

        if (!mMusicServiceStarted) {
            Intent intentStartService = new Intent(this, MusicService.class);
            intentStartService.putExtra(C.SERVICE_STATION_ID_KEY, mSelectedStation.getStationId());
            intentStartService.putExtra(C.SERVICE_STATION_NAME_KEY, mSelectedStation.getName());
            intentStartService.putExtra(C.SERVICE_STATION_STREAM_URL_KEY, mSelectedStation.getStreamUrl());
            intentStartService.putExtra(C.SERVICE_STATION_SONGS_API_URL_KEY, mSelectedStation.getSongNameApiUrl());

            this.startService(intentStartService);
            mMusicServiceStarted = true;
        } else {
            Intent intentStopService = new Intent(this, MusicService.class);
            this.stopService(intentStopService);
            mMusicServiceStarted = false;
        }
    }

    public void buttonSelectStationOnClick(View view) {
        StationsDialog dialog = new StationsDialog();

        Bundle bundle = new Bundle();
        bundle.putSerializable(C.SERIALIZABLE_STATIONS, (Serializable) mStations);
        dialog.setArguments(bundle);

        dialog.show(getSupportFragmentManager(), "DIALOG TAG");
    }

    public void buttonVolumeUpOnClick(View view) {
        mSeekBarAudioVolume.setProgress(mSeekBarAudioVolume.getProgress() + 1);
        mTextViewVolume.setText(Integer.toString(mSeekBarAudioVolume.getProgress()));    }

    public void buttonVolumeDownOnClick(View view) {
        mSeekBarAudioVolume.setProgress(mSeekBarAudioVolume.getProgress() - 1);
        mTextViewVolume.setText(Integer.toString(mSeekBarAudioVolume.getProgress()));    }

    public void updateUI() {
        Log.d(TAG, "updateUI: ");
        switch (mMusicPlayerStatus) {
            case C.MUSICSERVICE_STOPPED:
                mFab.setImageResource(android.R.drawable.ic_media_play);
                mFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.color_green)));
                mArtist = "";
                mTrackTitle = "";

                break;
            case C.MUSICSERVICE_BUFFERING:
                mFab.setImageResource(android.R.drawable.stat_notify_sync);
                mFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.color_orange)));
                break;
            case C.MUSICSERVICE_PLAYING:
                mFab.setImageResource(android.R.drawable.ic_media_pause);
                mFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.color_red)));
                break;
        }
        mTextViewArtist.setText(mArtist);
        mTextViewTitle.setText(mTrackTitle);
        mTextViewStation.setText(mSelectedStation.getName());
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        AudioManager audio = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_SHOW_UI);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void changeCurrentStation(int stationIndex) {
        //Doesn't restart service if station is same
        if (mSelectedStation == mStations.get(stationIndex)) return;
        mSelectedStation = mStations.get(stationIndex);
        if (mMusicServiceStarted)
            buttonControlMusicOnClick(null); // Stops music service if its running
        buttonControlMusicOnClick(null); // Starts music service
        mTextViewStation.setText(mSelectedStation.getName());
    }

    public class MainActivityBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive " + intent.getAction());
            switch (intent.getAction()) {
                case C.MUSICSERVICE_INTENT_BUFFERING:
                    mMusicPlayerStatus = C.MUSICSERVICE_BUFFERING;
                    break;
                case C.MUSICSERVICE_INTENT_PLAYING:
                    mMusicPlayerStatus = C.MUSICSERVICE_PLAYING;
                    break;
                case C.MUSICSERVICE_INTENT_STOPPED:
                    mMusicPlayerStatus = C.MUSICSERVICE_STOPPED;
                    break;
                case C.MUSICSERVICE_INTENT_SONGINFO:
                    mMusicServiceStarted = true;
                    mMusicPlayerStatus = C.MUSICSERVICE_PLAYING;
                    mSelectedStation = mStations.stream().filter(station -> station.getStationId() == intent.getIntExtra(C.MUSICSERVICE_STATION, 1)).findFirst().get();
                    mArtist = intent.getStringExtra(C.MUSICSERVICE_ARTIST);
                    mTrackTitle = intent.getStringExtra(C.MUSICSERVICE_TRACKTITLE);
                    break;
            }

            updateUI();
        }
    }


    public class SettingsContentObserver extends ContentObserver {
        private int previousVolume = -1;

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public SettingsContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            Log.d(TAG, "SettingsContentObserver.onChange selfChange" + selfChange);
            super.onChange(selfChange);


            AudioManager audio = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

            Log.d(TAG, "Volume: " + Integer.toString(currentVolume));

            if (currentVolume != previousVolume) {
                previousVolume = currentVolume;
                mSeekBarAudioVolume.setProgress(currentVolume);
                mTextViewVolume.setText(Integer.toString(mSeekBarAudioVolume.getProgress()));            }

        }


    }


}
