package com.nalinstudios.iscan.scanlibrary;

/**
 * A class to load the required native Libraries
 * @author Nalin Angrish, Sekar.
 */
public class Loader {
    /** Whether we have already loaded the native libraries or not */
    private static boolean done = false;


    /**
     * Load the native libraries.
     */
    public static synchronized void load() {
        if (done)
            return;
        
        System.loadLibrary("Scanner");
        System.loadLibrary("opencv_java3");
        
        done = true;
    }
}

