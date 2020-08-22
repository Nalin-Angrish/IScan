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


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clear();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getApplicationContext().getSharedPreferences("IScan", MODE_PRIVATE).edit().putString("sessionName", Statics.randString()).apply();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this , ScannerActivity.class);
                startActivity(intent);
            }
        });

        File storage = new File(Environment.getExternalStorageDirectory(), "IScan");
        File folder = new File(storage, ".data-internal");
        File[] files = folder.listFiles();
        try {
            for (File file : files) {
                PdfCard pdfCard = new PdfCard(getApplicationContext(), file, getLayoutInflater());
                LinearLayout main = findViewById(R.id.__main__);
                main.addView(pdfCard.getCard());
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    protected void clear(){
        LinearLayout main = findViewById(R.id.__main__);
        main.removeAllViews();
    }





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

    @Override
    public void onBackPressed(){
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}
