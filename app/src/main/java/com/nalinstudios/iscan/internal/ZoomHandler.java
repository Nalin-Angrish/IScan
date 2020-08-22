package com.nalinstudios.iscan.internal;

import android.content.Context;
import android.hardware.Camera;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;


public class ZoomHandler implements View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener {
    private Camera camera;
    private ScaleGestureDetector gestureScale;
    private float scaleFactor = 1;
    private boolean inScale = false;

    public ZoomHandler(Camera camera, Context c){
        this.camera = camera;
        gestureScale = new ScaleGestureDetector(c, this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        gestureScale.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        scaleFactor *= detector.getScaleFactor();
        scaleFactor = (scaleFactor < 1 ? 1 : scaleFactor); // prevent our view from becoming too small //
        scaleFactor = ((float)((int)(scaleFactor * 100))) / 100; // Change precision to help with jitter when user just rests their fingers //
        if (camera.getParameters().isZoomSupported()){
            if (camera.getParameters().getMaxZoom() >= scaleFactor && scaleFactor > 0){
                Camera.Parameters params = camera.getParameters();
                params.setZoom((int)scaleFactor);
                camera.setParameters(params);
            }
        }
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        inScale = true;
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) { inScale = false; }
}