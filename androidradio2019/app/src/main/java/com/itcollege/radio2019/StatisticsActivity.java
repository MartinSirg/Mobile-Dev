package com.itcollege.radio2019;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.itcollege.radio2019.Domain.Station;

import java.util.List;

public class StatisticsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = StatisticsActivity.class.getSimpleName();

    List<Station> mStations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        Log.d(TAG, "onCreate: ");

        Spinner spinnerStations = findViewById(R.id.spinnerStation);

        Bundle bundle = getIntent().getExtras().getBundle(C.SERIALIZABLE_STATIONS_BUNDLE);
        mStations =  (List<Station>) bundle.getSerializable(C.SERIALIZABLE_STATIONS);

        ArrayAdapter<Station> adapter = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                mStations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStations.setAdapter(adapter);

        spinnerStations.setOnItemSelectedListener(this);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemSelected: " + mStations.get(position).toString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    // ============================== LIFECYCLE EVENTS ===============================
    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }


    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }


    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart");
        super.onRestart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void buttonSearchOnClick(View view) {
        Log.d(TAG, "buttonSearchOnClick: ");
    }
}
