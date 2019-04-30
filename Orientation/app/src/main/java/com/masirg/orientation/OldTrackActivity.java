package com.masirg.orientation;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.PolylineOptions;
import com.masirg.orientation.Domain.Track;
import com.masirg.orientation.Domain.TrackCheckpoint;
import com.masirg.orientation.Domain.TrackPoint;
import com.masirg.orientation.Reposiotories.TrackCheckpointsRepository;
import com.masirg.orientation.Reposiotories.TrackPointsRepository;
import com.masirg.orientation.Reposiotories.TracksRepository;

import java.util.List;

public class OldTrackActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = OldTrackActivity.class.getSimpleName();

    private GoogleMap mMap;
    private PolylineOptions mPolylineOptions;

    private TracksRepository mTracksRepo;
    private TrackPointsRepository mPointsRepo;
    private TrackCheckpointsRepository mCheckpointsRepository;

    private Track mTrack;
    private List<TrackPoint> mTrackPoints;
    private List<TrackCheckpoint> mTrackCheckpoints;

    private TextView mTotalTimeTextView;
    private TextView mTotalDistanceTextView;
    private TextView mPaceTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_old_track);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapOld);
        mapFragment.getMapAsync(this);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //TODO: save stats, polylines, checkpoints
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //TODO: restore stats, polylines, checkpoints
    }

    // ==================Other methods=======================
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Bundle bundle = getIntent().getExtras().getBundle(C.START_OLD_TRACK_ACTIVITY_BUNDLE);
        if (bundle == null) throw new NullPointerException("Bundle null in onCreate. Please pass a bundle when launching this activity");
        long trackId = bundle.getLong(C.TRACK_ID, -1);
        if (trackId == -1) throw new NullPointerException("Track id wasn't found in the bundle");

        mTotalTimeTextView = findViewById(R.id.totalTimeOld);
        mTotalDistanceTextView = findViewById(R.id.totalDistanceOld);
        mPaceTextView = findViewById(R.id.totalPaceOld);

        mPolylineOptions = new PolylineOptions().width(10).color(Color.BLUE);

        mTracksRepo = new TracksRepository(this);
        mPointsRepo = new TrackPointsRepository(this);
        mCheckpointsRepository = new TrackCheckpointsRepository(this);

        mTracksRepo.open();
        mTrack = mTracksRepo.get(trackId);
        mTracksRepo.close();
        if (mTrack == null) {
            Log.d(TAG, "onMapReady: ERROR, track not found in database");
            onBackPressed();
            return;
        }

        mPointsRepo.open();
        mTrackPoints = mPointsRepo.getAllTrackPoints(mTrack.getTrackId());
        mPointsRepo.close();

        mCheckpointsRepository.open();
        mTrackCheckpoints = mCheckpointsRepository.getAllTrackCheckpoints(mTrack.getTrackId());
        mCheckpointsRepository.close();

    }
}
