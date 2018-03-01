package com.ghostchippies.spellblasterforandroidv44;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;


public class SubmitDetailsDialog extends DialogFragment {

    interface NoticeDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
    }

    NoticeDialogListener mListener;

    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder buildDialog = new AlertDialog.Builder(getActivity());
        buildDialog.setTitle("Submit your score to Twitter?");

        View.inflate(getActivity(), com.ghostchippies.spellblaster.R.layout.dialog_details, null);
                buildDialog.setPositiveButton(com.ghostchippies.spellblaster.R.string.submit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(SubmitDetailsDialog.this);
                    }
                });
                buildDialog.setNegativeButton(com.ghostchippies.spellblaster.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });
        // Create the AlertDialog object and return it
        return buildDialog.create();
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }
}
