package com.nalinstudios.iscan.internal;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;


import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to store all simple functions used by other files in this application.
 * @author Nalin Angrish.
 */
public class Statics {
    /** The characters which can be used to generate a random string*/
    private final static String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    /**
     * A function to generate a random string
     * @return A random String
     */
    public static String randString(){
        StringBuilder builder = new StringBuilder();
        for (int i = 0;i<10;i++){
            int index = (int)(chars.length() * Math.random());
            builder.append(chars.charAt(index));
        }
        return builder.toString();
    }

    /**
     * A function to create a PDF from all the Imaged captured in the current session.
     * @param app The application object to get the current session.
     * @param name The name of the PDF to store as.
     * @throws IOException Thrown if the PDF could not be written and stored.
     * @throws DocumentException Thrown if the Document could not be created,
     */
    public static void createPdf(Application app, String name) throws IOException, DocumentException {
        File pdfFolder = new File(Environment.getExternalStorageDirectory(), "IScan");
        if (!pdfFolder.exists()){System.out.println(pdfFolder.mkdir());}
        File pdf = new File(pdfFolder, name+".pdf");
        if (!pdf.exists()){System.out.println(pdf.createNewFile());}
        String sessionName = app.getSharedPreferences("IScan", Context.MODE_PRIVATE).getString("sessionName", "hello");
        File imageFolder = new File(app.getFilesDir(), sessionName);
        List<Bitmap> images = new ArrayList<>();
        File[] files = imageFolder.listFiles();
        for (File eachFile : files){
            images.add(BitmapFactory.decodeFile(eachFile.getAbsolutePath()));
        }

        Document doc = new Document();
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(pdf));
        doc.open();
        Rectangle rect = writer.getPageSize();
        writer.setMargins(0,0,0,0);
        float WIDTH = rect.getWidth()-72;
        float HEIGHT = rect.getHeight()-72;
        doc.addAuthor("IScan - The Document Scanner");
        for (Bitmap image : images){
            doc.newPage();
            Bitmap page = Bitmap.createScaledBitmap(image, (int)WIDTH, (int)HEIGHT, false);
            Image img = Image.getInstance(toByteArray(page));
            doc.add(img);
        }
        doc.close();
        File thumbnailFile = getThumbnail(app);
        File dataStorage = new File(pdfFolder, ".data-internal");
        if (!dataStorage.exists()){System.out.println(dataStorage.mkdir());}
        File thumbnail = new File(dataStorage,name+".jpg");
        if (!thumbnail.exists()){
            thumbnail.createNewFile();
            copy(thumbnailFile, thumbnail);
        }
        clearData(app);
    }

    /**
     * A function to get an array of bytes of the image so that the image can be added to the document.
     * @param image The image to be decoded into byte array.
     * @return The byte array for the image.
     */
    private static byte[] toByteArray(Bitmap image){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * A function to clear the data of the current session.
     * @param app The application object for which the data is to be cleared.
     */
    public static void clearData(Application app){
        File folder = app.getFilesDir();
        File deletable = new File(folder, app.getSharedPreferences("IScan", Context.MODE_PRIVATE).getString("sessionName", "hello"));
        try {
            for (File file : deletable.listFiles()){
                System.out.println(file.delete());
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        System.out.println(deletable.delete());
    }

    /**
     * A function to get the first image of the PDF to store it as a thumbnail.
     * @param app The application object to get the current session.
     * @return The file object of the first image of the Document.
     */
    private static File getThumbnail(Application app){
        String folder = app.getSharedPreferences("IScan", Context.MODE_PRIVATE).getString("sessionName", "hello");
        File thumb = new File(app.getFilesDir(), folder).listFiles()[0];
        assert thumb != null;
        return  thumb;
    }

    /**
     * A function to copy files from one location to another.
     * @param src The source location of the file.
     * @param dst The destination of the file.
     * @throws IOException Thrown if couldn't write to teh destination.
     */
    private static void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }


}
