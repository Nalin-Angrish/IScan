package com.nalinstudios.iscan.scanlibrary;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.view.KeyEvent;

/**
 * A class to show progress bar.
 */
@SuppressLint("ValidFragment")
public class ProgressDialogFragment extends DialogFragment {

    /** The message to be shown */
    public String message;

    /** A constructor to set the message of the dialog*/
    public ProgressDialogFragment(String message) {
        this.message = message;
    }

    /**
     * The function to initiate the formation of the ProgressBar
     * @param savedInstanceState the save state of the bar (not used)
     * @return the generated dialog.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setIndeterminate(true);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        // Disable the back button
        OnKeyListener keyListener = new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_BACK;
            }

        };
        dialog.setOnKeyListener(keyListener);
        return dialog;
    }
}