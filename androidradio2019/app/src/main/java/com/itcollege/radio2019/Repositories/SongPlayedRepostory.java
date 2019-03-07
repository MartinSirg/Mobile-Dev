package com.itcollege.radio2019.Repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.itcollege.radio2019.DatabaseHelper;
import com.itcollege.radio2019.Domain.SongPlayed;

import java.util.ArrayList;
import java.util.List;

public class SongPlayedRepostory {
    private static final String TAG = ArtistRepository.class.getSimpleName();
    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase db;

    public SongPlayedRepostory(Context context) {
        this.context = context;
    }

    public SongPlayedRepostory open() {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void add(SongPlayed songPlayed) {
        if (entryExists(songPlayed)) return;

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.SONG_PLAYED_TITLE, songPlayed.getSongTitle());
        contentValues.put(DatabaseHelper.SONG_PLAYED_ARTIST_ID, songPlayed.getArtistId());
        contentValues.put(DatabaseHelper.SONG_PLAYED_TIME_PLAYED, songPlayed.getTimePlayedAt());

        db.insert(DatabaseHelper.SONG_PLAYED_TABLE_NAME, null, contentValues);
    }

    public List<SongPlayed> getAll() {
        ArrayList<SongPlayed> songsPlayed = new ArrayList<>();
        Cursor cursor = fetch();

        if (cursor.moveToFirst()) {
            do {
                SongPlayed songPlayed = new SongPlayed(
                        cursor.getInt(cursor.getColumnIndex(DatabaseHelper.SONG_PLAYED_ID)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.SONG_PLAYED_TITLE)),
                        cursor.getInt(cursor.getColumnIndex(DatabaseHelper.SONG_PLAYED_ARTIST_ID)),
                        cursor.getInt(cursor.getColumnIndex(DatabaseHelper.SONG_PLAYED_TIME_PLAYED)));
                songsPlayed.add(songPlayed);
            } while (cursor.moveToNext());
        }
        return songsPlayed;
    }


    private boolean entryExists(SongPlayed songPlayed) {
        String songTitle = songPlayed.getSongTitle().replace("'", "''");

        String whereString = "REPLACE(" + DatabaseHelper.SONG_PLAYED_TITLE + ", ' ', '') = REPLACE('" + songTitle + "', ' ', '') and " +
                DatabaseHelper.SONG_PLAYED_ARTIST_ID + " = " + songPlayed.getArtistId() + " and " +
                DatabaseHelper.SONG_PLAYED_TIME_PLAYED + " = " + songPlayed.getTimePlayedAt();

        String[] columns = new String[]{
                DatabaseHelper.SONG_PLAYED_TITLE,
                DatabaseHelper.SONG_PLAYED_ARTIST_ID,
                DatabaseHelper.SONG_PLAYED_TIME_PLAYED
        };


        Cursor cursor = db.query(DatabaseHelper.SONG_PLAYED_TABLE_NAME, columns, whereString,
                null, null, null, null);
        boolean objectExists = false;
        if (cursor.moveToFirst()) {
            objectExists = true;
        }
        cursor.close();
        return objectExists;
    }


    public Cursor fetch() {
        String[] columns = new String[]{
                DatabaseHelper.SONG_PLAYED_ID,
                DatabaseHelper.SONG_PLAYED_TITLE,
                DatabaseHelper.SONG_PLAYED_ARTIST_ID,
                DatabaseHelper.SONG_PLAYED_TIME_PLAYED
        };

        Cursor cursor = db.query(DatabaseHelper.SONG_PLAYED_TABLE_NAME, columns,
                null, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }


}
