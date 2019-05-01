package com.masirg.orientation.Reposiotories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.masirg.orientation.DatabaseHelper;
import com.masirg.orientation.Domain.Track;

import java.util.ArrayList;
import java.util.List;

public class TracksRepository {
    private static final String TAG = TracksRepository.class.getSimpleName();
    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase db;

    public TracksRepository(Context context) {
        this.context = context;
    }

    public TracksRepository open() {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public Track add(Track track){
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TRACK_CREATION_TIME, track.getCreationTime());
        values.put(DatabaseHelper.TRACK_DESCRIPTION, track.getDescription());
        values.put(DatabaseHelper.TRACK_TOTAL_TIME, track.getTotalTime());
        values.put(DatabaseHelper.TRACK_TOTAL_DISTANCE, track.getTotalDistance());

        long id = db.insert(DatabaseHelper.TRACKS_TABLE_NAME, null, values);
        track.setTrackId(id);

        return track;
    }

    public void update(Track track){
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TRACK_DESCRIPTION, track.getDescription());
        values.put(DatabaseHelper.TRACK_TOTAL_TIME, track.getTotalTime());
        values.put(DatabaseHelper.TRACK_TOTAL_DISTANCE, track.getTotalDistance());

        String selection = DatabaseHelper.TRACK_ID + " = ?";
        String[] selectionArgs = { Long.toString(track.getTrackId()) };

        int x = db.update(DatabaseHelper.TRACKS_TABLE_NAME, values, selection, selectionArgs);
        if (x == 0){
            Log.d(TAG, "update: FAILED");
        } else if (x == 1){
            Log.d(TAG, "update: SUCCESS");
        } else {
            Log.d(TAG, "update: ERROR, more than one update");
        }
    }

    public Track get(long trackId) {
        String whereString = DatabaseHelper.TRACK_ID + " = ?";
        String[] args = {Long.toString(trackId)};
        Cursor cursor = fetchCursor(whereString, args);

        if (cursor.moveToFirst()) {
            Track track = new Track(
                    cursor.getLong(cursor.getColumnIndex(DatabaseHelper.TRACK_ID)),
                    cursor.getLong(cursor.getColumnIndex(DatabaseHelper.TRACK_CREATION_TIME)),
                    cursor.getString(cursor.getColumnIndex(DatabaseHelper.TRACK_DESCRIPTION)),
                    cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.TRACK_TOTAL_DISTANCE)),
                    cursor.getLong(cursor.getColumnIndex(DatabaseHelper.TRACK_TOTAL_TIME)));
            Log.d(TAG, "get: Found track " + track);
            return track;
        } else {
            Log.d(TAG, "get: Track not found");
            return null;
        }
    }

    public List<Track> getAll() {
        ArrayList<Track> tracks = new ArrayList<>();
        String whereString = DatabaseHelper.TRACK_TOTAL_DISTANCE + " > ?";
        String[] args = {Long.toString(0)};
        Cursor cursor = fetchCursor(whereString, args);

        if (cursor.moveToFirst()) {
            do {
                Track track = new Track(
                        cursor.getLong(cursor.getColumnIndex(DatabaseHelper.TRACK_ID)),
                        cursor.getLong(cursor.getColumnIndex(DatabaseHelper.TRACK_CREATION_TIME)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.TRACK_DESCRIPTION)),
                        cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.TRACK_TOTAL_DISTANCE)),
                        cursor.getLong(cursor.getColumnIndex(DatabaseHelper.TRACK_TOTAL_TIME))
                );
                Log.d(TAG, "getAll: Found track " + track);
                tracks.add(track);
            } while (cursor.moveToNext());
        }else {
            Log.d(TAG, "getAll: didn't find any");
        }
        return tracks;
    }

    private Cursor fetchCursor(String whereString, String[] whereArgs) {
        String[] columns = new String[]{
                DatabaseHelper.TRACK_ID,
                DatabaseHelper.TRACK_CREATION_TIME,
                DatabaseHelper.TRACK_TOTAL_DISTANCE,
                DatabaseHelper.TRACK_TOTAL_TIME,
                DatabaseHelper.TRACK_DESCRIPTION
        };

        Cursor cursor = db.query(DatabaseHelper.TRACKS_TABLE_NAME, columns,
                whereString, whereArgs, null, null, DatabaseHelper.TRACK_ID);

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public void delete(long trackId) {
        String whereString = DatabaseHelper.TRACK_ID + " = ?";
        String[] args = {Long.toString(trackId)};

        int deletedRows = db.delete(DatabaseHelper.TRACKS_TABLE_NAME, whereString, args);
        if (deletedRows == 0){
            Log.d(TAG, "delete: FAILED");
        } else if (deletedRows == 1){
            Log.d(TAG, "delete: SUCCESS");
        } else {
            Log.d(TAG, "delete: ERROR, more than one deletion");
        }
    }
}
