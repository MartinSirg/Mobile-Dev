package com.itcollege.radio2019.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.itcollege.radio2019.C;
import com.itcollege.radio2019.Domain.Station;
import com.itcollege.radio2019.R;

import java.util.List;

public class StationsDialog extends DialogFragment {
    private static final String TAG = StationsDialog.class.getSimpleName();

    private List<Station> stations;
    private StationDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String[] stationNames = new String[stations.size()];
        for (int i = 0; i < stations.size(); i++) {
            stationNames[i] = stations.get(i).getName();
        }

        builder.setTitle(getString(R.string.StationsDialogTitle))
                .setItems(stationNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Clicked on : " + stationNames[which]);
                        mListener.changeCurrentStation(which);
                    }
                });


        return builder.create();
    }

    @Override
    public void setArguments(Bundle args) {
        stations = (List<Station>) args.getSerializable(C.SERIALIZABLE_STATIONS);
        super.setArguments(args);
    }


    public interface StationDialogListener {
        void changeCurrentStation(int stationIndex);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (StationDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " Must implement DialogListener") ;
        }
    }
}
