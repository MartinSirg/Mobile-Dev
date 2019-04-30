package com.masirg.orientation;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "TRACKS_DB";
    private static final int DB_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TRACKS_TABLE);
        db.execSQL(CREATE_TRACK_POINTS_TABLE);
        db.execSQL(CREATE_TRACK_CHECKPOINTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TRACKS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TRACK_POINTS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TRACK_CHECKPOINT_TABLE_NAME);
        onCreate(db);
    }

    //---------------------------------Tracks---------------------------------
    public static final String TRACKS_TABLE_NAME = "TRACKS";
    public static final String TRACK_ID = "trackId";
    public static final String TRACK_CREATION_TIME = "trackCreationTime";
    public static final String TRACK_DESCRIPTION = "trackDescription";
    public static final String TRACK_TOTAL_DISTANCE = "trackTotalDistance";
    public static final String TRACK_TOTAL_TIME = "trackTotalTime";

    private static final String CREATE_TRACKS_TABLE = "CREATE TABLE " + TRACKS_TABLE_NAME +
            "(" +
            TRACK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TRACK_CREATION_TIME + " INTEGER NOT NULL, " +
            TRACK_TOTAL_DISTANCE + " REAL NOT NULL , " +
            TRACK_TOTAL_TIME + " INTEGER NOT NULL, " +
            TRACK_DESCRIPTION + " TEXT);";

    //---------------------------------TrackPoints---------------------------------
    public static final String TRACK_POINTS_TABLE_NAME = "trackPoints";
    public static final String TRACK_POINT_ID = "trackPointId";
    public static final String TRACK_POINT_TRACK_ID = "trackPointTrackId";
    public static final String TRACK_POINT_LATITUDE = "latitude";
    public static final String TRACK_POINT_LONGITUDE = "longitude";
    public static final String TRACK_POINT_ALTITUDE = "altitude";
    public static final String TRACK_POINT_TIME = "time";

    private static final String CREATE_TRACK_POINTS_TABLE = "CREATE TABLE " + TRACK_POINTS_TABLE_NAME +
            "(" +
            TRACK_POINT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TRACK_POINT_TRACK_ID + " INTEGER NOT NULL, " +
            TRACK_POINT_LATITUDE + " REAL NOT NULL," +
            TRACK_POINT_LONGITUDE + " REAL NOT NULL, " +
            TRACK_POINT_ALTITUDE + " REAL NOT NULL, " +
            TRACK_POINT_TIME     + " INTEGER NOT NULL);";

    //---------------------------------TrackCheckpoints---------------------------------
    public static final String TRACK_CHECKPOINT_TABLE_NAME = "trackCheckpoints";
    public static final String TRACK_CHECKPOINT_ID = "trackCheckpointId";
    public static final String TRACK_CHECKPOINT_TRACK_ID = "trackCheckpointTrackId";
    public static final String TRACK_CHECKPOINT_LATITUDE = "latitude";
    public static final String TRACK_CHECKPOINT_LONGITUDE = "longitude";
    public static final String TRACK_CHECKPOINT_ALTITUDE = "altitude";
    public static final String TRACK_CHECKPOINT_TIME = "time";

    private static final String CREATE_TRACK_CHECKPOINTS_TABLE = "CREATE TABLE " + TRACK_CHECKPOINT_TABLE_NAME +
            "(" +
            TRACK_CHECKPOINT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TRACK_CHECKPOINT_TRACK_ID + " INTEGER NOT NULL, " +
            TRACK_CHECKPOINT_LATITUDE     + " REAL NOT NULL, " +
            TRACK_CHECKPOINT_LONGITUDE + " REAL NOT NULL, " +
            TRACK_CHECKPOINT_ALTITUDE + " REAL NOT NULL, " +
            TRACK_CHECKPOINT_TIME + " INTEGER NOT NULL);";
}
