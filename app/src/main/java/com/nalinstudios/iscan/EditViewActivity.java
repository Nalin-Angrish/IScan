package com.nalinstudios.iscan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
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
import com.nalinstudios.iscan.graphics.ImageProcessor;
import com.nalinstudios.iscan.internal.Statics;

import org.opencv.core.Point;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity to handle all the editing functionality.
 * @author Nalin Angrish.
 */
@SuppressWarnings("ConstantConditions")
public class EditViewActivity extends AppCompatActivity implements View.OnClickListener {
    /** A FAB to handle submit events. */
    FloatingActionButton finishB;
    /** Buttons to handle 'next and previous image' commands. (Also handled by the sliding functionality) */
    Button nextB, prevB;
    /** Button to delete the current image */
    ImageButton  delB;
    /** Main ImageView to show the current image */
    ImageView imgview;
    /** The directory containing all the images for this session */
    File dir;
    /** The index of the image to be displayed */
    int index = 0;
    /** The minimum distance between the starting and ending points of the swipes by the user to handle 'next and previous image' commands */
    final int MIN_DISTANCE = 100;
    /** The starting and ending points of the swipes */
    float x1, x2;

    /**
     * The oncreate function to load the opencv library and initialize the main function.
     * @param savedInstanceState The state of the saved instance. This state is not used.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_view);
        try {
            System.loadLibrary("opencv_java");
            main();
        }catch (Exception e){
            Log.println(Log.ASSERT, "OPENCV", e.toString());
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "OpenCV couldn't be loaded. please try again.", Toast.LENGTH_LONG).show();
        }
    }


    /** The main function of this activity. It is only called when the OpenCV library is successfully loaded. It performs the main functions of the activity */
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
        List<Point> corners = ImageProcessor.getCorners(image);
        imgview.setImageBitmap(image);
        showCorners(corners, image);
    }


    /**
     * This function will delete the current picture being shown.
     * It will also perform the back/forward button press to show the previous/next picture respectively.
     */
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

    /**
     * This function will ask the user to enter the name of the PDF to be saved.
     */
    protected void Askname(){
        View p = getLayoutInflater().inflate(R.layout.popup_enter_name, null);

        final PopupWindow window = new PopupWindow(p, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        window.setAnimationStyle(android.R.style.Animation_Dialog);
        window.showAtLocation(p, Gravity.CENTER, 0, 0);
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


    /**
     * A function to arrange the imageViews representing the corners in the position of the detected corners.
     * @param corners The list of corners.
     * @param image The image on which to show (It is required to calculate the constraints for the corners
     */
    protected void showCorners(List<Point> corners, Bitmap image){
        List<ImageView> markers = new ArrayList<>();
        markers.add((ImageView) findViewById(R.id.c1));
        markers.add((ImageView) findViewById(R.id.c2));
        markers.add((ImageView) findViewById(R.id.c3));
        markers.add((ImageView) findViewById(R.id.c4));

        ConstraintLayout cornerLayer = findViewById(R.id.ev_act_main);

        int imgViewHeight, imgViewWidth;
        imgViewWidth = Resources.getSystem().getDisplayMetrics().widthPixels -20; // as the imageView has its constraints (10 dp on either side)
        imgViewHeight = (image.getHeight() * imgViewWidth)/image.getWidth(); //some math

        for (int i=0;i<4;i++){
            Point point = corners.get(i);

            double constraintTop = ((imgViewHeight*point.y)/image.getHeight())+150;   //some math
            double constraintLeft = ((imgViewWidth*point.x)/image.getWidth());    //some math

            Log.println(Log.ASSERT, "Constraints", constraintLeft+" x "+constraintTop);

            ImageView marker = markers.get(i);
            ConstraintSet set = new ConstraintSet();
            set.clone(cornerLayer);
            set.connect(marker.getId(), ConstraintSet.LEFT, cornerLayer.getId(), ConstraintSet.LEFT, (int)constraintLeft);
            set.connect(marker.getId(), ConstraintSet.TOP, cornerLayer.getId(), ConstraintSet.TOP, (int)constraintTop);
            set.applyTo(cornerLayer);
            cornerLayer.bringChildToFront(marker);

            marker.setVisibility(View.VISIBLE);
        }

    }


    /**
     * This function will handle all the button pressed and redirect to the one we need to.
     * @param v the view object of the button.
     */
    @Override
    public void onClick(View v){
        if (v.equals(nextB)){
            index=index+1;
            try {
                File imagefile = dir.listFiles()[index];
                Bitmap bitmap = BitmapFactory.decodeFile(imagefile.getAbsolutePath());
                List<Point> corners = ImageProcessor.getCorners(bitmap);
                showCorners(corners, bitmap);
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
                List<Point> corners = ImageProcessor.getCorners(bitmap);
                showCorners(corners, bitmap);
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


    /**
     * A function to handle the next/previous swipes to change the image.
     * @param event the motionEvent to react upon
     * @return super.onTouchEvent(event)
     */
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
