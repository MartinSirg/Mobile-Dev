package com.itcollege.radio2019;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "RADIO_DB";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_STATION_TABLE);
        db.execSQL(CREATE_ARTIST_TABLE);
        db.execSQL(CREATE_SONG_PLAYED_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ARTIST_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + STATION_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SONG_PLAYED_TABLE_NAME);
        onCreate(db);
    }

    //---------------------------------Artist---------------------------------
    public static final String ARTIST_TABLE_NAME = "ARTISTS";
    public static final String ARTIST_ID = "artistId";
    public static final String ARTIST_NAME = "artistName";
    public static final String ARTIST_STATION_ID = "stationId";

    private static final String CREATE_ARTIST_TABLE = "CREATE TABLE " + ARTIST_TABLE_NAME +
            "(" +
            ARTIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ARTIST_NAME + " TEXT NOT NULL, " +
            ARTIST_STATION_ID + " INTEGER NOT NULL);";

    //---------------------------------SongPlayed---------------------------------
    public static final String SONG_PLAYED_TABLE_NAME = "songsPlayed";
    public static final String SONG_PLAYED_ID = "songPlayedId";
    public static final String SONG_PLAYED_TITLE = "songTitle";
    public static final String SONG_PLAYED_ARTIST_ID = "artistId";
    public static final String SONG_PLAYED_TIME_PLAYED = "timePlayedAt";

    private static final String CREATE_SONG_PLAYED_TABLE = "CREATE TABLE " + SONG_PLAYED_TABLE_NAME +
            "(" +
            SONG_PLAYED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            SONG_PLAYED_TITLE + " TEXT NOT NULL, " +
            SONG_PLAYED_ARTIST_ID + " INTEGER NOT NULL, " +
            SONG_PLAYED_TIME_PLAYED + " INTEGER NOT NULL);";

    //---------------------------------Station---------------------------------
    public static final String STATION_TABLE_NAME = "STATIONS";
    public static final String STATION_ID = "stationId";
    public static final String STATION_NAME = "stationName";
    public static final String STATION_STREAM_URL = "streamUrl";
    public static final String STATION_SONG_NAME_API_URL = "songNameApiUrl";

    private static final String CREATE_STATION_TABLE = "CREATE TABLE " + STATION_TABLE_NAME +
            "(" +
            STATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            STATION_NAME + " TEXT NOT NULL, " +
            STATION_STREAM_URL + " TEXT NOT NULL, " +
            STATION_SONG_NAME_API_URL + " TEXT NOT NULL);";
}
