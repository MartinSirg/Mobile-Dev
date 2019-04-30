package com.masirg.orientation;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.masirg.orientation.Domain.Track;
import com.masirg.orientation.Domain.TrackCheckpoint;
import com.masirg.orientation.Domain.TrackPoint;
import com.masirg.orientation.Reposiotories.TrackCheckpointsRepository;
import com.masirg.orientation.Reposiotories.TrackPointsRepository;
import com.masirg.orientation.Reposiotories.TracksRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OrientationService extends Service implements LocationListener {

    private static final String TAG = OrientationService.class.getSimpleName();
    private LocationManager locationManager;
    private String provider;
    private OrientationServiceBroadcastReceiver mBroadcastReceiver;
    private ScheduledExecutorService mScheduledExecutorService;

    private boolean mCollectDataInBackground = false;

    private List<Location> mLocationsCache = new ArrayList<>();
    private List<Location> mCheckpointLocations = new ArrayList<>();

    private int mTimeSinceLastWaypoint = -1;
    private int mTimeSinceLastCheckpoint = -1;
    private int mTimeSinceStart = -1;

    private double mDistanceSinceLastWaypoint = -1;
    private double mDistanceSinceLastCheckpoint = -1;
    private double mDistanceSinceStart = -1;

    private Location startingLocation;
    private Location mLastWaypointLocation;
    private Location mLastLocation;
    private Track mTrack;

    private TrackPointsRepository mPointsRepository;
    private TracksRepository mTracksRepository;
    private TrackCheckpointsRepository mCheckpointsRepository;


    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Log.d(TAG, "onCreate: Provider = " + provider);

        createNotificationChannel();
        createNotification();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            onDestroy();
            Log.e(TAG, "onStartCommand: Missing permissions");
            return START_NOT_STICKY;
        }
        locationManager.requestLocationUpdates(provider, 1000, 1, this);

        mBroadcastReceiver = new OrientationServiceBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(C.MAPS_ACTIVITY_INTENT_COLLECT_IN_BACKGROUND);
        intentFilter.addAction(C.MAPS_ACTIVITY_INTENT_STOP_COLLECT_IN_BACKGROUND);
        intentFilter.addAction(C.MAPS_ACTIVITY_INTENT_ADD_CHECKPOINT);
        intentFilter.addAction(C.MAPS_ACTIVITY_INTENT_ADD_WAYPOINT);

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mBroadcastReceiver, intentFilter);

        TracksRepository repository = new TracksRepository(this);
        repository.open();
        mTrack = repository.add(new Track(System.currentTimeMillis() /  1000));
        repository.close();
        Log.d(TAG, "Track : " + mTrack.toString() );

        startTimer();
        mTracksRepository = new TracksRepository(this);
        mPointsRepository = new TrackPointsRepository(this);
        mCheckpointsRepository = new TrackCheckpointsRepository(this);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        if (locationManager != null) locationManager.removeUpdates(this);
        if (mScheduledExecutorService != null) mScheduledExecutorService.shutdown();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mBroadcastReceiver);

        mTrack.setTotalDistance(mDistanceSinceStart);
        mTrack.setTotalTime(mTimeSinceStart);
        mTrack.setDescription("-");

        mTracksRepository.open();
        mTracksRepository.update(mTrack);
        mTracksRepository.close();
    }

    //=================================================================
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //================================================================
    @Override
    public void onLocationChanged(Location location) {
        saveTrackPointToDb(location);
        if (startingLocation == null) {
            mDistanceSinceStart = 0;
            mTimeSinceStart = 0;
            startingLocation = location;
        }

        if (mLastWaypointLocation != null && mLastLocation != null){
           mDistanceSinceLastWaypoint += location.distanceTo(mLastLocation);
        }
        if (mLastLocation != null && mCheckpointLocations != null && mCheckpointLocations.size() > 0){
            mDistanceSinceLastCheckpoint += location.distanceTo(mLastLocation);
        }
        if (mLastLocation != null) {
            mDistanceSinceStart += location.distanceTo(mLastLocation);
        }



        if (mCollectDataInBackground) {
            Log.d(TAG, "onLocationChanged: SAVING LOCATION IN CACHE");
            mLocationsCache.add(location);
        } else {
            Log.d(TAG, "onLocationChanged: SENDING LOCATION");
            Intent sendLocationInfoIntent = new Intent(C.ORIENTATION_SERVICE_INTENT_LOCATION_UPDATE);
            sendLocationInfoIntent.putExtra(C.ORIENTATION_SERVICE_LATITUDE, location.getLatitude());
            sendLocationInfoIntent.putExtra(C.ORIENTATION_SERVICE_LONGITUDE, location.getLongitude());
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(sendLocationInfoIntent);
        }
        mLastLocation = location;
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
    //================================================================

    private void createNotificationChannel() {
        Log.d(TAG, "createNotificationChannel: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    C.NOTIFICATION_CHANNEL_1,
                    "Orientation App Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setSound(null, null);
            channel.setDescription("All notifications for Orientation app");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createNotification() {
        Log.d(TAG, "createNotification: ");
        Intent intent = new Intent(this, MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(this, C.NOTIFICATION_CHANNEL_1)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Title")
                .setContentText("Longer text")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

//        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        startForeground(2, notification);
    }

    //=================================================================

    private void startTimer(){
        if (mScheduledExecutorService != null) mScheduledExecutorService.shutdown();
        mScheduledExecutorService = Executors.newScheduledThreadPool(5);
        mScheduledExecutorService.scheduleAtFixedRate(() ->{
            if (mTimeSinceStart != -1) mTimeSinceStart++;
            if (mTimeSinceLastCheckpoint!= -1) mTimeSinceLastCheckpoint++;
            if (mTimeSinceLastWaypoint != -1) mTimeSinceLastWaypoint++;
            sendStatisticsUpdate();
        },0,1, TimeUnit.SECONDS);
    }

    private void sendStatisticsUpdate() {
        Log.d(TAG, "sendStatisticsUpdate: ");
        if (mLastLocation == null || startingLocation == null) return;
        if (mCollectDataInBackground) return;
        Intent intent = new Intent(C.ORIENTATION_SERVICE_INTENT_STATS_UPDATE);

        intent.putExtra(C.ORIENTATION_SERVICE_TOTAL_DISTANCE, mDistanceSinceStart);
        intent.putExtra(C.ORIENTATION_SERVICE_TOTAL_TIME, mTimeSinceStart);

        if (mCheckpointLocations != null && mCheckpointLocations.size() > 0){
            intent.putExtra(C.ORIENTATION_SERVICE_CHECKPOINT_DISTANCE, mDistanceSinceLastCheckpoint);
            double directDistance = mLastLocation
                    .distanceTo(mCheckpointLocations.get(mCheckpointLocations.size() - 1));

            intent.putExtra(C.ORIENTATION_SERVICE_CHECKPOINT_DIRECT_DISTANCE, directDistance);
            intent.putExtra(C.ORIENTATION_SERVICE_CHECKPOINT_TIME, mTimeSinceLastCheckpoint);
        }

        if (mLastWaypointLocation != null){
            intent.putExtra(C.ORIENTATION_SERVICE_WAYPOINT_DISTANCE, mDistanceSinceLastWaypoint);
            double directDistance = mLastLocation.distanceTo(mLastWaypointLocation);
            intent.putExtra(C.ORIENTATION_SERVICE_WAYPOINT_DIRECT_DISTANCE, directDistance);
            intent.putExtra(C.ORIENTATION_SERVICE_WAYPOINT_TIME, mTimeSinceLastWaypoint);
        }
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    //=================================================================

    private void saveTrackPointToDb(Location location) {
        TrackPoint trackPoint = new TrackPoint(
                mTrack.getTrackId(),
                location.getLatitude(),
                location.getLongitude(),
                location.getAltitude(),
                System.currentTimeMillis() / 1000
                );
        mPointsRepository.open();
        long id = mPointsRepository.add(trackPoint);
        mPointsRepository.close();

        Log.d(TAG, "saveTrackPointToDb: id = " + id);
    }

    private void saveCheckpointToDb(Location location){
        TrackCheckpoint checkpoint = new TrackCheckpoint(
                mTrack.getTrackId(),
                location.getLatitude(),
                location.getLongitude(),
                location.getAltitude(),
                System.currentTimeMillis() / 1000
        );
        mCheckpointsRepository.open();
        long id = mCheckpointsRepository.add(checkpoint);
        mCheckpointsRepository.close();

        Log.d(TAG, "saveCheckpointToDb: id = " + id);
    }

    //===============================================================
    public class OrientationServiceBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location location = locationManager.getLastKnownLocation(provider);

            switch (intent.getAction()){
                case C.MAPS_ACTIVITY_INTENT_COLLECT_IN_BACKGROUND:
                    Log.d(TAG, "onReceive: Starting background collection");
                    mCollectDataInBackground = true;
                    break;

                case C.MAPS_ACTIVITY_INTENT_STOP_COLLECT_IN_BACKGROUND:
                    Log.d(TAG, "onReceive: Sending all locations");
                    mCollectDataInBackground = false;

                    double[] latitudes = new double[mLocationsCache.size()];
                    double[] longitudes = new double[mLocationsCache.size()];
                    for (int i = 0; i < mLocationsCache.size(); i++) {
                        latitudes[i] = mLocationsCache.get(i).getLatitude();
                        longitudes[i] = mLocationsCache.get(i).getLongitude();
                    }
                    mLocationsCache.clear();

                    Intent sendIntent = new Intent(C.ORIENTATION_SERVICE_INTENT_BACKGROUND_LOCATIONS_UPDATE);
                    sendIntent.putExtra(C.ORIENTATION_SERVICE_LATITUDES, latitudes);
                    sendIntent.putExtra(C.ORIENTATION_SERVICE_LONGITUDES, longitudes);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(sendIntent);
                    break;

                case C.MAPS_ACTIVITY_INTENT_ADD_CHECKPOINT:
                    mCheckpointLocations.add(location);
                    saveCheckpointToDb(location);
                    mDistanceSinceLastCheckpoint = 0;
                    mTimeSinceLastCheckpoint = 0;

                    Intent checkpointIntent = new Intent(C.ORIENTATION_SERVICE_INTENT_NEW_CHECKPOINT);
                    checkpointIntent.putExtra(C.ORIENTATION_SERVICE_LATITUDE, location.getLatitude());
                    checkpointIntent.putExtra(C.ORIENTATION_SERVICE_LONGITUDE, location.getLongitude());
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(checkpointIntent);
                    break;

                case C.MAPS_ACTIVITY_INTENT_ADD_WAYPOINT:
                    mLastWaypointLocation = location;
                    mDistanceSinceLastWaypoint = 0;
                    mTimeSinceLastWaypoint = 0;

                    Intent waypointIntent = new Intent(C.ORIENTATION_SERVICE_INTENT_NEW_WAYPOINT);
                    waypointIntent.putExtra(C.ORIENTATION_SERVICE_LATITUDE, location.getLatitude());
                    waypointIntent.putExtra(C.ORIENTATION_SERVICE_LONGITUDE, location.getLongitude());
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(waypointIntent);
                    break;
            }
        }
    }
}
