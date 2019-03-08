package com.itcollege.radio2019;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.itcollege.radio2019.Domain.Station;
import com.itcollege.radio2019.Repositories.StationRepository;

import java.io.Serializable;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, StationsDialog.StationDialogListener {
    static private final String TAG = MainActivity.class.getSimpleName();
    private BroadcastReceiver mBroadcastReceiver;

    private int mMusicPlayerStatus = C.MUSICSERVICE_STOPPED;
    private boolean mMusicServiceStarted = false;
    private Station mSelectedStation;

    private Button mButtonControlMusic;
    private TextView mTextViewArtist;
    private TextView mTextViewTitle;
    private SeekBar mSeekBarAudioVolume;

    private SettingsContentObserver mSettingsContentObserver;

    private String mArtist;
    private String mTrackTitle;

    private List<Station> mStations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mButtonControlMusic = (Button) findViewById(R.id.buttonControlMusic);
        mTextViewArtist = (TextView) findViewById(R.id.textViewArtist);
        mTextViewTitle = (TextView) findViewById(R.id.textViewTitle);
        mSeekBarAudioVolume = (SeekBar) findViewById(R.id.seekBarAudioVolume);

        // set the seek bar maximum - based on audiomanager reported max
        AudioManager audio = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mSeekBarAudioVolume.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        // and the position
        mSeekBarAudioVolume.setProgress(audio.getStreamVolume(AudioManager.STREAM_MUSIC));


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
        mStations =  artistRepository.getAllOrInitialize();
        Log.d(TAG, "Stations: " + mStations.size());
        artistRepository.close();

        mSelectedStation = mStations.get(0);

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


        mSettingsContentObserver = new SettingsContentObserver( new Handler() );
        this.getApplicationContext().getContentResolver().registerContentObserver(
                android.provider.Settings.System.CONTENT_URI, true,
                mSettingsContentObserver );

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
    }


    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart");
        super.onRestart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
    }

    // ============================== OTHER ===============================

    public void buttonControlMusicOnClick(View view) {
        Log.d(TAG, "buttonControlMusicOnClick");

        if (!mMusicServiceStarted){
            Intent intentStartService = new Intent(this, MusicService.class);
            intentStartService.putExtra(C.SERVICE_STATION_ID_KEY, mSelectedStation.getStationId());
            intentStartService.putExtra(C.SERVICE_STATION_NAME_KEY, mSelectedStation.getName());
            intentStartService.putExtra(C.SERVICE_STATION_STREAM_URL_KEY, mSelectedStation.getStreamUrl());
            intentStartService.putExtra(C.SERVICE_STATION_SONGS_API_URL_KEY, mSelectedStation.getSongNameApiUrl());

            this.startService(intentStartService);
            mMusicServiceStarted = true;
        } else {
            Intent intentInformService = new Intent(C.ACTIVITY_INTENT_STOPPMUSIC);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentInformService);
            mMusicServiceStarted = false;
        }
    }

    public void buttonSelectStationOnClick(View view){
        StationsDialog dialog = new StationsDialog();

        Bundle bundle = new Bundle();
        bundle.putSerializable(C.SERIALIZABLE_STATIONS, (Serializable) mStations);
        dialog.setArguments(bundle);

        dialog.show(getSupportFragmentManager(), "DIALOG TAG");
    }

    public void UpdateUI(){
        switch (mMusicPlayerStatus){
            case C.MUSICSERVICE_STOPPED:
                mButtonControlMusic.setText(C.BUTTONCONTROLMUSIC_LABEL_STOPPED);
                break;
            case C.MUSICSERVICE_BUFFERING:
                mButtonControlMusic.setText(C.BUTTONCONTROLMUSIC_LABEL_BUFFERING);
                break;
            case C.MUSICSERVICE_PLAYING:
                mButtonControlMusic.setText(C.BUTTONCONTROLMUSIC_LABEL_PLAYING);
                break;
        }
        mTextViewArtist.setText(mArtist);
        mTextViewTitle.setText(mTrackTitle);
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
        if (mMusicServiceStarted) buttonControlMusicOnClick(null); // Stops music service if its running
        buttonControlMusicOnClick(null); // Starts music service
    }


    public class MainActivityBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive " + intent.getAction());
            switch (intent.getAction()){
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
                    mArtist = intent.getStringExtra(C.MUSICSERVICE_ARTIST);
                    mTrackTitle = intent.getStringExtra(C.MUSICSERVICE_TRACKTITLE);
                    break;
            }

            UpdateUI();
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

            if (currentVolume != previousVolume){
                previousVolume = currentVolume;
                mSeekBarAudioVolume.setProgress(currentVolume);
            }

        }


    }


    }
