package com.itcollege.radio2019;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.itcollege.radio2019.Domain.Artist;
import com.itcollege.radio2019.Domain.Station;
import com.itcollege.radio2019.Fragments.DatePickerFragment;
import com.itcollege.radio2019.Fragments.TimePickerFragment;
import com.itcollege.radio2019.Repositories.ArtistRepository;
import com.itcollege.radio2019.Repositories.SongPlayedRepostory;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener,
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener,
        RecyclerViewAdapter.ItemClickListener {
    private static final String TAG = StatisticsActivity.class.getSimpleName();

    List<Station> mStations;
    private Calendar mCalendarStart;
    private Calendar mCalendarEnd;
    private Station mSelectedStation;
    private boolean mSettingStartDate = true;

    private TextView mTextViewStart;
    private TextView mTextViewEnd;
    private TextView mTextViewArtistsCount;
    private TextView mTextVieUniqueSongsCount;
    private Spinner mSpinnerStations;

    private RecyclerViewAdapter mRecyclerViewAdapter;

    private ArtistRepository mArtistRepo;
    private SongPlayedRepostory mSongsRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        Log.d(TAG, "onCreate: ");

        mSpinnerStations = findViewById(R.id.spinnerStation);
        mTextViewStart = findViewById(R.id.textViewStartTime);
        mTextViewEnd = findViewById(R.id.textViewEndTime);
        mTextViewArtistsCount = findViewById(R.id.textViewUniqueArtists);
        mTextVieUniqueSongsCount = findViewById(R.id.textViewStationUniqueSongs);

        //-----------------------------------Setting up calendars-----------------------------------
        mCalendarStart = Calendar.getInstance();
        mCalendarStart.set(Calendar.SECOND, 0);
        mCalendarStart.set(Calendar.MINUTE, 0);
        mCalendarStart.set(Calendar.HOUR_OF_DAY, 0);
        mCalendarStart.add(Calendar.DATE, -1);

        mCalendarEnd = Calendar.getInstance();
        mCalendarEnd.set(Calendar.SECOND, 0);

        mTextViewStart.setText(DateFormat.getDateTimeInstance().format(mCalendarStart.getTime()));
        mTextViewEnd.setText(DateFormat.getDateTimeInstance().format(mCalendarEnd.getTime()));
        //-------------------------------Stations Spinner-------------------------------------------

        Bundle bundle = getIntent().getExtras().getBundle(C.SERIALIZABLE_STATIONS_BUNDLE);
        mStations = (List<Station>) bundle.getSerializable(C.SERIALIZABLE_STATIONS);
        mSelectedStation = mStations.get(0);
        ArrayAdapter<Station> stationArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, mStations);
        stationArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinnerStations.setAdapter(stationArrayAdapter);
        mSpinnerStations.setOnItemSelectedListener(this);

        // ---------------------------------- Recycler View setup ----------------------------------
        mArtistRepo = new ArtistRepository(this);
        mSongsRepo = new SongPlayedRepostory(this);
    }

    //================================ View buttons listener methods ===============================

    public void buttonSearchOnClick(View view) {
        Log.d(TAG, "buttonSearchOnClick: ");
        mArtistRepo.open();
        List<Artist> artistsWithUniqueSongs = mArtistRepo.getAllWithUniqueSongs(
                mSelectedStation.getStationId(),
                mSongsRepo,
                mCalendarStart.getTimeInMillis() / 1000,
                mCalendarEnd.getTimeInMillis() / 1000);
        mArtistRepo.close();

        RecyclerView recyclerView = findViewById(R.id.recyclerViewStats);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerViewAdapter = new RecyclerViewAdapter(this, artistsWithUniqueSongs);
        mRecyclerViewAdapter.setmItemClickListener(this);
        recyclerView.setAdapter(mRecyclerViewAdapter);

        mTextViewArtistsCount.setText("Unique artists - " + artistsWithUniqueSongs.size());

        int uniqueSongsCount = 0;
        for (Artist artist : artistsWithUniqueSongs) uniqueSongsCount += artist.getUniqueSongs().size();

        mTextVieUniqueSongsCount.setText("Unique songs - " + uniqueSongsCount);
    }

    public void buttonSetStartOnClick(View view) {
        mSettingStartDate = true;
        DialogFragment datePicker = new DatePickerFragment();
        datePicker.show(getSupportFragmentManager(), "tag");
    }

    public void buttonSetEndOnClick(View view) {
        mSettingStartDate = false;
        DialogFragment datePicker = new DatePickerFragment();
        datePicker.show(getSupportFragmentManager(), "tag");
    }

    //================================= Interface Listener methods =================================

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemSelected: " + mStations.get(position).toString());
        mSelectedStation = mStations.get(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        if (mSettingStartDate) {
            mCalendarStart.set(Calendar.YEAR, year);
            mCalendarStart.set(Calendar.MONTH, month);
            mCalendarStart.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            mTextViewStart.setText(DateFormat.getDateTimeInstance().format(mCalendarStart.getTime()));
            DialogFragment timePicker = new TimePickerFragment();
            timePicker.show(getSupportFragmentManager(), "start");
        } else {
            mCalendarEnd.set(Calendar.YEAR, year);
            mCalendarEnd.set(Calendar.MONTH, month);
            mCalendarEnd.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            mTextViewEnd.setText(DateFormat.getDateTimeInstance().format(mCalendarEnd.getTime()));
            DialogFragment timePicker = new TimePickerFragment();
            timePicker.show(getSupportFragmentManager(), "end");
        }

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (mSettingStartDate) {
            mCalendarStart.set(Calendar.HOUR_OF_DAY, hourOfDay);
            mCalendarStart.set(Calendar.MINUTE, hourOfDay);
            mCalendarStart.set(Calendar.SECOND, 0);
            mTextViewStart.setText(DateFormat.getDateTimeInstance().format(mCalendarStart.getTime()));
        } else {
            mCalendarEnd.set(Calendar.HOUR_OF_DAY, hourOfDay);
            mCalendarEnd.set(Calendar.MINUTE, hourOfDay);
            mCalendarEnd.set(Calendar.SECOND, 0);
            mTextViewEnd.setText(DateFormat.getDateTimeInstance().format(mCalendarEnd.getTime()));
        }
    }

    @Override
    public void onRecyclerRowItemClick(View view, int position) {
        Log.d(TAG, "onRecyclerRowItemClick: ");
    }

    // ===================================== LIFECYCLE EVENTS ======================================

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
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
        outState.putLong(C.SAVE_STATE_START_DATE, mCalendarStart.getTimeInMillis());
        outState.putLong(C.SAVE_STATE_END_DATE, mCalendarEnd.getTimeInMillis());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState");
        mCalendarStart.setTimeInMillis(savedInstanceState.getLong(C.SAVE_STATE_START_DATE));
        mCalendarEnd.setTimeInMillis(savedInstanceState.getLong(C.SAVE_STATE_END_DATE));
        mTextViewStart.setText(DateFormat.getDateTimeInstance().format(mCalendarStart.getTime()));
        mTextViewEnd.setText(DateFormat.getDateTimeInstance().format(mCalendarEnd.getTime()));
        super.onRestoreInstanceState(savedInstanceState);
    }
}
