package com.nalinstudios.iscan.scanlibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import java.io.File;

import id.zelory.compressor.Compressor;

/**
 * The class containing helper method(s)
 * @author Nalin Angrish, Jhansi
 */
public class Utils {

    /*
    static Uri getUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        Log.wtf("PATH", "before insertImage");
        // String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title" + " - " + (Calendar.getInstance().getTime()), null);
        Log.wtf("PATH", path);
        return Uri.parse(path);
    }*/


    /**
     * A function to get the image from its URI
     * @param ctx the context of the activity
     * @param uri the URI to obtain image from.
     * @return the obtained bitmap
     */
    public static Bitmap getBitmap(Context ctx, final Uri uri){
        Bitmap img = new Compressor(ctx)
                .setQuality(70)
                .compressToBitmap(new File(uri.getPath()));
        Log.println(Log.ASSERT, "memory used: ", (img.getAllocationByteCount()/1024)+" kb");
        return img;
    }

}

