package com.nalinstudios.iscan.scanlibrary;

import android.os.Environment;

/**
 * A class to contain all the constants for the scanLibrary
 * @author Jhansi
 */
public class ScanConstants {

    public final static int PICKFILE_REQUEST_CODE = 1;
    public final static int START_CAMERA_REQUEST_CODE = 2;
    public final static String SCAN_FILE = "scanFile";
    public final static String IMAGE_BASE_PATH_EXTRA = "ImageBasePath";
    public final static int OPEN_CAMERA = 4;
    public final static int OPEN_MEDIA = 5;
    public final static String IMAGE_PATH = Environment.getExternalStorageDirectory().getPath() + "/scanSample";
    public final static String SELECTED_BITMAP = "selectedBitmap";


    public final static String SCANNED_RESULT = "scannedResult";


}