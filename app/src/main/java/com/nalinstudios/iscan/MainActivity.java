package com.nalinstudios.iscan;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nalinstudios.iscan.internal.PdfCard;
import com.nalinstudios.iscan.internal.Statics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import android.os.Environment;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Main Activity (The name tells you).
 * It will make the main list of all the PDFs created till now and will provide access to the other activities.
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
                finish();
            }
        });
        askPermissions();
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
        assert folder != null;
        folder = reverseArray(folder);
        int stdlimit = 10; // this limit is the number of cards that will be shown. Keep this as low so it does not lag the device (low end devices may lag)
                //But keep it high enough that the user does not get frustrated by asking to open another app (the file explorer)
        int limit;
        if (folder.length < stdlimit){
            limit = folder.length;
        }else {
            limit = stdlimit;
        }
        try {
            for (int i=0;i<limit;i++) {
                File file = folder[i];
                PdfCard pdfCard = new PdfCard(getApplicationContext(), file, getLayoutInflater(), this);
                LinearLayout main = findViewById(R.id.__main__);
                main.addView(pdfCard.getCard());
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        if (folder.length > stdlimit){ // I.E., the no. of files was more than the standard limit
            // We need to show the user the option to see older files
            CardView card = new CardView(this);
            int color = Color.rgb(255,255,255);
            card.setCardBackgroundColor(color);
            card.setRadius(10);
            card.setUseCompatPadding(true);
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ListFileActivity.class);
                    intent.putExtra("path", "/storage/emulated/0/IScan/");
                    startActivity(intent);
                }
            });
            getLayoutInflater().inflate(R.layout.see_more, card);
            ((LinearLayout)findViewById(R.id.__main__)).addView(card);
        }

        super.onResume();
    }


    /**
     * A function to reverse an array of files
     * @param arr the array to reverse
     * @return the reversed array
     */
    public File[] reverseArray(File[] arr) {
        ArrayList<File> revArrayList = new ArrayList<>();
        for (int i = arr.length - 1; i >= 0; i--) {
            revArrayList.add(arr[i]);
        }
        return revArrayList.toArray(new File[revArrayList.size()]);
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


    /**
     * A function to check whether all permissions are given and if not, then ask.
     */
    protected void askPermissions(){
        boolean hasAll = true;
        List<String> permissions = new ArrayList<>();
        if (PermissionChecker.checkSelfPermission(getApplicationContext(), "android.permission.CAMERA") == PermissionChecker.PERMISSION_DENIED ){
               hasAll = false;
               permissions.add("android.permission.CAMERA");
        }
        if (PermissionChecker.checkSelfPermission(getApplicationContext(), "android.permission.READ_EXTERNAL_STORAGE") == PermissionChecker.PERMISSION_DENIED ){
            hasAll = false;
            permissions.add("android.permission.READ_EXTERNAL_STORAGE");
        }
        if (PermissionChecker.checkSelfPermission(getApplicationContext(), "android.permission.WRITE_EXTERNAL_STORAGE") == PermissionChecker.PERMISSION_DENIED ){
            hasAll = false;
            permissions.add("android.permission.WRITE_EXTERNAL_STORAGE");
        }
        if (!hasAll){
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[permissions.size()]), 101);
        }
    }


    /**
     * A function to know whether the permissions requested have been granted. If not, then close the application
     * because it cannot run without the permissions.
     * @param requestCode to check whether this is called due to our request or not
     * @param permissions The permissions (array) that were requested
     * @param grantResults the results (array) that we got
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean allgranted = true;
        if (requestCode == 101){
            for (int granted : grantResults){
                if (!(granted == PermissionChecker.PERMISSION_GRANTED)){
                    allgranted = false;
                }
            }
        }
        if (!allgranted){
            Toast.makeText(this, "Permissions not allowed so closing app. Go to Settings > Apps > IScan and allow the permissions", Toast.LENGTH_LONG).show();
            finish();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
