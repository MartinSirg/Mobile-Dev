package com.masirg.orientation.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

public class ConfirmStopDialog extends AppCompatDialogFragment {
    private ConfirmStopDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = (ConfirmStopDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getActivity().getClass().toString()
                    + " must implement ConfirmStopDialogListener");
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // 1. Instantiate an <code><a href="/reference/android/app/AlertDialog.Builder.html">AlertDialog.Builder</a></code> with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage("Are you sure you want to stop this session")
                .setTitle("stop session")
                .setPositiveButton("Stop", (dialog, which) -> listener.OnStopConfirmedClicked())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // 3. Get the <code><a href="/reference/android/app/AlertDialog.html">AlertDialog</a></code> from <code><a href="/reference/android/app/AlertDialog.Builder.html#create()">create()</a></code>
        return builder.create();
    }

    public interface ConfirmStopDialogListener{
        void OnStopConfirmedClicked();
    }
}
