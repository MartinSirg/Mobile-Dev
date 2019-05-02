package com.masirg.orientation.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.masirg.orientation.C;
import com.masirg.orientation.Dialogs.ConfirmStopDialog;
import com.masirg.orientation.OrientationService;
import com.masirg.orientation.R;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        ConfirmStopDialog.ConfirmStopDialogListener,
        LocationListener,
        SensorEventListener {

    private static final String TAG = "MapsActivity";
    private static final float DEFAULT_ZOOM = 16;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private String provider;

    // ======================================

    private float[] mGravity = new float[3];
    private float[] mGeoMagnetic = new float[3];
    private float mAzimuth = 0f;
    private float mCurrentAzimuth = 0f;
    private SensorManager mSensorManager;

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
    private ImageView mCompassImageView;

    private boolean mServiceStarted = false;
    private MapsActivityBroadcastReceiver mBroadcastReceiver;

    private Polyline mPolyLine;
    private List<Marker> mCheckPoints = new ArrayList<>();
    private PolylineOptions mPolylineOptions;
    private Marker mLastWaypointMarker;

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
    private ToggleButton mNorthUpToggleButton;
    private ToggleButton mCompassToggleButton;
    private ToggleButton mCenterToggleButton;

    private boolean mCenterToggled;
    private boolean mNorthUpToggled;
    private boolean mCompassToggled;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);


        //Find text views from UI
        mTotalDistanceTextView = findViewById(R.id.totalDistanceOld);
        mTotalTimeTextView = findViewById(R.id.totalTimeOld);
        mTotalPaceTextView = findViewById(R.id.totalPaceOld);

        mCheckpointDistanceTextView = findViewById(R.id.checkPointDistance);
        mCheckpointDirectDistanceTextView = findViewById(R.id.checkPointDirectDistance);
        mCheckpointPaceTextView = findViewById(R.id.checkPointPace);

        mWaypointDistanceTextView = findViewById(R.id.waypointDistance);
        mWaypointDirectDistanceTextView = findViewById(R.id.waypointDirectDistance);
        mWaypointPaceTextView = findViewById(R.id.waypointPace);

        mStartStopButton = findViewById(R.id.startStopAllButton);

        mNorthUpToggleButton = findViewById(R.id.northUpToggleButton);
        mCenterToggleButton = findViewById(R.id.centerToggleButton);
        mCompassToggleButton = findViewById(R.id.compassToggleButton);

        // ===========================Compass===========================
        mCompassImageView = findViewById(R.id.compassImageView);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);



        setUpToggleButtonListeners();

        updateUI();

        // Register intents we intend to receive
        // this is explicit - ie we can receive only those we specify. this is mandatory.
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(C.ORIENTATION_SERVICE_INTENT_LOCATION_UPDATE);
        intentFilter.addAction(C.ORIENTATION_SERVICE_INTENT_BACKGROUND_LOCATIONS_UPDATE);
        intentFilter.addAction(C.ORIENTATION_SERVICE_INTENT_NEW_CHECKPOINT);
        intentFilter.addAction(C.ORIENTATION_SERVICE_INTENT_NEW_WAYPOINT);
        intentFilter.addAction(C.ORIENTATION_SERVICE_INTENT_STATS_UPDATE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapOld);
        mapFragment.getMapAsync(this);

        //Broadcast receiver
        mBroadcastReceiver = new MapsActivityBroadcastReceiver();

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void setUpToggleButtonListeners() {
        mNorthUpToggleButton.setChecked(false);
        mNorthUpToggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "North-Up Toggled = " + isChecked);

            if (mMap == null) {
                buttonView.setChecked(!isChecked);
                return;
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location lastLocation = locationManager.getLastKnownLocation(provider);

            if (isChecked) {
                mMap.getUiSettings().setRotateGesturesEnabled(false);
                CameraPosition position = CameraPosition.builder()
                        .target(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
                        .bearing(0)
                        .zoom(DEFAULT_ZOOM)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
            } else {
                mMap.getUiSettings().setRotateGesturesEnabled(true);
            }

            mNorthUpToggled = isChecked;
        });

        mCenterToggleButton.setChecked(false);
        mCenterToggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "Center Toggled = " + isChecked);

            if (mMap == null) {
                buttonView.setChecked(!isChecked);
                return;
            }

            Location lastLocation = locationManager.getLastKnownLocation(provider);
            if (isChecked) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude())));
                mMap.getUiSettings().setScrollGesturesEnabled(false);
            } else {
                mMap.getUiSettings().setScrollGesturesEnabled(true);
            }

            mCenterToggled = isChecked;
            if (mServiceStarted) return;
            if (isChecked) {
                locationManager.requestLocationUpdates(provider, 1000, 1, this);
            } else {
                locationManager.removeUpdates(this);
            }
        });

        mCompassToggleButton.setChecked(false);
        mCompassToggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "Compass Toggled = " + isChecked);
            mCompassToggled = isChecked;
            if (isChecked){
                mCompassImageView.setVisibility(View.VISIBLE);
            }else {
                mCompassImageView.setVisibility(View.INVISIBLE);
            }
        });
    }

    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);

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
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        locationManager.removeUpdates(this);
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

        if (mPolylineOptions != null) {
            double[] latitudes = new double[mPolylineOptions.getPoints().size()];
            double[] longitudes = new double[mPolylineOptions.getPoints().size()];
            for (int i = 0; i < mPolylineOptions.getPoints().size(); i++) {
                latitudes[i] = mPolylineOptions.getPoints().get(i).latitude;
                longitudes[i] = mPolylineOptions.getPoints().get(i).longitude;

                outState.putDoubleArray(C.INSTANCE_STATE_LOCATIONS_LATITUDES, latitudes);
                outState.putDoubleArray(C.INSTANCE_STATE_LOCATIONS_LONGITUDES, longitudes);
            }
        }

        if (mCheckPoints != null && mCheckPoints.size() != 0) {
            double[] checkpointLatitudes = new double[mCheckPoints.size()];
            double[] checkpointLongitudes = new double[mCheckPoints.size()];

            for (int i = 0; i < mCheckPoints.size(); i++) {
                checkpointLatitudes[i] = mCheckPoints.get(i).getPosition().latitude;
                checkpointLongitudes[i] = mCheckPoints.get(i).getPosition().longitude;
                outState.putDoubleArray(C.INSTANCE_STATE_CHECKPOINT_LOCATIONS_LATITUDES, checkpointLatitudes);
                outState.putDoubleArray(C.INSTANCE_STATE_CHECKPOINT_LOCATIONS_LONGITUDES, checkpointLongitudes);
            }
        }

        if (mLastWaypointMarker != null) {
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

        outState.putBoolean(C.INSTANCE_STATE_NORTH_TOGGLED, mNorthUpToggled);
        outState.putBoolean(C.INSTANCE_STATE_CENTER_TOGGLED, mCenterToggled);
        outState.putBoolean(C.INSTANCE_STATE_COMPASS_TOGGLED, mCompassToggled);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState");

        mServiceStarted = savedInstanceState.getBoolean(C.INSTANCE_STATE_SERVICE_STARTED, false);
        if (mServiceStarted) mStartStopButton.setText("STOP");

        mNorthUpToggled = savedInstanceState.getBoolean(C.INSTANCE_STATE_NORTH_TOGGLED, false);

        mCenterToggled = savedInstanceState.getBoolean(C.INSTANCE_STATE_CENTER_TOGGLED, false);

        mCompassToggled = savedInstanceState.getBoolean(C.INSTANCE_STATE_COMPASS_TOGGLED, false);
        mCompassToggleButton.setChecked(mCompassToggled);

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
        Location location = locationManager.getLastKnownLocation(provider);
        if (mCenterToggled){
            mCenterToggleButton.setChecked(true);
        }
        if (mNorthUpToggled){
            mNorthUpToggleButton.setChecked(true);
        }

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (mSavedCheckpointsLatLngs != null) {
            for (LatLng latLng : mSavedCheckpointsLatLngs) {
                mCheckPoints.add(mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        .title("Checkpoint #" + (mCheckPoints.size() + 1))));
            }
        }

        if (mSavedWaypointLatLng != null) {
            mLastWaypointMarker = mMap.addMarker(new MarkerOptions()
                    .position(mSavedWaypointLatLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title("Waypoint"));
        }
        if (mPolylineOptions != null) {
            Log.d(TAG, "onMapReady: Placing previous points on map");
            mPolyLine = mMap.addPolyline(mPolylineOptions);
            if (mPolyLine.getPoints().size() == 0) return;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mPolyLine.getPoints().get(mPolyLine.getPoints().size() - 1), DEFAULT_ZOOM));
            return;
        }

        if (location != null) {
            Log.d(TAG, "onMapReady: moving camera to current location");
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

        } else {
            Log.d(TAG, "onMapReady: last location missing");
        }
    }

    //=======================================
    public void startStopButtonClicked(View view) {

        if (!mServiceStarted) {
            Log.d(TAG, "startStopButtonClicked: START");
            ((Button) view).setText("STOP");
            clearMap();
            locationManager.removeUpdates(this);
            mServiceStarted = true;

            Intent intentStartService = new Intent(this, OrientationService.class);
            this.startService(intentStartService);
            mPolylineOptions = new PolylineOptions().width(10).color(Color.RED);
            mPolyLine = mMap.addPolyline(mPolylineOptions);
        } else {
            Log.d(TAG, "startStopButtonClicked: STOP");
            ConfirmStopDialog dialog = new ConfirmStopDialog();
            dialog.show(getSupportFragmentManager(), "stopConfirmDialog");
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

    public void optionsButtonClicked(View view) {
        Intent intent = new Intent(this, TracksHistoryActivity.class);
        this.startActivity(intent);
    }

    public void clearButtonClicked(View view) {
        clearMap();
    }

    @Override
    public void OnStopConfirmedClicked() {
        Log.d(TAG, "OnStopConfirmedClicked: ");
        mStartStopButton.setText("START");
        mServiceStarted = false;
        Intent intentStopService = new Intent(this, OrientationService.class);
        this.stopService(intentStopService);
        if (mCenterToggled) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(provider, 1000, 1, this);
        }
    }


    //====================== UI update methods =======================

    private void clearMap() {
        Log.d(TAG, "clearMap: ");
        if (mServiceStarted) return;
        if (mPolylineOptions != null) mPolylineOptions = null;
        if (mPolyLine != null) mPolyLine.remove();
        if (mLastWaypointMarker != null) mLastWaypointMarker.remove();
        if (mCheckPoints != null && mCheckPoints.size() > 0) {
            for (Marker marker : mCheckPoints) {
                Log.d(TAG, "clearMap: Removing checkpoint");
                marker.remove();
            }
        } else {
            Log.d(TAG, "clearMap: didn't find any checkpoints");
        }
        mTotalTime = -1;
        mTotalDistance = -1;

        mCheckpointDistance = -1;
        mCheckpointDirectDistance = -1;
        mCheckpointTime = -1;

        mWaypointDistance = -1;
        mWaypointDirectDistance = -1;
        mWaypointTime = -1;
        updateUI();

    }

    @SuppressLint("DefaultLocale")
    public void updateUI() {

        if (mTotalTime == -1) {
            mTotalTimeTextView.setText("-");
        } else {
            Log.d(TAG, "updateUI: Updating time");
            mTotalTimeTextView.setText(String.format("%d:%02d:%02d",
                    mTotalTime / 3600, mTotalTime / 60 , mTotalTime % 60));
        }

        if (mTotalDistance == -1) {
            mTotalDistanceTextView.setText("-");
            mTotalPaceTextView.setText("-");
        } else {
            mTotalDistanceTextView.setText(String.format("%.0f m", mTotalDistance));

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

    //======= Location updates when service not running and centered is toggled =======
    @Override
    public void onLocationChanged(Location location) {
        if (mMap == null) return;

        if (mCenterToggled){
            mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //================================== Sensor events ==================================
    @Override
    public void onSensorChanged(SensorEvent event) {

        final float alpha = 0.97f;
        synchronized (this){
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                mGravity[0] = alpha * mGravity[0] + (1 - alpha) * event.values[0];
                mGravity[1] = alpha * mGravity[1] + (1 - alpha) * event.values[1];
                mGravity[2] = alpha * mGravity[1] + (1 - alpha) * event.values[2];
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                mGeoMagnetic[0] = alpha * mGeoMagnetic[0] + (1 - alpha) * event.values[0];
                mGeoMagnetic[1] = alpha * mGeoMagnetic[1] + (1 - alpha) * event.values[1];
                mGeoMagnetic[2] = alpha * mGeoMagnetic[1] + (1 - alpha) * event.values[2];
            }

            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeoMagnetic);
            if (success){
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                mAzimuth = (float)Math.toDegrees(orientation[0]);
                mAzimuth = (mAzimuth + 360) % 360;

                if (!mCompassToggled) return;
                Animation anim = new RotateAnimation(-mCurrentAzimuth, -mAzimuth, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                mCurrentAzimuth = mAzimuth;
                anim.setDuration(500);
                anim.setRepeatCount(0);
                mCompassImageView.startAnimation(anim);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //====================== Broadcast receiver ======================

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
                    if (mCenterToggled){
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
                    }
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
                    if (mCenterToggled) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(locations.get(locations.size() - 1)));
                    }
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
                    Log.d(TAG, "onReceive: Time = " + intent.getIntExtra(C.ORIENTATION_SERVICE_TOTAL_TIME, -1));
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
