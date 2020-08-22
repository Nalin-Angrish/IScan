package com.nalinstudios.iscan;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nalinstudios.iscan.internal.Statics;


import java.io.File;

@SuppressWarnings("ConstantConditions")
public class EditViewActivity extends AppCompatActivity implements View.OnClickListener {
    FloatingActionButton finishB;
    Button nextB, prevB;
    ImageButton  delB;
    ImageView imgview;
    File dir;
    int index = 0;
    final int MIN_DISTANCE = 100;
    float x1, x2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_view);
        main();
    }

    protected void main(){
        nextB = findViewById(R.id.nextButton);
        prevB = findViewById(R.id.PreviousButton);
        delB = findViewById(R.id.deleteButton);
        finishB = findViewById(R.id.finishB);
        nextB.setOnClickListener(this);
        prevB.setOnClickListener(this);
        delB.setOnClickListener(this);
        finishB.setOnClickListener(this);
        String sessionDir = getApplication().getSharedPreferences("IScan", MODE_PRIVATE).getString("sessionName", "hello");
        dir = new File(getFilesDir(), sessionDir);

        File imageFile = dir.listFiles()[index];
        Bitmap image = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        imgview = findViewById(R.id.preview);
        imgview.setImageBitmap(image);
    }

    protected void deleteCurrentPic(){
        File fileToBeDeleted = dir.listFiles()[index];
        if (fileToBeDeleted.delete()){
            Toast.makeText(getApplicationContext(), "Image Successfully Deleted", Toast.LENGTH_LONG).show();
            if (index==0){
                nextB.performClick();
            }else {
                prevB.performClick();
            }

        }else {
            Toast.makeText(getApplicationContext(), "Image couldn't be deleted", Toast.LENGTH_LONG).show();
        }
    }

    protected void Askname(){
        View popup = getLayoutInflater().inflate(R.layout.popup_enter_name, null);
        final PopupWindow window = new PopupWindow(popup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        window.setAnimationStyle(android.R.style.Animation_Dialog);
        window.showAtLocation(popup, Gravity.CENTER, 0, 0);
        window.getContentView().findViewById(R.id.end).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                EditText tBox = window.getContentView().findViewById(R.id.pdfName);
                window.dismiss();
                try {
                    Statics.createPdf(getApplication(), tBox.getText().toString());
                    Intent intent = new Intent(EditViewActivity.this, MainActivity.class);
                    startActivity(intent);
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Couldn't create PDF, Please try again", Toast.LENGTH_LONG).show();
                }
            }
        });
        window.getContentView().findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
            }
        });
    }

    @Override
    public void onClick(View v){
        if (v.equals(nextB)){
            index=index+1;
            try {
                File imagefile = dir.listFiles()[index];
                Bitmap bitmap = BitmapFactory.decodeFile(imagefile.getAbsolutePath());
                imgview.setImageBitmap(bitmap);
            }catch (Exception e){
                index=index-1;
                e.printStackTrace();
            }
        }else if (v.equals(prevB)){
            index=index-1;
            try {
                File imagefile = dir.listFiles()[index];
                Bitmap bitmap = BitmapFactory.decodeFile(imagefile.getAbsolutePath());
                imgview.setImageBitmap(bitmap);
            }catch (Exception e){
                index=index+1;
                e.printStackTrace();
            }
        }else if (v.equals(delB)){
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_delete)
                    .setTitle("Delete?")
                    .setMessage("Are you sure you want to delete this page? This operation cannot be undone.")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteCurrentPic();
                        }
                    })
                    .setNegativeButton("Don't Delete", null).show();
        }else if (v.equals(finishB)){
            Askname();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                float deltaX = x2 - x1;

                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    if (x2 > x1) {
                        prevB.performClick();
                    }else {
                        nextB.performClick();
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }
}
