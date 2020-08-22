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

public class Statics {
    private final static String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    public static String randString(){
        StringBuilder builder = new StringBuilder();
        for (int i = 0;i<10;i++){
            int index = (int)(chars.length() * Math.random());
            builder.append(chars.charAt(index));
        }
        return builder.toString();
    }

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

    private static byte[] toByteArray(Bitmap image){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

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

    private static File getThumbnail(Application app){
        String folder = app.getSharedPreferences("IScan", Context.MODE_PRIVATE).getString("sessionName", "hello");
        File thumb = new File(app.getFilesDir(), folder).listFiles()[0];
        assert thumb != null;
        return  thumb;
    }

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
