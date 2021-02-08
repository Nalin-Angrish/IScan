package com.nalinstudios.iscan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nalinstudios.iscan.internal.LockedHScrollView;


/**
 * A class to give a simple walkthrough to inform the user about how to use the app.
 * @author Nalin Angrish.
 */
public class TutorialActivity extends AppCompatActivity {
    /** A ratio defining the relative size of each slide of the tutorial */
    public final float ratio = 0.75f;


    /**
     * The main function of the class to show the slide show.
     * @param savedInstanceState The saved instance. not used.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);


        ImageButton nextB = findViewById(R.id.nextSlide);
        ImageButton prevB = findViewById(R.id.previousSlide);
        nextB.setOnClickListener(new SlideListener());
        prevB.setOnClickListener(new SlideListener());

        Button skip_finishB = findViewById(R.id.skip);
        skip_finishB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getApplication().getSharedPreferences("IScan", MODE_PRIVATE)
                        .edit().putBoolean("firstTime", false).apply();
                Intent i = new Intent(TutorialActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });





        LinearLayout layout = findViewById(R.id.slideLayout);
        Bitmap[] images = new Bitmap[]{
                BitmapFactory.decodeResource(getResources(), R.raw.slide1),
                BitmapFactory.decodeResource(getResources(), R.raw.slide2),
                BitmapFactory.decodeResource(getResources(), R.raw.slide3),
                BitmapFactory.decodeResource(getResources(), R.raw.slide4),
                BitmapFactory.decodeResource(getResources(), R.raw.slide5),
                BitmapFactory.decodeResource(getResources(), R.raw.slide6)
        };

        for (Bitmap image:images){
            int width = (int)(getResources().getDisplayMetrics().widthPixels * ratio);


            ImageView imageView = new ImageView(this);
            imageView.setImageBitmap(image);
            imageView.setMaxWidth(width);


            FrameLayout imageHolder = new FrameLayout(this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    width,
                    getResources().getDisplayMetrics().heightPixels,
                    Gravity.CENTER);
            imageHolder.setLayoutParams(params);


            FrameLayout imageHolderParent = new FrameLayout(this);
            FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(
                    getResources().getDisplayMetrics().widthPixels,
                    getResources().getDisplayMetrics().heightPixels,
                    Gravity.CENTER);
            imageHolderParent.setLayoutParams(params2);


            imageHolder.addView(imageView);
            imageHolderParent.addView(imageHolder);
            layout.addView(imageHolderParent);
        }
    }


    /**
     * A Click listener to navigate through the slides.
     */
    protected class SlideListener implements View.OnClickListener{
        /**
         * The main click listener function.
         * @param v the view clicked. used to check whether the back key is pressed or the front key.
         */
        @Override
        public void onClick(View v){
            LockedHScrollView layout = findViewById(R.id.lockedHScrollView);
            if (v == findViewById(R.id.nextSlide)){
                layout.scrollRight();
            }else {
                layout.scrollLeft();
            }
        }
    }
}
