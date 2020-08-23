package com.nalinstudios.iscan.internal;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

class ImageProcessor {
    private static Bitmap toGrayScale(Bitmap image){
        Mat myImage = new Mat();
        Utils.bitmapToMat(image, myImage);

        Imgproc.cvtColor(myImage, myImage, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(myImage, myImage, new Size(3,3), 2, 2);

        Bitmap.Config config = image.getConfig();
        Bitmap greyImage = Bitmap.createBitmap(myImage.width(), myImage.height(), config);

        Utils.matToBitmap(myImage, greyImage);
        return greyImage;
    }

    public static ArrayList<Point> getCorners(Bitmap img){
        Bitmap image = toGrayScale(img);

        Mat myImage = new Mat();
        Utils.bitmapToMat(image, myImage);

        Imgproc.Canny(myImage, myImage, 20, 60, 3, false);
        Imgproc.dilate(myImage, myImage, new Mat(), new Point(-1,-1), 3, 1, new Scalar(1));

        Bitmap.Config config = image.getConfig();
        Bitmap edgedImage = Bitmap.createBitmap(myImage.width(), myImage.height(), config);

        Utils.matToBitmap(myImage, edgedImage);
        return new ArrayList<>();
    }


}