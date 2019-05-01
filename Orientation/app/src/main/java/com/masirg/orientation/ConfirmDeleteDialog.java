package com.masirg.orientation;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;

public class ConfirmDeleteDialog extends AppCompatDialogFragment {

    private ConfirmDeleteDialogListener listener;
    private long trackId = -1;
    private int position = -1;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (ConfirmDeleteDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getActivity().getClass().toString()
                    + " must implement ConfirmDeleteDialogListener");
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (trackId == -1) throw new NullPointerException("Please set the trackId before showing fragment");
        if (position == -1) throw new NullPointerException("Please set the position before showing fragment");

        // 1. Instantiate an <code><a href="/reference/android/app/AlertDialog.Builder.html">AlertDialog.Builder</a></code> with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage("Are you sure you want to delete this track")
                .setTitle("Delete track")
                .setPositiveButton("Delete", (dialog, which) -> listener.OnDeleteConfirmedClicked(trackId, position))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // 3. Get the <code><a href="/reference/android/app/AlertDialog.html">AlertDialog</a></code> from <code><a href="/reference/android/app/AlertDialog.Builder.html#create()">create()</a></code>
        return builder.create();
    }

    public ConfirmDeleteDialog setTrackId(long trackId) {
        this.trackId = trackId;
        return this;
    }

    public ConfirmDeleteDialog setPosition(int position) {
        this.position = position;
        return this;
    }

    public interface ConfirmDeleteDialogListener{
        void OnDeleteConfirmedClicked(long trackId, int position);
    }


}
