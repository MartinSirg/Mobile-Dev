package com.masirg.orientation.Reposiotories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.masirg.orientation.DatabaseHelper;
import com.masirg.orientation.Domain.TrackCheckpoint;
import com.masirg.orientation.Domain.TrackPoint;

import java.util.ArrayList;
import java.util.List;

public class TrackCheckpointsRepository {
    private static final String TAG = TrackCheckpointsRepository.class.getSimpleName();
    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase db;

    public TrackCheckpointsRepository(Context context) {
        this.context = context;
    }

    public TrackCheckpointsRepository open() {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public long add(TrackCheckpoint trackCheckpoint){
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TRACK_CHECKPOINT_TRACK_ID, trackCheckpoint.getTrackId());
        values.put(DatabaseHelper.TRACK_CHECKPOINT_LATITUDE, trackCheckpoint.getLatitude());
        values.put(DatabaseHelper.TRACK_CHECKPOINT_LONGITUDE, trackCheckpoint.getLongitude());
        values.put(DatabaseHelper.TRACK_CHECKPOINT_ALTITUDE, trackCheckpoint.getAltitude());
        values.put(DatabaseHelper.TRACK_CHECKPOINT_TIME, trackCheckpoint.getTime());

        return db.insert(DatabaseHelper.TRACK_CHECKPOINT_TABLE_NAME, null, values);
    }

    public List<TrackCheckpoint> getAllTrackCheckpoints(long trackId) {
        ArrayList<TrackCheckpoint> checkpoints = new ArrayList<>();
        String whereString = DatabaseHelper.TRACK_CHECKPOINT_TRACK_ID + " = ?";
        String[] args = {Long.toString(trackId)};
        Cursor cursor = fetchCursor(whereString, args);

        if (cursor.moveToFirst()) {
            do {
                TrackCheckpoint checkpoint = new TrackCheckpoint(
                        cursor.getLong(cursor.getColumnIndex(DatabaseHelper.TRACK_CHECKPOINT_ID)),
                        cursor.getLong(cursor.getColumnIndex(DatabaseHelper.TRACK_CHECKPOINT_TRACK_ID)),
                        cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.TRACK_CHECKPOINT_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.TRACK_CHECKPOINT_LONGITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.TRACK_CHECKPOINT_ALTITUDE)),
                        cursor.getLong(cursor.getColumnIndex(DatabaseHelper.TRACK_CHECKPOINT_TIME))
                );
                checkpoints.add(checkpoint);
            } while (cursor.moveToNext());
        }
        Log.d(TAG, "getAllTrackCheckpoints: found " + checkpoints.size() + " checkpoints");
        return checkpoints;
    }

    private Cursor fetchCursor(String whereString, String[] whereArgs) {
        String[] columns = new String[]{
                DatabaseHelper.TRACK_CHECKPOINT_ID,
                DatabaseHelper.TRACK_CHECKPOINT_TRACK_ID,
                DatabaseHelper.TRACK_CHECKPOINT_LATITUDE,
                DatabaseHelper.TRACK_CHECKPOINT_LONGITUDE,
                DatabaseHelper.TRACK_CHECKPOINT_ALTITUDE,
                DatabaseHelper.TRACK_CHECKPOINT_TIME
        };

        Cursor cursor = db.query(DatabaseHelper.TRACK_CHECKPOINT_TABLE_NAME, columns,
                whereString, whereArgs, null, null, DatabaseHelper.TRACK_CHECKPOINT_ID);

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public void deleteTrackCheckpoints(long trackId) {
        String whereString = DatabaseHelper.TRACK_CHECKPOINT_TRACK_ID + " = ?";
        String[] args = {Long.toString(trackId)};

        int deletedRows = db.delete(DatabaseHelper.TRACK_CHECKPOINT_TABLE_NAME, whereString, args);
        Log.d(TAG, "deleteTrackCheckpoints: Deleted " + deletedRows + " checkpoints");
    }
}
