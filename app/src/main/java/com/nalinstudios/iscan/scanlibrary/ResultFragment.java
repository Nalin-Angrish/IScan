package com.nalinstudios.iscan.scanlibrary;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.nalinstudios.iscan.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Customized combination of ScanActivity and ResultFragment.
 * @author Nalin Angrish, Jhansi.
 */
public class ResultFragment extends Fragment {

    /** The root view of the fragment*/
    private View view;
    /** The frame containing the source image*/
    private FrameLayout sourceFrame;
    /** The imageView containing the image to be formatted*/
    private ImageView scannedImageView;
    /** The polygonView to mark the corners*/
    private PolygonView polygonView;
    /** A bitmap to keep in memory the original picture */
    private Bitmap original;
    /** The bitmap that will be shown to the user*/
    private Bitmap transformed;
    /** The Fragment to show when any operation is being performed*/
    private ProgressDialogFragment progressDialogFragment;


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
        sourceFrame = view.findViewById(R.id.sourceFrame);
        sourceFrame.post(new Runnable() {
            @Override
            public void run() {
                original = getBitmap();
                if (original != null) {
                    setBitmap(original);
                }
            }
        });
        polygonView = view.findViewById(R.id.polygonView);
        scannedImageView = view.findViewById(R.id.scannedImage);
        originalButton = view.findViewById(R.id.original);
        originalButton.setOnClickListener(new OriginalButtonClickListener());
        MagicColorButton = view.findViewById(R.id.magicColor);
        MagicColorButton.setOnClickListener(new MagicColorButtonClickListener());
        grayModeButton = view.findViewById(R.id.grayMode);
        grayModeButton.setOnClickListener(new GrayButtonClickListener());
        bwButton = view.findViewById(R.id.BWMode);
        bwButton.setOnClickListener(new BWButtonClickListener());


        Bitmap bitmap = getBitmap();
        transformed = bitmap;

        setScannedImage(bitmap);
    }


    /**
     * Get the bitmap set by the user
     * @return the Bitmap
     */
    private Bitmap getBitmap() {
        Uri uri = getUri();
        try {
            original = Utils.getBitmap(getActivity(), uri);
            return original;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * The URI of the bitmap
     * @return the URI
     */
    private Uri getUri() {
        return Uri.parse(getArguments().getString(ScanConstants.SCANNED_RESULT));
    }


    /**
     * Tha function to set the image on the view
     * @param scannedImage the Image to be set
     */
    public void setScannedImage(Bitmap scannedImage) {
        scannedImageView.setImageBitmap(scannedImage);
    }


    /**
     * Get edge points (corners)
     * @param tempBitmap the bitmap to track on
     * @return the Map of corners
     */
    private Map<Integer, PointF> getEdgePoints(Bitmap tempBitmap) {
        List<PointF> pointFs = getContourEdgePoints(tempBitmap);
        return orderedValidEdgePoints(tempBitmap, pointFs);
    }


    /**
     * A function to set the initial image in the fragment
     * @param original the image to be set.
     */
    private void setBitmap(Bitmap original) {
        Bitmap scaledBitmap = scaledBitmap(original, sourceFrame.getWidth(), sourceFrame.getHeight());
        scannedImageView.setImageBitmap(scaledBitmap);
        Bitmap tempBitmap = ((BitmapDrawable) scannedImageView.getDrawable()).getBitmap();
        Map<Integer, PointF> pointFs = getEdgePoints(tempBitmap);
        polygonView.setPoints(pointFs);
        polygonView.setVisibility(View.VISIBLE);
        int padding = (int) getResources().getDimension(R.dimen.scanPadding);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(tempBitmap.getWidth() + 2 * padding, tempBitmap.getHeight() + 2 * padding);
        layoutParams.gravity = Gravity.CENTER;
        polygonView.setLayoutParams(layoutParams);
    }


    /**
     * A function to scale the Bitmap to the given width-height
     * @param bitmap the bitmap to be scaled
     * @param width the width to be scaled to
     * @param height the height to be scaled to
     * @return the scaled Bitmap
     */
    private Bitmap scaledBitmap(Bitmap bitmap, int width, int height) {
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), new RectF(0, 0, width, height), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }


    /**
     * A function to get the points of the corners
     * @param tempBitmap the bitmap to find the corners in
     * @return the List of points
     */
    private List<PointF> getContourEdgePoints(Bitmap tempBitmap) {
        float[] points = com.scanlibrary.ScanActivity.getPoints(tempBitmap);
        float x1 = points[0];
        float x2 = points[1];
        float x3 = points[2];
        float x4 = points[3];

        float y1 = points[4];
        float y2 = points[5];
        float y3 = points[6];
        float y4 = points[7];

        List<PointF> pointFs = new ArrayList<>();
        pointFs.add(new PointF(x1, y1));
        pointFs.add(new PointF(x2, y2));
        pointFs.add(new PointF(x3, y3));
        pointFs.add(new PointF(x4, y4));
        return pointFs;
    }


    /**
     * The outline points of the image (the actual image)
     * @param tempBitmap the image to get points of
     * @return a list of Integer-Point pairs.
     */
    private Map<Integer, PointF> getOutlinePoints(Bitmap tempBitmap) {
        Map<Integer, PointF> outlinePoints = new HashMap<>();
        outlinePoints.put(0, new PointF(0, 0));
        outlinePoints.put(1, new PointF(tempBitmap.getWidth(), 0));
        outlinePoints.put(2, new PointF(0, tempBitmap.getHeight()));
        outlinePoints.put(3, new PointF(tempBitmap.getWidth(), tempBitmap.getHeight()));
        return outlinePoints;
    }


    /**
     * A function to order the edge points
     * @param tempBitmap the bitmap to get ordered points of.
     * @param pointFs the points to be ordered
     * @return a list of Integer-Point pairs.
     */
    private Map<Integer, PointF> orderedValidEdgePoints(Bitmap tempBitmap, List<PointF> pointFs) {
        Map<Integer, PointF> orderedPoints = polygonView.getOrderedPoints(pointFs);
        if (!polygonView.isValidShape(orderedPoints)) {
            orderedPoints = getOutlinePoints(tempBitmap);
        }
        return orderedPoints;
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
        Toast.makeText(getActivity().getApplicationContext(), "Finish function called", Toast.LENGTH_LONG).show();
        showProgressDialog(getResources().getString(R.string.loading));
        Map<Integer, PointF> points = polygonView.getPoints();
        try {
            Bitmap bmp = transformed;
            if (points.size()==4) {
                bmp = getScannedBitmap(bmp, points);
            } else {
                showErrorDialog();
            }
            Uri uri = getUri();
            Log.println(Log.ASSERT,"URI", ""+uri.getPath());
            FileOutputStream stream = new FileOutputStream(new File(""+uri.getPath()));
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        }catch (Exception e){
            e.printStackTrace();
            Log.println(Log.ASSERT, "Error while converting",e.toString());
        }
    }


    /**
     * A function to show an Error dialog.
     */
    private void showErrorDialog() {
        SingleButtonDialogFragment fragment = new SingleButtonDialogFragment(R.string.ok, getString(R.string.cantCrop), "Error", true);
        FragmentManager fm = getActivity().getFragmentManager();
        fragment.show(fm, SingleButtonDialogFragment.class.toString());
    }


    /**
     * A function to crop the image from the given points
     * @param original the image to crop.
     * @param points the points to crop from
     * @return the Cropped Bitmap
     */
    private Bitmap getScannedBitmap(Bitmap original, Map<Integer, PointF> points) {
        //int width = original.getWidth();
        //int height = original.getHeight();
        float xRatio = (float) original.getWidth() / scannedImageView.getWidth();
        float yRatio = (float) original.getHeight() / scannedImageView.getHeight();

        float x1 = (points.get(0).x) * xRatio;
        float x2 = (points.get(1).x) * xRatio;
        float x3 = (points.get(2).x) * xRatio;
        float x4 = (points.get(3).x) * xRatio;
        float y1 = (points.get(0).y) * yRatio;
        float y2 = (points.get(1).y) * yRatio;
        float y3 = (points.get(2).y) * yRatio;
        float y4 = (points.get(3).y) * yRatio;
        Log.d("", "Points(" + x1 + "," + y1 + ")(" + x2 + "," + y2 + ")(" + x3 + "," + y3 + ")(" + x4 + "," + y4 + ")");

        return com.scanlibrary.ScanActivity.getScannedBitmap(original, x1, y1, x2, y2, x3, y3, x4, y4);
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
                        transformed = com.scanlibrary.ScanActivity.getBWBitmap(original);
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
                        transformed = com.scanlibrary.ScanActivity.getMagicColorBitmap(original);
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
                transformed = original;

                scannedImageView.setImageBitmap(original);
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
                        transformed = com.scanlibrary.ScanActivity.getGrayBitmap(original);
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



}
