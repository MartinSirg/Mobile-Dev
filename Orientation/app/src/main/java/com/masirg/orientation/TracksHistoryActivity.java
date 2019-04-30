package com.masirg.orientation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.masirg.orientation.Domain.Track;
import com.masirg.orientation.Reposiotories.TracksRepository;

import java.util.List;

public class TracksHistoryActivity extends AppCompatActivity implements
        RecyclerViewAdapter.ItemClickListener,
        RecyclerViewAdapter.ItemButtonsClickListener {

    private final static String TAG = TracksHistoryActivity.class.getSimpleName();

    private RecyclerViewAdapter mRecyclerViewAdapter;
    private TracksRepository mTracksRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks_history);

        List<Track> tracks;
        mTracksRepository = new TracksRepository(this);
        mTracksRepository.open();
        tracks = mTracksRepository.getAll();
        mTracksRepository.close();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewAdapter = new RecyclerViewAdapter(this, tracks, this, this);
        recyclerView.setAdapter(mRecyclerViewAdapter);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: ");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    // =================== Listener methods ======================

    @Override
    public void onRecyclerRowItemClick(View view, int position) {

    }

    @Override
    public void onEditButtonClick(long trackId) {
        Log.d(TAG, "onEditButtonClick: trackId = " + trackId);
    }

    @Override
    public void onViewButtonClick(long trackId) {
        Log.d(TAG, "onViewButtonClick: trackId = " + trackId);
        Log.d(TAG, "buttonStatisticsOnClick: ");
        Intent intent = new Intent(this, OldTrackActivity.class);
        Bundle bundle = new Bundle();
        bundle.putLong(C.TRACK_ID, trackId);
        intent.putExtra(C.START_OLD_TRACK_ACTIVITY_BUNDLE, bundle);
        this.startActivity(intent);
    }

    @Override
    public void onDeleteButtonClick(long trackId) {
        Log.d(TAG, "onDeleteButtonClick: trackId = " + trackId);

    }
}
