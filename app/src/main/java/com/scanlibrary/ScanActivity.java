package com.scanlibrary;

import android.graphics.Bitmap;

/**
 * A simple class to get access to native methods of the scanLibrary's ScanActivity class.
 * @author Nalin Angrish
 */
public class ScanActivity {

    public static native Bitmap getScannedBitmap(Bitmap bitmap, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4);

    public static native Bitmap getGrayBitmap(Bitmap bitmap);

    public static native Bitmap getMagicColorBitmap(Bitmap bitmap);

    public static native Bitmap getBWBitmap(Bitmap bitmap);

    public static native float[] getPoints(Bitmap bitmap);
}
