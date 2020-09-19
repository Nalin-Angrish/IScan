package com.nalinstudios.iscan.scanlibrary;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * A class to generate a dialog with a single button.
 */
@SuppressLint("ValidFragment")
public class SingleButtonDialogFragment extends DialogFragment {

    /** The title of the positive button */
    protected int positiveButtonTitle;
    /** The message to be given to the user.*/
    protected String message;
    /** The title of the notification*/
    protected String title;
    /** Whether or not the notification is cancellable*/
    protected boolean isCancelable;

    /**
     * The constructor of the notification (this only sets the local variables)
     * @param positiveButtonTitle The title of the positive button
     * @param message The message to be given to the user.
     * @param title The title of the notification
     * @param isCancelable Whether or not the notification is cancellable
     */
    public SingleButtonDialogFragment(int positiveButtonTitle, String message, String title, boolean isCancelable) {
        this.positiveButtonTitle = positiveButtonTitle;
        this.message = message;
        this.title = title;
        this.isCancelable = isCancelable;
    }


    /**
     * The main function to create the dialog
     * @param savedInstanceState the saved instance (not required)
     * @return the generated Dialog.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setCancelable(isCancelable)
                .setMessage(message)
                .setPositiveButton(positiveButtonTitle,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {

                            }
                        });

        return builder.create();
    }
}