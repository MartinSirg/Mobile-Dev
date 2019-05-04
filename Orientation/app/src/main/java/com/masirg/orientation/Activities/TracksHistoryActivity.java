package com.masirg.orientation.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.masirg.orientation.C;
import com.masirg.orientation.Dialogs.ConfirmDeleteDialog;
import com.masirg.orientation.Domain.Track;
import com.masirg.orientation.Domain.TrackCheckpoint;
import com.masirg.orientation.Domain.TrackPoint;
import com.masirg.orientation.R;
import com.masirg.orientation.RecyclerViewAdapter;
import com.masirg.orientation.Reposiotories.TrackCheckpointsRepository;
import com.masirg.orientation.Reposiotories.TrackPointsRepository;
import com.masirg.orientation.Reposiotories.TracksRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.SimpleFormatter;

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
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

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

    @SuppressLint("DefaultLocale")
    @Override
    public void onEditButtonClick(long trackId, int position) {
        Log.d(TAG, "onEditButtonClick: trackId = " + trackId);


        mPointsRepository.open();
        List<TrackPoint> trackPoints = mPointsRepository.getAllTrackPoints(trackId);
        mPointsRepository.close();

        mCheckpointsRepository.open();
        List<TrackCheckpoint> trackCheckpoints = mCheckpointsRepository.getAllTrackCheckpoints(trackId);
        mCheckpointsRepository.close();

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<gpx version=\"1.1\" creator=\"MartinSirgOrientationApp\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd\" xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\" xmlns:gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");

        for (TrackCheckpoint cp : trackCheckpoints) {
            sb.append("<wpt lat=\"").append(String.format("%.8f", cp.getLatitude())).append("\" lon=\"").append(String.format("%.8f", cp.getLongitude())).append("\">\n");
            sb.append("<ele>").append(String.format("%.0f", cp.getAltitude())).append("</ele>\n");
            sb.append("<name>Checkpoint #").append(trackCheckpoints.indexOf(cp) + 1).append("</name>");
            sb.append("</wpt>\n");
        }
        sb.append("<trk><trkseg>\n");
        for (TrackPoint point : trackPoints) {
            sb.append("<trkpt lat=\"").append(String.format("%.8f", point.getLatitude())).append("\" lon=\"").append(String.format("%.8f", point.getLongitude())).append("\"><ele>").append(String.format("%.0f", point.getAltitude()))
                    .append("</ele><time>").append(formatter.format(new Date(point.getTime() * 1000))).append("</time></trkpt>\n");
        }
        sb.append("</trkseg></trk>\n");
        sb.append("</gpx>");

        writeFile(sb.toString(), "Track-" + formatter.format(new Date(mTracks.get(position).getCreationTime() * 1000)) + ".gpx");
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

    private boolean isExternalStorageWritable(){
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            Log.i("State","Yes, it is writable!");
            return true;
        }else{
            return false;
        }
    }

    public boolean checkPermission(String permission){
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    //======================= https://www.youtube.com/watch?v=7CEcevGbIZU =======================
    public void writeFile(String text, String fileName){
        if(isExternalStorageWritable() && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            File textFile = new File(Environment.getExternalStorageDirectory(), fileName);
            try{
                FileOutputStream fos = new FileOutputStream(textFile);
                fos.write(text.getBytes());
                fos.close();

                Toast.makeText(this, "File Saved.", Toast.LENGTH_SHORT).show();
            }catch (IOException e){
                e.printStackTrace();
            }
        }else{
            Toast.makeText(this, "Cannot Write to External Storage.", Toast.LENGTH_SHORT).show();
        }
    }
}
