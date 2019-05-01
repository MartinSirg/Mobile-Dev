package com.masirg.orientation;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.masirg.orientation.Domain.Track;
import com.masirg.orientation.Reposiotories.TrackCheckpointsRepository;
import com.masirg.orientation.Reposiotories.TrackPointsRepository;
import com.masirg.orientation.Reposiotories.TracksRepository;

import java.util.List;

public class TracksHistoryActivity extends AppCompatActivity implements
        RecyclerViewAdapter.ItemClickListener,
        RecyclerViewAdapter.ItemButtonsClickListener,
        ConfirmDeleteDialog.ConfirmDeleteDialogListener
{

    private final static String TAG = TracksHistoryActivity.class.getSimpleName();

    private RecyclerViewAdapter mRecyclerViewAdapter;
    private TracksRepository mTracksRepository;
    private TrackPointsRepository mPointsRepository;
    private TrackCheckpointsRepository mCheckpointsRepository;
    private List<Track> mTracks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks_history);

        mTracksRepository = new TracksRepository(this);
        mPointsRepository = new TrackPointsRepository(this);
        mCheckpointsRepository = new TrackCheckpointsRepository(this);

        mTracksRepository.open();
        mTracks = mTracksRepository.getAll();
        mTracksRepository.close();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewAdapter = new RecyclerViewAdapter(this, mTracks, this, this);
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
    public void onEditButtonClick(long trackId, int position) {
        Log.d(TAG, "onEditButtonClick: trackId = " + trackId);
    }

    @Override
    public void onViewButtonClick(long trackId, int position) {
        Log.d(TAG, "onViewButtonClick: trackId = " + trackId);
        Log.d(TAG, "buttonStatisticsOnClick: ");
        Intent intent = new Intent(this, OldTrackActivity.class);
        Bundle bundle = new Bundle();
        bundle.putLong(C.TRACK_ID, trackId);
        intent.putExtra(C.START_OLD_TRACK_ACTIVITY_BUNDLE, bundle);
        this.startActivity(intent);
    }

    @Override
    public void onDeleteButtonClick(long trackId, int position) {
        Log.d(TAG, "onDeleteButtonClick: trackId = " + trackId);
        ConfirmDeleteDialog dialog = new ConfirmDeleteDialog();
        dialog.setTrackId(trackId)
                .setPosition(position)
                .show(getSupportFragmentManager(), "deleteConfirmDialog");
    }

    @Override
    public void OnDeleteConfirmedClicked(long trackId, int position) {
        Log.d(TAG, "OnDeleteConfirmedClicked: trackId = " + trackId);

        mTracksRepository.open();
        mTracksRepository.delete(trackId);
        mTracksRepository.close();

        mPointsRepository.open();
        mPointsRepository.deleteTrackPoints(trackId);
        mPointsRepository.close();

        mCheckpointsRepository.open();
        mCheckpointsRepository.deleteTrackCheckpoints(trackId);
        mCheckpointsRepository.close();

        mTracks.remove(position);
        mRecyclerViewAdapter.notifyItemRemoved(position);
    }
}
