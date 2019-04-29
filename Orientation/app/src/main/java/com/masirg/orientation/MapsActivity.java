package com.masirg.orientation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private LocationManager locationManager;
    private String provider;
    private PolylineOptions mPolylineOptions;

    private Marker mLastWaypointMarker;

    // ========== TextView elements ==========

    private TextView mTotalDistanceTextView;
    private TextView mTotalTimeTextView;
    private TextView mTotalPaceTextView;

    private TextView mCheckpointDistanceTextView;
    private TextView mCheckpointDirectDistanceTextView;
    private TextView mCheckpointPaceTextView;

    private TextView mWaypointDistanceTextView;
    private TextView mWaypointDirectDistanceTextView;
    private TextView mWaypointPaceTextView;
    private Button mStartStopButton;

    private boolean mServiceStarted = false;
    private MapsActivityBroadcastReceiver mBroadcastReceiver;
    private Polyline mPolyLine;
    private List<Marker> mCheckPoints = new ArrayList<>();

    private List<LatLng> mSavedCheckpointsLatLngs;
    private LatLng mSavedWaypointLatLng;

    private int mTotalTime = -1;
    private int mWaypointTime = -1;
    private int mCheckpointTime = -1;
    private double mTotalDistance = -1;
    private double mCheckpointDistance = -1;
    private double mCheckpointDirectDistance = -1;
    private double mWaypointDistance = -1;
    private double mWaypointDirectDistance = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);


        //Find text views from UI
        mTotalDistanceTextView = findViewById(R.id.totalDistance);
        mTotalTimeTextView = findViewById(R.id.totalTime);
        mTotalPaceTextView = findViewById(R.id.totalPace);

        mCheckpointDistanceTextView = findViewById(R.id.checkPointDistance);
        mCheckpointDirectDistanceTextView = findViewById(R.id.checkPointDirectDistance);
        mCheckpointPaceTextView = findViewById(R.id.checkPointPace);

        mWaypointDistanceTextView = findViewById(R.id.waypointDistance);
        mWaypointDirectDistanceTextView = findViewById(R.id.waypointDirectDistance);
        mWaypointPaceTextView = findViewById(R.id.waypointPace);

        mStartStopButton =findViewById(R.id.startStopAllButton);

        updateUI();

        // Register intents we intend to receive
        // this is explicit - ie we can receive only those we specify. this is mandatory.
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(C.ORIENTATION_SERVICE_INTENT_LOCATION_UPDATE);
        intentFilter.addAction(C.ORIENTATION_SERVICE_INTENT_BACKGROUND_LOCATIONS_UPDATE);
        intentFilter.addAction(C.ORIENTATION_SERVICE_INTENT_NEW_CHECKPOINT);
        intentFilter.addAction(C.ORIENTATION_SERVICE_INTENT_NEW_WAYPOINT);
        intentFilter.addAction(C.ORIENTATION_SERVICE_INTENT_STATS_UPDATE);

        //Broadcast receiver

        mBroadcastReceiver = new MapsActivityBroadcastReceiver();

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        if (mServiceStarted) {
            Intent intent = new Intent(C.MAPS_ACTIVITY_INTENT_STOP_COLLECT_IN_BACKGROUND);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
        Intent intent = new Intent(C.MAPS_ACTIVITY_INTENT_COLLECT_IN_BACKGROUND);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
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

        if (isFinishing()) {
            Log.d(TAG, "onDestroy: isFinishing == true, stopping service");
            Intent intentStopService = new Intent(this, OrientationService.class);
            this.stopService(intentStopService);
        }

    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart");
        super.onRestart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: mService is started = " + mServiceStarted);
        outState.putBoolean(C.INSTANCE_STATE_SERVICE_STARTED, mServiceStarted);

        double[] latitudes = new double[mPolylineOptions.getPoints().size()];
        double[] longitudes = new double[mPolylineOptions.getPoints().size()];
        for (int i = 0; i < mPolylineOptions.getPoints().size(); i++) {
            latitudes[i] = mPolylineOptions.getPoints().get(i).latitude;
            longitudes[i] = mPolylineOptions.getPoints().get(i).longitude;
        }

        double[] checkpointLatitudes = new double[mCheckPoints.size()];
        double[] checkpointLongitudes = new double[mCheckPoints.size()];

        for (int i = 0; i < mCheckPoints.size(); i++) {
            checkpointLatitudes[i] = mCheckPoints.get(i).getPosition().latitude;
            checkpointLongitudes[i] = mCheckPoints.get(i).getPosition().longitude;
        }

        outState.putDoubleArray(C.INSTANCE_STATE_LOCATIONS_LATITUDES, latitudes);
        outState.putDoubleArray(C.INSTANCE_STATE_LOCATIONS_LONGITUDES, longitudes);

        if (checkpointLatitudes.length > 0){
            outState.putDoubleArray(C.INSTANCE_STATE_CHECKPOINT_LOCATIONS_LATITUDES, checkpointLatitudes);
            outState.putDoubleArray(C.INSTANCE_STATE_CHECKPOINT_LOCATIONS_LONGITUDES, checkpointLongitudes);
        }

        if (mLastWaypointMarker != null){
            outState.putDouble(C.INSTANCE_STATE_WAYPOINT_LATITUDE, mLastWaypointMarker.getPosition().latitude);
            outState.putDouble(C.INSTANCE_STATE_WAYPOINT_LONGITUDE, mLastWaypointMarker.getPosition().longitude);
        }

        outState.putInt(C.INSTANCE_STATE_TOTAL_TIME, mTotalTime);
        outState.putDouble(C.INSTANCE_STATE_TOTAL_DISTANCE, mTotalDistance);

        outState.putDouble(C.INSTANCE_STATE_CHECKPOINT_DISTANCE, mCheckpointDistance);
        outState.putDouble(C.INSTANCE_STATE_CHECKPOINT_DIRECT_DISTANCE, mCheckpointDirectDistance);
        outState.putInt(C.INSTANCE_STATE_CHECKPOINT_TIME, mCheckpointTime);

        outState.putInt(C.INSTANCE_STATE_WAYPOINT_TIME, mWaypointTime);
        outState.putDouble(C.INSTANCE_STATE_WAYPOINT_DISTANCE, mWaypointDistance);
        outState.putDouble(C.INSTANCE_STATE_WAYPOINT_DIRECT_DISTANCE, mWaypointDirectDistance);


        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState");

        mServiceStarted = savedInstanceState.getBoolean(C.INSTANCE_STATE_SERVICE_STARTED);
        if (mServiceStarted) mStartStopButton.setText("STOP");

        mPolylineOptions = new PolylineOptions()
                .color(Color.RED)
                .width(10);

        double[] lats = savedInstanceState.getDoubleArray(C.INSTANCE_STATE_LOCATIONS_LATITUDES);
        double[] lngs = savedInstanceState.getDoubleArray(C.INSTANCE_STATE_LOCATIONS_LONGITUDES);

        if (lats != null && lngs != null && lats.length != 0 && lngs.length != 0) {
            Log.d(TAG, "onRestoreInstanceState: Detected previous locations");
            for (int i = 0; i < lats.length; i++) {
                mPolylineOptions.add(new LatLng(lats[i], lngs[i]));
            }

        }

        double[] checkpointLats = savedInstanceState.getDoubleArray(C.INSTANCE_STATE_CHECKPOINT_LOCATIONS_LATITUDES);
        double[] checkpointLngs = savedInstanceState.getDoubleArray(C.INSTANCE_STATE_CHECKPOINT_LOCATIONS_LONGITUDES);

        if (checkpointLats != null && checkpointLngs != null && checkpointLats.length != 0 && checkpointLngs.length != 0) {
            Log.d(TAG, "onRestoreInstanceState: Detected previous checkpoints");
            mSavedCheckpointsLatLngs = new ArrayList<>();
            for (int i = 0; i < checkpointLats.length; i++) {
                mSavedCheckpointsLatLngs.add(new LatLng(checkpointLats[i], checkpointLngs[i]));
            }

        }
        double waypointLat = savedInstanceState.getDouble(C.INSTANCE_STATE_WAYPOINT_LATITUDE, -1);
        double waypointLng = savedInstanceState.getDouble(C.INSTANCE_STATE_WAYPOINT_LONGITUDE, -1);
        if (waypointLat != -1 && waypointLng != -1) {
            Log.d(TAG, "onRestoreInstanceState: Detected previous waypoint");
            mSavedWaypointLatLng = new LatLng(waypointLat, waypointLng);
        }

        mTotalTime = savedInstanceState.getInt(C.INSTANCE_STATE_TOTAL_TIME, -1);
        mTotalDistance = savedInstanceState.getDouble(C.INSTANCE_STATE_TOTAL_DISTANCE, -1);

        mCheckpointDistance = savedInstanceState.getDouble(C.INSTANCE_STATE_CHECKPOINT_DISTANCE, -1);
        mCheckpointDirectDistance = savedInstanceState.getDouble(C.INSTANCE_STATE_CHECKPOINT_DIRECT_DISTANCE, -1);
        mCheckpointTime = savedInstanceState.getInt(C.INSTANCE_STATE_CHECKPOINT_TIME, -1);

        mWaypointDistance = savedInstanceState.getDouble(C.INSTANCE_STATE_WAYPOINT_DISTANCE, -1);
        mWaypointDirectDistance = savedInstanceState.getDouble(C.INSTANCE_STATE_WAYPOINT_DIRECT_DISTANCE, -1);
        mWaypointTime = savedInstanceState.getInt(C.INSTANCE_STATE_WAYPOINT_TIME, -1);

        updateUI();
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: ");

        moveTaskToBack(false);
    }

    //=======================================

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "onMapReady: Permission missing");
            return;
        }
        mMap.setMyLocationEnabled(true);
        if (mSavedCheckpointsLatLngs != null){
            for (LatLng latLng : mSavedCheckpointsLatLngs) {
                mCheckPoints.add(mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        .title("Checkpoint #" + (mCheckPoints.size() + 1))));
            }
        }

        if (mSavedWaypointLatLng != null){
            mLastWaypointMarker = mMap.addMarker(new MarkerOptions()
                    .position(mSavedWaypointLatLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title("Waypoint"));
        }


        if (mPolylineOptions != null) {
            Log.d(TAG, "onMapReady: Placing previous points on map");
            mPolyLine = mMap.addPolyline(mPolylineOptions);
            if (mPolyLine.getPoints().size() == 0) return;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mPolyLine.getPoints().get(mPolyLine.getPoints().size() - 1), 16.0f));
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            Log.d(TAG, "onMapReady: moving camera to current location");
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));

        } else {
            Log.d(TAG, "onMapReady: last location missing");
        }
    }

    //=======================================
    public void startStopButtonClicked(View view) {
        Log.d(TAG, "startStopButtonClicked: ");
        if (!mServiceStarted) {
            ((Button) view).setText("STOP");
            mServiceStarted = true;

            Intent intentStartService = new Intent(this, OrientationService.class);
            this.startService(intentStartService);

            if (mPolyLine != null) mPolyLine.remove();
            mPolylineOptions = new PolylineOptions().width(10).color(Color.RED);
            mPolyLine = mMap.addPolyline(mPolylineOptions);
        } else {
            //TODO: double check are you sure you want to stop
            ((Button) view).setText("START");
            mServiceStarted = false;
            Intent intentStopService = new Intent(this, OrientationService.class);
            this.stopService(intentStopService);
        }

    }

    public void addCheckpointButtonClicked(View view) {
        Log.d(TAG, "addCheckpointButtonClicked: ");
        Intent waypointIntent = new Intent(C.MAPS_ACTIVITY_INTENT_ADD_CHECKPOINT);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(waypointIntent);
    }

    public void addWaypointButtonClicked(View view) {
        Log.d(TAG, "addWaypointButtonClicked: ");
        Intent waypointIntent = new Intent(C.MAPS_ACTIVITY_INTENT_ADD_WAYPOINT);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(waypointIntent);
    }

    //=======================================

    @SuppressLint("DefaultLocale")
    public void updateUI() {
        Log.d(TAG, "updateUI: ");
        if (mTotalDistance == -1) {
            mTotalDistanceTextView.setText("-");
            mTotalTimeTextView.setText("-");
            mTotalPaceTextView.setText("-");
        } else {
            mTotalDistanceTextView.setText(String.format("%.0f m", mTotalDistance));

            mTotalTimeTextView.setText(String.format("%d:%02d:%02d",
                    mTotalTime / 3600, mTotalTime / 60 , mTotalTime % 60));

            if (mTotalDistance > 0){
                double minsPerKm = (mTotalTime * 50.0) / (mTotalDistance * 3);
                mTotalPaceTextView.setText(String.format("%.0f:%02.0f min/km", minsPerKm, (minsPerKm % 1) * 60));
            }
        }

        if (mCheckpointDistance == -1) {
            mCheckpointDistanceTextView.setText("-");
            mCheckpointDirectDistanceTextView.setText("-");
            mCheckpointPaceTextView.setText("-");
        } else {
            mCheckpointDistanceTextView.setText(String.format("%.0f m", mCheckpointDistance));
            mCheckpointDirectDistanceTextView.setText(String.format("%.0f m", mCheckpointDirectDistance));

            if (mCheckpointDistance > 0){
                double minsPerKm = (mCheckpointTime * 50.0) / (mCheckpointDistance * 3);
                mCheckpointPaceTextView.setText(String.format("%.0f:%02.0f min/km", minsPerKm, (minsPerKm % 1) * 60));
            }
        }

        if (mWaypointDistance == -1) {
            mWaypointDistanceTextView.setText("-");
            mWaypointDirectDistanceTextView.setText("-");
            mWaypointPaceTextView.setText("-");
        } else {
            mWaypointDistanceTextView.setText(String.format("%.0f m", mWaypointDistance));
            mWaypointDirectDistanceTextView.setText(String.format("%.0f m", mWaypointDirectDistance));

            if (mWaypointDistance > 0){
                double minsPerKm = (mWaypointTime * 50.0) / (mWaypointDistance * 3);
                mWaypointPaceTextView.setText(String.format("%.0f:%02.0f min/km", minsPerKm, (minsPerKm % 1) * 60));
            }
        }



    }

    //======================================

    private class MapsActivityBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;

            switch (intent.getAction()) {
                case C.ORIENTATION_SERVICE_INTENT_LOCATION_UPDATE:
                    double lat = intent.getDoubleExtra(C.ORIENTATION_SERVICE_LATITUDE, -1);
                    double lng = intent.getDoubleExtra(C.ORIENTATION_SERVICE_LONGITUDE, -1);
                    Log.d(TAG, "onReceive Location: LATITUDE = " + lat + "; LONGITUDE = " + lng);
                    if (lat == -1 || lng == -1 || !mServiceStarted) return;

                    LatLng latlng = new LatLng(lat, lng);
                    mPolylineOptions.add(latlng);
                    mPolyLine.setPoints(mPolylineOptions.getPoints());
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
                    break;
                case C.ORIENTATION_SERVICE_INTENT_BACKGROUND_LOCATIONS_UPDATE:
                    Log.d(TAG, "onReceive: Receiving all locations");
                    double[] lats = intent.getDoubleArrayExtra(C.ORIENTATION_SERVICE_LATITUDES);
                    double[] lngs = intent.getDoubleArrayExtra(C.ORIENTATION_SERVICE_LONGITUDES);
                    if (lats.length != lngs.length || lats.length == 0 || !mServiceStarted) return;

                    List<LatLng> locations = new ArrayList<>();
                    for (int i = 0; i < lats.length; i++) {
                        locations.add(new LatLng(lats[i], lngs[i]));
                    }
                    mPolylineOptions.addAll(locations);
                    mPolyLine.setPoints(mPolylineOptions.getPoints());
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(locations.get(locations.size() - 1)));
                    Log.d(TAG, "onReceive: All locations added");
                    break;
                case C.ORIENTATION_SERVICE_INTENT_NEW_CHECKPOINT:
                    double checkpointLat = intent.getDoubleExtra(C.ORIENTATION_SERVICE_LATITUDE, -1);
                    double checkpointLng = intent.getDoubleExtra(C.ORIENTATION_SERVICE_LONGITUDE, -1);
                    if (checkpointLat == -1 || checkpointLng == -1) return;

                    mCheckPoints.add(mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(checkpointLat, checkpointLng))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            .title("Checkpoint #" + (mCheckPoints.size() + 1))));
                    break;
                case C.ORIENTATION_SERVICE_INTENT_NEW_WAYPOINT:
                    double waypointLat = intent.getDoubleExtra(C.ORIENTATION_SERVICE_LATITUDE, -1);
                    double waypointLng = intent.getDoubleExtra(C.ORIENTATION_SERVICE_LONGITUDE, -1);
                    if (waypointLat == -1 || waypointLng == -1) return;

                    if (mLastWaypointMarker != null) mLastWaypointMarker.remove();
                    mLastWaypointMarker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(waypointLat, waypointLng))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .title("Waypoint"));
                    break;

                case C.ORIENTATION_SERVICE_INTENT_STATS_UPDATE:
                    mTotalTime = intent.getIntExtra(C.ORIENTATION_SERVICE_TOTAL_TIME, -1);
                    mTotalDistance = intent.getDoubleExtra(C.ORIENTATION_SERVICE_TOTAL_DISTANCE, -1);

                    mCheckpointDistance = intent.getDoubleExtra(C.ORIENTATION_SERVICE_CHECKPOINT_DISTANCE, -1);
                    mCheckpointDirectDistance = intent.getDoubleExtra(C.ORIENTATION_SERVICE_CHECKPOINT_DIRECT_DISTANCE, -1);
                    mCheckpointTime = intent.getIntExtra(C.ORIENTATION_SERVICE_CHECKPOINT_TIME, -1);

                    mWaypointDistance = intent.getDoubleExtra(C.ORIENTATION_SERVICE_WAYPOINT_DISTANCE, -1);
                    mWaypointDirectDistance = intent.getDoubleExtra(C.ORIENTATION_SERVICE_WAYPOINT_DIRECT_DISTANCE, -1);
                    mWaypointTime = intent.getIntExtra(C.ORIENTATION_SERVICE_WAYPOINT_TIME, -1);
                    updateUI();
            }
        }
    }
}
