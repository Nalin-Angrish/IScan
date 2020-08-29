package com.nalinstudios.iscan.internal;

import android.content.Context;
import android.hardware.Camera;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;


/**
 * A class to handle zoom events in the ScannerActivity.
 * @author Nalin Angrish.
 */
public class ZoomHandler implements View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener {
    /** The camera object to apply zoom effects on*/
    private Camera camera;
    /** The scale gesture detector to detect pinch in and pinch out gestures*/
    private ScaleGestureDetector gestureScale;
    /** The amount to scale*/
    private float scaleFactor = 1;

    /**
     * The default constructor of the zoom detector
     * @param camera The camera to apply zoom effects on.
     * @param c The context of the texture to listen zoo events onto.
     */
    public ZoomHandler(Camera camera, Context c){
        this.camera = camera;
        gestureScale = new ScaleGestureDetector(c, this);
    }

    /**
     * A function to activate zoom on the touch events
     * @param view The view object of the texture
     * @param event The type of motion event.
     * @return true
     */
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        gestureScale.onTouchEvent(event);
        return true;
    }

    /**
     * The main function to handle the zoom.
     * @param detector Detector to get the scale factor to zoom accordingly.
     * @return true
     */
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (camera.getParameters().isZoomSupported()){
            float defaultScale = scaleFactor;
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = (scaleFactor < 1 ? 1 : scaleFactor); // prevent our view from becoming too small //
            scaleFactor = ((float)((int)(scaleFactor * 100))) / 100; // Change precision to help with jitter when user just rests their fingers //

            if (camera.getParameters().getMaxZoom() >= scaleFactor && scaleFactor > 0){
                Camera.Parameters params = camera.getParameters();
                params.setZoom((int)scaleFactor);
                camera.setParameters(params);
            }else {
                scaleFactor = defaultScale;
            }
        }
        return true;
    }


    /**
     * Blank Function
     * @param detector -blank-
     * @return true
     */
    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    /**
     * Blank Function
     * @param detector -blank-
     */
    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {}
}