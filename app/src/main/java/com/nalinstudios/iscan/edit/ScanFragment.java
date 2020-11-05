package com.nalinstudios.iscan.edit;

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

import com.nalinstudios.iscan.R;
import com.nalinstudios.iscan.scanlibrary.PolygonView;
import com.nalinstudios.iscan.scanlibrary.ProgressDialogFragment;
import com.nalinstudios.iscan.scanlibrary.ScanConstants;
import com.nalinstudios.iscan.scanlibrary.SingleButtonDialogFragment;
import com.nalinstudios.iscan.scanlibrary.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A customized version of ScanFragment specifically enhanced for later-editing purposes
 * @author Nalin Angrish, Jhansi.
 */
public class ScanFragment extends Fragment{

    /** A button to approve the corners selected by the user */
    public Button scanButton;
    /** The imageView containing the image to be cropped*/
    private ImageView sourceImageView;
    /** The frame containing the source image*/
    private FrameLayout sourceFrame;
    /** The polygonView to mark the corners*/
    private PolygonView polygonView;
    /** The root view of the fragment*/
    private View view;
    /** The Fragment to show when any operation is being performed*/
    private ProgressDialogFragment progressDialogFragment;
    /** The image to be cropped */
    private Bitmap original;



    /**
     * A function to create the view and start the main function (the init function)
     * @param inflater the default LayoutInflator
     * @param container the default container
     * @param savedInstanceState the save state of the fragment (not used)
     * @return the inflated view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.scan_fragment_layout, null);
        init();
        return view;
    }

    /**
     * The main function of the Fragment
     */
    private void init() {
        sourceImageView = view.findViewById(R.id.sourceImageView);
        scanButton = view.findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new ScanButtonClickListener());
        sourceFrame = view.findViewById(R.id.sourceFrame);
        polygonView = view.findViewById(R.id.polygonView);
        sourceFrame.post(new Runnable() {
            @Override
            public void run() {
                getBitmap();
            }
        });
    }


    /**
     * Get the bitmap asynchronously set by the user
     */
    private void getBitmap() {
        Uri uri = getUri();
        onReceiveBitmap(Utils.getBitmap(getActivity(), uri));
    }


    /**
     * The URI of the bitmap
     * @return the URI
     */
    private Uri getUri() {
        return getArguments().getParcelable(ScanConstants.SELECTED_BITMAP);
    }



    /**
     * A function to set the image in the fragment
     * @param original the image to be set.
     */
    private void setBitmap(Bitmap original) {
        Bitmap scaledBitmap = scaledBitmap(original, sourceFrame.getWidth(), sourceFrame.getHeight());
        sourceImageView.setImageBitmap(scaledBitmap);
        Bitmap tempBitmap = ((BitmapDrawable) sourceImageView.getDrawable()).getBitmap();
        Map<Integer, PointF> pointFs = getEdgePoints(tempBitmap);
        polygonView.setPoints(pointFs);
        polygonView.setVisibility(View.VISIBLE);
        int padding = (int) getResources().getDimension(R.dimen.scanPadding);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(tempBitmap.getWidth() + 2 * padding, tempBitmap.getHeight() + 2 * padding);
        layoutParams.gravity = Gravity.CENTER;
        polygonView.setLayoutParams(layoutParams);
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
     * A class to know when to scan the image.
     */
    private class ScanButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Map<Integer, PointF> points = polygonView.getPoints();
            if (isScanPointsValid(points)) {
                new ScanAsyncTask(points).execute();
            } else {
                showErrorDialog();
            }
        }
    }


    /**
     * A function to show an error dialog.
     */
    private void showErrorDialog() {
        SingleButtonDialogFragment fragment = new SingleButtonDialogFragment(R.string.ok, getString(R.string.cantCrop), "Error", true);
        FragmentManager fm = getActivity().getFragmentManager();
        fragment.show(fm, SingleButtonDialogFragment.class.toString());
    }


    /**
     * A function to check whether the given points correspond to a quadrilateral or not.
     * @param points the points to check
     * @return whether or not thee corners make a quadrilateral
     */
    private boolean isScanPointsValid(Map<Integer, PointF> points) {
        return points.size() == 4;
    }


    /**
     * A function to scale a bitmap
     * @param bitmap the bitmap to be scaled
     * @param width the width to be scaled to.
     * @param height the height to be scaled to.
     * @return the scaled bitmap
     */
    private Bitmap scaledBitmap(Bitmap bitmap, int width, int height) {
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), new RectF(0, 0, width, height), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }


    /**
     * A function to crop the image to the given points
     * @param original the image to be cropped
     * @param points the points to crop the image from
     * @return the cropped image
     */
    @SuppressWarnings("ConstantConditions")
    private Bitmap getScannedBitmap(Bitmap original, Map<Integer, PointF> points) {
        float xRatio = (float) original.getWidth() / sourceImageView.getWidth();
        float yRatio = (float) original.getHeight() / sourceImageView.getHeight();

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
     * A class to scan the image on the click of the button.
     * This includes cropping and saving the image on the disk.
     */
    private class ScanAsyncTask extends AsyncTask<Void, Void, Bitmap> {

        /** The points to crop from*/
        private Map<Integer, PointF> points;

        /**
         * The constructor to store the points
         * @param points the points to crop the image from
         */
        ScanAsyncTask(Map<Integer, PointF> points) {
            this.points = points;
        }

        /**
         * A function to show a progress dialog before performing the main function.
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(getString(R.string.scanning));
        }


        /**
         * The main function to crop the image in background
         * @param param i don't know what it does but it is not required
         * @return the scanned bitmap
         */
        @Override
        protected Bitmap doInBackground(Void... param) {
            Bitmap bitmap = getScannedBitmap(original, points);
            try {
                OutputStream s = new FileOutputStream(new File(Objects.requireNonNull(getArguments().getString(ScanConstants.SCAN_FILE))));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, s);
            }catch (Exception e){e.printStackTrace();}
            return bitmap;
        }


        /**
         * A function to hide the progress dialog after performing the main function.
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            bitmap.recycle();
            dismissDialog();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((PDFEditActivity)getActivity()).onScanFinish();
                }
            });
        }
    }

    /**
     * A function to show the progress dialog.
     * @param message the message to show.
     */
    protected void showProgressDialog(String message) {
        progressDialogFragment = new ProgressDialogFragment(message);
        FragmentManager fm = getFragmentManager();
        progressDialogFragment.show(fm, ProgressDialogFragment.class.toString());
    }


    /**
     * A function to dismiss the progress dialog.
     */
    protected void dismissDialog() {
        progressDialogFragment.dismissAllowingStateLoss();
    }

    /**
     * A function to receive the requested bitmap
     * @param bitmap the requested bitmap
     */
    public void onReceiveBitmap(Bitmap bitmap){
        original = bitmap;
        if (original != null) {
            setBitmap(original);
        }
    }
}