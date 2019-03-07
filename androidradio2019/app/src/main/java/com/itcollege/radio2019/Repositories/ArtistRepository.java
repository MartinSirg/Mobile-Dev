package com.itcollege.radio2019.Repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.itcollege.radio2019.DatabaseHelper;
import com.itcollege.radio2019.Domain.Artist;

import java.util.ArrayList;
import java.util.List;

public class ArtistRepository {
    private static final String TAG = ArtistRepository.class.getSimpleName();
    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase db;

    public ArtistRepository(Context context) {
        this.context = context;
    }

    public ArtistRepository open() {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        return this;
    }


    /**
     * @param artist artist object, id can be 0
     */
    public int getArtistIdOrInsert(Artist artist){
        Log.d(TAG, "getArtistIdOrInsert");

        String[] columns = new String[]{
                DatabaseHelper.ARTIST_ID,
                DatabaseHelper.ARTIST_NAME,
                DatabaseHelper.ARTIST_STATION_ID
        };

        //escapes ' for sql
        String artistName = artist.getArtistName().replace("'", "''");

        //Replaces whitespaces with empty strings
        String whereString = "REPLACE(" + DatabaseHelper.ARTIST_NAME + ", ' ', '') = REPLACE('" + artistName + "', ' ', '') and " +
                DatabaseHelper.ARTIST_STATION_ID + " = " + artist.getStationId();

        Cursor cursor = db.query(DatabaseHelper.ARTIST_TABLE_NAME, columns, whereString,
                null, null, null, null);

        if (cursor.moveToFirst()){
            int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.ARTIST_ID));
            cursor.close();
            return id;
        } else {
            cursor.close();
            return add(artist);
        }
    }

    public void close() {
        dbHelper.close();
    }

    public int add(Artist artist) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.ARTIST_NAME, artist.getArtistName());
        values.put(DatabaseHelper.ARTIST_STATION_ID, artist.getStationId());

        long id = db.insert(DatabaseHelper.ARTIST_TABLE_NAME, null, values);
        return (int) id;
    }

    public List<Artist> getAll() {
        ArrayList<Artist> artists = new ArrayList<>();
        Cursor cursor = fetch();

        if (cursor.moveToFirst()) {
            do {
                Artist artist = new Artist(
                        cursor.getInt(cursor.getColumnIndex(DatabaseHelper.ARTIST_ID)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.ARTIST_NAME)),
                        cursor.getInt(cursor.getColumnIndex(DatabaseHelper.ARTIST_STATION_ID)));
                artists.add(artist);
            } while (cursor.moveToNext());
        }
        return artists;
    }


    private Cursor fetch() {
        String[] columns = new String[]{
                DatabaseHelper.ARTIST_ID,
                DatabaseHelper.ARTIST_NAME,
                DatabaseHelper.ARTIST_STATION_ID
        };

        Cursor cursor = db.query(DatabaseHelper.ARTIST_TABLE_NAME, columns,
                null, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }
}
