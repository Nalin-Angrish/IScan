package com.nalinstudios.iscan.scanlibrary;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.nalinstudios.iscan.EditViewActivity;
import com.nalinstudios.iscan.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * A customized version of ResultFragment
 * @author Nalin Angrish, Jhansi.
 */
public class ResultFragment extends Fragment{

    /** The root view of the fragment*/
    private View view;
    /** The imageView containing the image to be formatted*/
    private ImageView scannedImageView;
    /** A bitmap to keep in memory the original picture */
    private Bitmap original;
    /** The bitmap that will be shown to the user*/
    private Bitmap transformed;
    /** The bitmap to keep in memory the original rotation of the image*/
    private Bitmap rotoriginal;
    /** The Fragment to show loading when any operation is being performed*/
    private static ProgressDialogFragment progressDialogFragment;
    /** A variable used by the EditViewActivity to check whether the image has been deleted or not*/
    public boolean deleted = false;



    /**
     * A function to create the view and start the main function (the init function)
     * @param inflater the default LayoutInflator
     * @param container the default container
     * @param savedInstanceState the save state of the fragment (not used)
     * @return the inflated view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.result_layout, null);
        init();
        return view;
    }


    /**
     * The main function of the Fragment
     */
    private void init() {
        Button originalButton;
        Button MagicColorButton;
        Button grayModeButton;
        Button bwButton;
        Button rotcButton;
        Button delButton;
        scannedImageView = view.findViewById(R.id.scannedImage);
        originalButton = view.findViewById(R.id.original);
        originalButton.setOnClickListener(new OriginalButtonClickListener());
        MagicColorButton = view.findViewById(R.id.magicColor);
        MagicColorButton.setOnClickListener(new MagicColorButtonClickListener());
        grayModeButton = view.findViewById(R.id.grayMode);
        grayModeButton.setOnClickListener(new GrayButtonClickListener());
        bwButton = view.findViewById(R.id.BWMode);
        bwButton.setOnClickListener(new BWButtonClickListener());
        rotcButton = view.findViewById(R.id.rotcButton);
        rotcButton.setOnClickListener(new RotButtonClickListener());
        delButton = view.findViewById(R.id.delete);
        delButton.setOnClickListener(new DeleteButtonListener());

        getBitmap();
    }


    /**
     * Get the bitmap set by the user asynchronously
     */
    private void getBitmap() {
        Uri uri = getUri();
        try{
            onReceiveBitmap(Utils.getBitmap(getActivity(), uri));
        }catch (IOException e){e.printStackTrace();}
    }



    /**
     * The URI of the bitmap
     * @return the URI
     */
    private Uri getUri() {
        return getArguments().getParcelable(ScanConstants.SCANNED_RESULT);
    }



    /**
     * Tha function to set the image on the view
     * @param scannedImage the Image to be set
     */
    public void setScannedImage(Bitmap scannedImage) {
        scannedImageView.setImageBitmap(scannedImage);
    }



    /**
     * A class to transform the image to a Black-&-White image.
     */
    private class BWButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            showProgressDialog(getResources().getString(R.string.applying_filter));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        transformed = com.scanlibrary.ScanActivity.getBWBitmap(rotoriginal);
                    } catch (final OutOfMemoryError e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                transformed = original;
                                scannedImageView.setImageBitmap(original);
                                e.printStackTrace();
                                dismissDialog();
                                onClick(v);
                            }
                        });
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scannedImageView.setImageBitmap(transformed);
                            dismissDialog();
                        }
                    });
                }
            });
        }
    }



    /**
     * A class to transform the image to a Magic-Color image.
     */
    private class MagicColorButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            showProgressDialog(getResources().getString(R.string.applying_filter));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        transformed = com.scanlibrary.ScanActivity.getMagicColorBitmap(rotoriginal);
                    } catch (final OutOfMemoryError e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                transformed = original;
                                scannedImageView.setImageBitmap(original);
                                e.printStackTrace();
                                dismissDialog();
                                onClick(v);
                            }
                        });
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scannedImageView.setImageBitmap(transformed);
                            dismissDialog();
                        }
                    });
                }
            });
        }
    }


    /**
     * A class to transform the image to the original image.
     */
    private class OriginalButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            try {
                showProgressDialog(getResources().getString(R.string.applying_filter));
                transformed = rotoriginal;

                scannedImageView.setImageBitmap(rotoriginal);
                dismissDialog();
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                dismissDialog();
            }
        }
    }


    /**
     * A class to transform the image to a Grayscale image.
     */
    private class GrayButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            showProgressDialog(getResources().getString(R.string.applying_filter));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        transformed = com.scanlibrary.ScanActivity.getGrayBitmap(rotoriginal);
                    } catch (final OutOfMemoryError e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                transformed = original;
                                scannedImageView.setImageBitmap(original);
                                e.printStackTrace();
                                dismissDialog();
                                onClick(v);
                            }
                        });
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scannedImageView.setImageBitmap(transformed);
                            dismissDialog();
                        }
                    });
                }
            });
        }
    }



    /**
     * A class to rotate the image.
     */
    private class RotButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            showProgressDialog(getResources().getString(R.string.applying_filter));
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Bitmap imageViewBitmap=((android.graphics.drawable.BitmapDrawable)scannedImageView.getDrawable()).getBitmap();

                        android.graphics.Matrix matrix = new android.graphics.Matrix();
                        matrix.postRotate(90);
                        rotoriginal = Bitmap.createBitmap(rotoriginal, 0, 0, rotoriginal.getWidth(), rotoriginal.getHeight(), matrix, true);
                        transformed = Bitmap.createBitmap(imageViewBitmap, 0, 0, imageViewBitmap.getWidth(), imageViewBitmap.getHeight(), matrix, true);

                    } catch (final OutOfMemoryError e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                transformed = original;
                                scannedImageView.setImageBitmap(original);
                                e.printStackTrace();
                                dismissDialog();
                                onClick(v);
                            }
                        });
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scannedImageView.setImageBitmap(transformed);
                            dismissDialog();
                        }
                    });
                }
            });
        }
    }


    /**
     * A class to delete the image
     */
    private class DeleteButtonListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            // noinspection ConstantConditions
            File imageFile = new File(getUri().getPath());
            if (imageFile.exists()){
                imageFile.delete();
            }
            ((EditViewActivity)getActivity()).delete(ResultFragment.this);
        }
    }


    /**
     * A function to show the progress dialog.
     * @param message the message to show.
     */
    protected synchronized void showProgressDialog(String message) {
        if (progressDialogFragment != null && progressDialogFragment.isVisible()) {
            // Before creating another loading dialog, close all opened loading dialogs (if any)
            progressDialogFragment.dismissAllowingStateLoss();
        }
        progressDialogFragment = null;
        progressDialogFragment = new ProgressDialogFragment(message);
        FragmentManager fm = getFragmentManager();
        progressDialogFragment.show(fm, ProgressDialogFragment.class.toString());
    }



    /**
     * A function to dismiss the progress dialog.
     */
    protected synchronized void dismissDialog() {
        progressDialogFragment.dismissAllowingStateLoss();
    }



    /**
     * A function to prepare the image for compiling the images into a PDF.
     */
    public void finish(){
        try {
            File file = new File(Objects.requireNonNull(getArguments().getString(ScanConstants.SCAN_FILE)));
            FileOutputStream stream = new FileOutputStream(file);
            transformed.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            original.recycle();
            transformed.recycle();
            rotoriginal.recycle();
        }catch (Exception e){e.printStackTrace();}
    }

    /**
     * A function to receive the requested bitmap
     * @param bitmap the requested bitmap
     */
    public void onReceiveBitmap(Bitmap bitmap){
        original = bitmap;
        transformed = bitmap;
        rotoriginal = bitmap;
        setScannedImage(bitmap);
    }
}
