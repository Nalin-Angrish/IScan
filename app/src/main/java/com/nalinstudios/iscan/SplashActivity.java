package com.nalinstudios.iscan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;


/**
 * A splash screen for the app.
 * @author Nalin Angrish.
 */
public class SplashActivity extends AppCompatActivity {


    /**
     * The on create method of SplashActivity.
     * @param savedInstanceState The state of the saved instance. This state is not used.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        ImageView imgView = findViewById(R.id.splash);    // The imageView to show the splash icon
        imgView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.icon_foreground));



        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i;
                if (getApplication().getSharedPreferences("IScan", MODE_PRIVATE).getBoolean("firstTime", true)){
                    i = new Intent(SplashActivity.this, TutorialActivity.class);
                }else {
                    i = new Intent(SplashActivity.this, MainActivity.class);
                }
                startActivity(i);
                finish();
            }
        }, 2000);
    }
}
