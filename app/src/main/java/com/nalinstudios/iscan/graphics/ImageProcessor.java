package com.nalinstudios.iscan.graphics;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;


public class ImageProcessor {

    final private static int cannyThreshold = 25;


    public static Bitmap getCorners(Bitmap img){
        Mat image = new Mat();
        Utils.bitmapToMat(img, image);

        Mat greyImage = new Mat();
        Imgproc.cvtColor(image, greyImage, Imgproc.COLOR_BGR2GRAY);

        Mat blurred = new Mat();
        Imgproc.GaussianBlur(greyImage, blurred, new Size(1,1), 5);

        Mat edges = new Mat();
        Imgproc.Canny(blurred, edges, cannyThreshold, cannyThreshold*3);


        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        MatOfPoint mainContour = contours.get(ImgHelper.findLargestSquare(contours));
        Rect rect = Imgproc.boundingRect(mainContour);


        Point p1 = new Point(rect.x, rect.y);
        Point p2 = new Point(rect.x+rect.width, rect.y);
        Point p4 = new Point(rect.x+rect.width, rect.y+rect.height);
        Point p3 = new Point(rect.x, rect.y+rect.height);
        List<Point> source = new ArrayList<>();
        source.add(p1);
        source.add(p2);
        source.add(p3);
        source.add(p4);
        ImgHelper.sortCorners(source);

        for (Point p : source){
            Core.circle(image, p, 50, new Scalar(255,0,0), 30);
        }
        Core.line(image, p1, p2, new Scalar(0,255,0), 20);
        Core.line(image, p2, p4, new Scalar(0,255,0), 20);
        Core.line(image, p3, p4, new Scalar(0,255,0), 20);
        Core.line(image, p3, p1, new Scalar(0,255,0), 20);

        Utils.matToBitmap(image, img);
        return img;
    }








    public static Bitmap cropImage(Bitmap image, ArrayList<Point> corners){
        Mat mat = new Mat();
        Utils.bitmapToMat(image, mat);
        ImgHelper.sortCorners(corners);

        Point p0 = corners.get(0);
        Point p1 = corners.get(1);
        Point p2 = corners.get(2);
        Point p3 = corners.get(3);
        double space0 = ImgHelper.getSpacePointToPoint(p0, p1);
        double space1 = ImgHelper.getSpacePointToPoint(p1, p2);
        double space2 = ImgHelper.getSpacePointToPoint(p2, p3);
        double space3 = ImgHelper.getSpacePointToPoint(p3, p0);

        double imgWidth = space1 > space3 ? space1 : space3;
        double imgHeight = space0 > space2 ? space0 : space2;

        if (imgWidth < imgHeight) {
            double temp = imgWidth;
            //noinspection all
            imgWidth = imgHeight;
            imgHeight = temp;
            Point tempPoint = p0.clone();
            p0 = p1.clone();
            p1 = p2.clone();
            p2 = p3.clone();
            p3 = tempPoint.clone();
        }

        Mat quad = Mat.zeros((int)imgHeight * 2, (int)imgWidth * 2, CvType.CV_8UC3);

        MatOfPoint2f cornerMat = new MatOfPoint2f(p0, p1, p2, p3);
        MatOfPoint2f quadMat = new MatOfPoint2f(new Point(imgWidth*0.4, imgHeight*1.6),
                new Point(imgWidth*0.4, imgHeight*0.4),
                new Point(imgWidth*1.6, imgHeight*0.4),
                new Point(imgWidth*1.6, imgHeight*1.6));

        Mat transmtx = Imgproc.getPerspectiveTransform(cornerMat, quadMat);
        Imgproc.warpPerspective(mat, quad, transmtx, quad.size());

        Utils.matToBitmap(mat, image);
        return image;
    }




























}
