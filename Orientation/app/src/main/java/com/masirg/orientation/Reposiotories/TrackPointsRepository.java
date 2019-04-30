package com.masirg.orientation.Reposiotories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.masirg.orientation.DatabaseHelper;
import com.masirg.orientation.Domain.Track;
import com.masirg.orientation.Domain.TrackPoint;

import java.util.ArrayList;
import java.util.List;

public class TrackPointsRepository {
    private static final String TAG = TrackPointsRepository.class.getSimpleName();
    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase db;

    public TrackPointsRepository(Context context) {
        this.context = context;
    }

    public TrackPointsRepository open() {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public long add(TrackPoint trackPoint){
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TRACK_POINT_TRACK_ID, trackPoint.getTrackId());
        values.put(DatabaseHelper.TRACK_POINT_LATITUDE, trackPoint.getLatitude());
        values.put(DatabaseHelper.TRACK_POINT_LONGITUDE, trackPoint.getLongitude());
        values.put(DatabaseHelper.TRACK_POINT_ALTITUDE, trackPoint.getAltitude());
        values.put(DatabaseHelper.TRACK_POINT_TIME, trackPoint.getTime());

        return db.insert(DatabaseHelper.TRACK_POINTS_TABLE_NAME, null, values);
    }

    public List<TrackPoint> getAllTrackPoints(long trackId) {
        ArrayList<TrackPoint> trackPoints = new ArrayList<>();
        String whereString = DatabaseHelper.TRACK_POINT_TRACK_ID + " = ?";
        String[] args = {Long.toString(trackId)};
        Cursor cursor = fetchCursor(whereString, args);

        if (cursor.moveToFirst()) {
            do {
                TrackPoint trackPoint = new TrackPoint(
                        cursor.getLong(cursor.getColumnIndex(DatabaseHelper.TRACK_POINT_ID)),
                        cursor.getLong(cursor.getColumnIndex(DatabaseHelper.TRACK_POINT_TRACK_ID)),
                        cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.TRACK_POINT_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.TRACK_POINT_LONGITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.TRACK_POINT_ALTITUDE)),
                        cursor.getLong(cursor.getColumnIndex(DatabaseHelper.TRACK_POINT_TIME))
                );
                trackPoints.add(trackPoint);
            } while (cursor.moveToNext());
        }
        Log.d(TAG, "getAllTrackPoints: found " + trackPoints.size() + " track points");
        return trackPoints;
    }

    private Cursor fetchCursor(String whereString, String[] whereArgs) {
        String[] columns = new String[]{
                DatabaseHelper.TRACK_POINT_ID,
                DatabaseHelper.TRACK_POINT_TRACK_ID,
                DatabaseHelper.TRACK_POINT_LATITUDE,
                DatabaseHelper.TRACK_POINT_LONGITUDE,
                DatabaseHelper.TRACK_POINT_ALTITUDE,
                DatabaseHelper.TRACK_POINT_TIME
        };

        Cursor cursor = db.query(DatabaseHelper.TRACK_POINTS_TABLE_NAME, columns,
                whereString, whereArgs, null, null, DatabaseHelper.TRACK_POINT_ID);

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }
}
