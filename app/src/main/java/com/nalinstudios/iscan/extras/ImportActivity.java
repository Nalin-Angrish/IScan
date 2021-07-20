package com.nalinstudios.iscan.extras;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import com.nalinstudios.iscan.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * An activity to import all files from the previous storage location to the new one.
 *
 * @author Nalin Angrish.
 */
public class ImportActivity extends AppCompatActivity {

    /**
     * The initial function that starts the import process
     * @param savedInstanceState the saved instance (Not used)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);
        new Thread(new Runnable() {
            @Override
            public void run() {
                importScans();
            }
        })
        .start();
    }


    /**
     * The main function that actually imports the files in the background
     */
    public void importScans(){
        File src = new File(Environment.getExternalStorageDirectory(), "IScan");
        File dest = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        try{
            moveFiles(src, dest);
            finish();
        }catch (IOException e){
            Toast.makeText(this, "An internal error occurred.", Toast.LENGTH_LONG).show();
            System.out.println(e.toString());
            throw new RuntimeException(e);
        }
    }


    /**
     * A function to recursively move the files from the <code>src</code> directory to the <code>dest</code> directory
     * @param src the source directory
     * @param dest the destination directory
     * @throws IOException If there was issue reading/writing/deleting the files
     */
    private void moveFiles(File src, File dest) throws IOException {
        File[] srcFiles = src.listFiles();
        if(srcFiles==null) srcFiles = new File[]{};
        for(File srcFile : srcFiles){
            File destFile = new File(dest, srcFile.getName());
            if(srcFile.isDirectory()){
                if(!destFile.exists())destFile.mkdir();
                moveFiles(srcFile, destFile);
            }else {
                if(!destFile.exists())destFile.createNewFile();
                try (InputStream in = new FileInputStream(srcFile);
                     OutputStream out = new FileOutputStream(destFile)) {
                    byte[] buf = new byte[1024];
                    int length;
                    while ((length = in.read(buf)) > 0) {
                        out.write(buf, 0, length);
                    }
                }
                srcFile.delete();
            }
        }
    }
}