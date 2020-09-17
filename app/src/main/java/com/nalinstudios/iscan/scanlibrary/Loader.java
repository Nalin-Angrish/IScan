package com.nalinstudios.iscan.scanlibrary;

/**
 * Created by sekar on 21/2/18.
 */

public class Loader {
    private static boolean done = false;

    public static synchronized void load() {
        if (done)
            return;
        
        System.loadLibrary("Scanner");
        System.loadLibrary("opencv_java3");
        
        done = true;
    }
}

