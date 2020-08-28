package com.nalinstudios.iscan;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nalinstudios.iscan.internal.PdfCard;
import com.nalinstudios.iscan.internal.Statics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.view.View;
import android.widget.LinearLayout;

import java.io.File;

/**
 * Main Activity (The name tells you).
 * It will make the main list of all the PDFs created till now.
 * @author Nalin Angrish.
 */
public class MainActivity extends AppCompatActivity {
    /**
     * The on create method of MainActivity.
     * @param savedInstanceState The state of the saved instance. This state is not used.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clear();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this , ScannerActivity.class);
                startActivity(intent);
            }
        });

    }

    /**
     * A function to clear all entries of the PDFs in the main window.
     */
    protected void clear(){
        LinearLayout main = findViewById(R.id.__main__);
        main.removeAllViews();
    }


    /**
     * This function will create all the CardLayouts for the PDFs.
     */
    @Override
    protected void onResume() {
        clear();
        getApplicationContext().getSharedPreferences("IScan", MODE_PRIVATE).edit().putString("sessionName", Statics.randString()).apply();
        File[] folder = new File(new File(Environment.getExternalStorageDirectory(), "IScan"), ".data-internal").listFiles();
        try {
            for (File file : folder) {
                PdfCard pdfCard = new PdfCard(getApplicationContext(), file, getLayoutInflater());
                LinearLayout main = findViewById(R.id.__main__);
                main.addView(pdfCard.getCard());
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        super.onResume();
    }

    /**
     * This function will take to the home screen instead of the previous activity if the user has came here after clicking the PDF.
     */
    @Override
    public void onBackPressed(){
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}
