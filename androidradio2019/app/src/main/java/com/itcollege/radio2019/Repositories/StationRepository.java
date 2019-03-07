package com.itcollege.radio2019.Repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.itcollege.radio2019.DatabaseHelper;
import com.itcollege.radio2019.Domain.Station;
import com.itcollege.radio2019.R;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StationRepository {
    private static final String TAG = StationRepository.class.getSimpleName();
    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase db;

    public StationRepository(Context context) {
        this.context = context;
    }

    public StationRepository open() {
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void add(Station station) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.STATION_NAME, station.getName());
        values.put(DatabaseHelper.STATION_STREAM_URL, station.getStreamUrl());
        values.put(DatabaseHelper.STATION_SONG_NAME_API_URL, station.getSongNameApiUrl());

        db.insert(DatabaseHelper.STATION_TABLE_NAME, null, values);
    }

    public List<Station> getAll() {
        ArrayList<Station> stations = new ArrayList<>();
        Cursor cursor = fetch();

        if (cursor.moveToFirst()) {
            do {
                Station s = new Station(
                        cursor.getInt(cursor.getColumnIndex(DatabaseHelper.STATION_ID)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.STATION_NAME)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.STATION_STREAM_URL)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.STATION_SONG_NAME_API_URL)));
                stations.add(s);
            } while (cursor.moveToNext());
        }
        return stations;
    }

    public List<Station> getAllOrInitialize() {
        Log.d(TAG, "getAllOrInitialize");
        List<Station> stations;
        stations = getAll();
        if (stations.size() == 0) {
            Log.d(TAG, "getAllOrInitialize: Inserting");
            insertAllStations();
            stations = getAll();
        }
        return stations;
    }


    private Cursor fetch() {
        String[] columns = new String[]{
                DatabaseHelper.STATION_ID,
                DatabaseHelper.STATION_NAME,
                DatabaseHelper.STATION_STREAM_URL,
                DatabaseHelper.STATION_SONG_NAME_API_URL,
        };

        Cursor cursor = db.query(DatabaseHelper.STATION_TABLE_NAME, columns,
                null, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public void insertAllStations() {
        InputStream inputStream = context.getResources().openRawResource(R.raw.stations);
        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
        String jsonString = s.hasNext() ? s.next() : "";

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                Station station = new Station(
                        jsonObject.getString("name"),
                        jsonObject.getString("streamUrl"),
                        jsonObject.getString("songNameApiUrl"));
                Log.d(TAG, "insertAllStations: found station :\n " + station.toString());
                add(station);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
